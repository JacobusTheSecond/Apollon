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
import java.util.stream.Stream;

public class HomologyOne {
    private final List<Cycle> cycles = new ArrayList<>();

    private final List<Circle> relations = new ArrayList<>();

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
        relations.clear();
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

    public void addEdge(int a, int b, int edge, double radius) {
        Optional<Circle> optionalCircle = graph.find(a, b);
        graph.addEdge(a, b, edge);
        optionalCircle.ifPresent(circle -> addCycle(new Circle(edge, circle.getEdges()), radius));
    }

    public void addCycle(@NotNull Circle circle, double radius) {
        cycles.add(new Cycle(circle, radius));
        cycles.sort(Comparator.naturalOrder());
    }

    public void addRelation(@NotNull Circle circle, double radius) {
        relations.add(circle);
        killObsoleteCycles(radius);
    }

    private void killObsoleteCycles(double radius) {
        List<Cycle> cycles = alive().collect(Collectors.toList());
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
        List<Circle> circles = new ArrayList<>();
        cycles.stream().map(Cycle::getCircle).forEach(circles::add);
        circles.addAll(relations);
        double[][] columns = circles.stream().map(Circle::getEdges).map(this::direct).map(edges -> getColumn(edges, allEdges)).toArray(double[][]::new);
        SimpleMatrix matrix = new SimpleMatrix(allEdges.length, columns.length);
        for (int i = 0; i < columns.length; i++) {
            matrix.setColumn(i, 0, columns[i]);
        }
        return matrix;
    }

    @NotNull
    private int[] direct(@NotNull int[] edges) {
        int[] directed = new int[edges.length];
        int start = graph.getSameSite(edges[0], edges[edges.length - 1]);
        int end;
        for (int i = 0; i < edges.length; i++) {
            int edge = edges[i];
            end = graph.getOtherSite(edge, start);
            directed[i] = start < end ? edge : -edge - 1;
            start = end;
        }
        return directed;
    }

    @NotNull
    private double[] getColumn(@NotNull int[] edges, @NotNull int[] allEdges) {
        double[] column = new double[allEdges.length];
        for (int directedEdge : edges) {
            boolean positive = directedEdge >= 0;
            int edge = positive ? directedEdge : -directedEdge - 1;
            column[ArrayUtils.indexOf(allEdges, edge)] += positive ? 1 : -1;
        }
        return column;
    }

    public void render(@NotNull Graphics g) {
        graph.render(g, voronoi::getSite);
        if (actions.isEmpty()) {
            renderHomology(g);
        }
    }

    private void renderHomology(@NotNull Graphics g) {
        g.setColor(Color.BLACK);
        g.drawString("Hom1: " + this, 0, 30);
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

    @Override
    public String toString() {
        return cycles.stream().map(cycle -> "(" + cycle.getBorn() + ", " + cycle.getDied() + ")").collect(Collectors.toList()).toString();
    }
}
