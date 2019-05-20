package apollon.homology.one;

import apollon.homology.one.action.Action;
import apollon.voronoi.Voronoi;
import org.apache.commons.lang3.ArrayUtils;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class HomologyOne {
    private final List<Cycle> cycles = new ArrayList<>();

    private final List<Action> actions = new ArrayList<>();

    private final Graph graph = new Graph();

    private final ActionGenerator generator;

    private final Voronoi voronoi;

    public HomologyOne(@NotNull Voronoi voronoi) {
        this.voronoi = voronoi;
        generator = new ActionGenerator(voronoi, graph);
    }

    public void compute() {
        init();
        actions.addAll(generator.generate());
    }

    private void init() {
        cycles.clear();
        actions.clear();
        graph.init(voronoi.getSitesCount());
    }

    public synchronized void executeActions() {
        while (!actions.isEmpty()) {
            actions.remove(0).execute(this);
        }
    }

    public synchronized void executeNextAction() {
        if (!actions.isEmpty()) {
            actions.remove(0).execute(this);
        }
    }

    public void addEdgeAndCycle(@NotNull Site a, @NotNull Site b, int edge, double radius) {
        Optional<Circle> optionalCircle = graph.find(a, b);
        addEdge(a, b, edge);
        optionalCircle.ifPresent(circle -> addCycle(circle.append(edge), radius));
    }

    public void addEdge(@NotNull Site a, @NotNull Site b, int edge) {
        graph.addEdge(a, b, edge);
    }

    public void addCycle(@NotNull Circle circle, double radius) {
        cycles.add(new Cycle(circle, radius));
        cycles.sort(Comparator.naturalOrder());
    }

    public void addRelation(@NotNull Circle circle, double radius) {
        if (circle.isEmpty()) {
            return;
        }
        if (circle.size() == 1 || graph.hasNoLoops(circle)) {
            remove(circle.getEdges());
        }
        else if (graph.hasOnlyLoops(circle)) {
            replaceLoop(circle);
        }
        else {
            replaceNonLoop(circle);
        }
        killEmptyCycles(radius);
        killObsoleteCycles(radius);
    }

    private void replaceLoop(@NotNull Circle circle) {
        int edge = circle.getSingleEdge();
        int[] edges = circle.getInverse(edge);
        replace(edge, edges);
        graph.remove(edge);
    }

    private void replaceNonLoop(@NotNull Circle circle) {
        int edge = graph.getSingleNonLoop(circle);
        int[] edges = circle.getInverse(edge);
        replace(edge, edges);
        remove(graph.getNonLoops(circle));
    }

    private void replace(int edge, @NotNull int[] edges) {
        actions.forEach(action -> action.replace(edge, edges));
        cycles.forEach(cycle -> cycle.replace(edge, edges));
    }

    private void remove(@NotNull int... edges) {
        actions.forEach(action -> action.remove(edges));
        cycles.forEach(cycle -> cycle.remove(edges));
        graph.remove(edges);
    }

    private void killEmptyCycles(double radius) {
        alive().forEach(cycle -> cycle.killIfEmpty(radius));
    }

    private void killObsoleteCycles(double radius) {
        List<Cycle> cycles = alive().collect(Collectors.toList());
        if (cycles.size() < 2) {
            return;
        }
        SimpleMatrix matrix = createMatrix(cycles);
        SimpleMatrix nullSpace = matrix.svd().nullSpace();
        while (nullSpace.numRows() > 0 && nullSpace.numCols() > 0) {
            for (int column = 0; column < nullSpace.numCols(); column++) {
                OptionalInt row = findNonZeroRow(nullSpace, column, cycles.size());
                if (row.isPresent()) {
                    cycles.remove(row.getAsInt()).kill(radius);
                    break;
                }
            }
            matrix = createMatrix(cycles);
            nullSpace = matrix.svd().nullSpace();
        }
    }

    @NotNull
    private OptionalInt findNonZeroRow(@NotNull SimpleMatrix matrix, int column, int cycles) {
        for (int row = cycles - 1; row >= 0; row--) {
            if (matrix.get(row, column) != 0) {
                return OptionalInt.of(row);
            }
        }
        return OptionalInt.empty();
    }

    @NotNull
    private SimpleMatrix createMatrix(@NotNull List<Cycle> cycles) {
        int[] allEdges = graph.getEdgeIndices();
        double[][] columns = cycles.stream().map(Cycle::getCircle).map(Circle::getEdges).map(edges -> getColumn(edges, allEdges)).toArray(double[][]::new);
        SimpleMatrix matrix = new SimpleMatrix(allEdges.length, columns.length);
        for (int i = 0; i < columns.length; i++) {
            matrix.setColumn(i, 0, columns[i]);
        }
        return matrix;
    }

    @NotNull
    private double[] getColumn(@NotNull int[] edges, @NotNull int[] allEdges) {
        double[] column = new double[allEdges.length];
        IntStream.of(edges).forEach(edge -> column[ArrayUtils.indexOf(allEdges, Graph.positive(edge))] += edge);
        return column;
    }

    @NotNull
    private Stream<Cycle> alive() {
        return all().filter(Cycle::isAlive);
    }

    @NotNull
    private Stream<Cycle> all() {
        return cycles.stream();
    }

    @NotNull
    public double[][] plot() {
        return cycles.stream().filter(Cycle::wasLiving).map(cycle -> new double[]{cycle.getBorn(), cycle.getDied()}).toArray(double[][]::new);
    }

    public void render(@NotNull Graphics g) {
        graph.render(g, voronoi::getSite);
        renderHomology(g);
    }

    private void renderHomology(@NotNull Graphics g) {
        int y = 30;
        g.setColor(Color.BLACK);
        g.drawString("Homology 1:", 5, y);
        y += 20;
        for (Cycle cycle : cycles) {
            g.setColor(cycle.isAlive() ? Color.GREEN : Color.RED);
            g.drawString(cycle.toString(), 10, y);
            y += 20;
        }
        g.setColor(Color.BLACK);
        g.drawString("Actions:", 5, y);
        y += 20;
        for (Action action : actions) {
            g.setColor(action.getColor());
            g.drawString(action.toString(), 10, y);
            y += 20;
        }
    }

    @Override
    public String toString() {
        return "[" + cycles.stream().map(Object::toString).collect(Collectors.joining("\r\n")) + "]";
    }
}
