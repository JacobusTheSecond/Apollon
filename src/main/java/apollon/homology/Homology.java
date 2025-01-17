package apollon.homology;

import apollon.homology.action.Action;
import apollon.voronoi.Voronoi;
import org.apache.commons.lang3.ArrayUtils;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Homology {
    private static final Logger LOGGER = LoggerFactory.getLogger(Homology.class);

    private final List<Cycle> cycles = new ArrayList<>();

    private final List<Action> actions = new ArrayList<>();

    private final Graph graph = new Graph();

    private final ActionGenerator generator;

    private final Voronoi voronoi;

    private double[] zero;

    private double scale;

    private boolean contractEdges = false;

    public Homology(@NotNull Voronoi voronoi) {
        this.voronoi = voronoi;
        generator = new ActionGenerator(voronoi, graph);
    }

    public void toggleContractEdges() {
        contractEdges = !contractEdges;
        compute();
    }

    public boolean isContractEdges() {
        return contractEdges;
    }

    public void compute() {
        init();
        actions.addAll(generator.generate());
        scale = 1;
    }

    private void init() {
        initZero();
        initOne();
    }

    private void initOne() {
        cycles.clear();
        actions.clear();
        graph.init(voronoi.getSitesCount());
    }

    private void initZero() {
        int components = voronoi.getSitesCount();
        zero = new double[components];
        Arrays.fill(zero, Double.POSITIVE_INFINITY);
    }

    public synchronized void executeActions() {
        LOGGER.info("==> Executing actions...");
        long start = System.currentTimeMillis();
        while (!actions.isEmpty()) {
            executeAction();
        }
        start = System.currentTimeMillis() - start;
        LOGGER.info("<== Executed actions in {} ms", start);
    }

    public synchronized void executeNextAction() {
        if (!actions.isEmpty()) {
            executeAction();
        }
    }

    private void executeAction() {
        actions.remove(0).execute(this);
        if (actions.isEmpty()) {
            scale = Arrays.stream(zero).filter(Double::isFinite).map(radius -> 1 / radius).min().orElse(1);
        }
    }

    public void addEdge(@NotNull Site source, @NotNull Site target, int edge, boolean addCycle, double radius) {
        if (contractEdges) {
            addAndContractEdge(source, target, edge, addCycle, radius);
            return;
        }
        if (!addCycle) {
            addEdge(source, target, edge, radius);
            return;
        }
        Optional<Circuit> optionalCircle = graph.find(source, target);
        addEdge(source, target, edge, radius);
        optionalCircle.ifPresent(circle -> addCycle(circle.append(Graph.inverse(edge)), radius));
    }

    private void addAndContractEdge(@NotNull Site source, @NotNull Site target, int edge, boolean addCycle, double radius) {
        addEdge(source, target, edge, radius);
        if (addCycle && source.equals(target)) {
            addCycle(new Circuit(edge), radius);
            return;
        }
        if (!source.equals(target)) {
            remove(edge);
        }
    }

    private void addEdge(@NotNull Site source, @NotNull Site target, int edge, double radius) {
        addZero(source, target, radius);
        graph.addEdge(source, target, edge);
    }

    private void addZero(@NotNull Site source, @NotNull Site target, double radius) {
        Site a = source.getComponent();
        Site b = target.getComponent();
        if (a.index() == b.index()) {
            return;
        }
        Site min = a.index() > b.index() ? b : a;
        Site max = a.index() > b.index() ? a : b;
        zero[max.index()] = radius;
        max.setComponent(min);
    }

    private void addCycle(@NotNull Circuit circuit, double radius) {
        cycles.add(new Cycle(circuit, radius));
        cycles.sort(Comparator.naturalOrder());
    }

    public void addRelation(@NotNull Circuit circuit, double radius) {
        if (circuit.isEmpty()) {
            return;
        }
        if (circuit.size() == 1 || graph.hasNoLoops(circuit)) {
            remove(circuit.getEdges());
        }
        else if (graph.hasOnlyLoops(circuit)) {
            replaceLoop(circuit);
        }
        else {
            replaceNonLoop(circuit);
        }
        killEmptyCycles(radius);
        killObsoleteCycles(radius);
    }

    private void replaceLoop(@NotNull Circuit circuit) {
        int edge = circuit.getSingleEdge();
        int[] edges = circuit.getInverse(edge);
        replace(edge, edges);
        graph.remove(edge);
    }

    private void replaceNonLoop(@NotNull Circuit circuit) {
        int edge = graph.getSingleNonLoop(circuit);
        int[] edges = circuit.getInverse(edge);
        replace(edge, edges);
        remove(graph.getNonLoops(circuit));
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
        double[][] columns = cycles.stream().map(Cycle::getCircuit).map(Circuit::getEdges).map(edges -> getColumn(edges, allEdges)).toArray(double[][]::new);
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
    public double[][] plotOne() {
        return cycles.stream().filter(Cycle::wasLiving).map(cycle -> new double[]{scale * cycle.getBorn(), scale * cycle.getDied()}).toArray(double[][]::new);
    }

    @NotNull
    public double[][] plotZero() {
        return DoubleStream.of(zero).filter(Double::isFinite).map(radius -> scale * radius).mapToObj(radius -> new double[]{0, radius}).toArray(double[][]::new);
    }

    public void render(@NotNull Graphics g) {
        graph.render(voronoi::getSite, cycles, g);
    }

    public void renderCycles(@NotNull Graphics g) {
        int y = 10;
        g.setColor(Color.BLACK);
        g.drawString("Cycles:", 5, y);
        y += 20;
        for (Cycle cycle : all().sorted(this::compareCycles).toArray(Cycle[]::new)) {
            g.setColor(cycle.isAlive() ? Color.GREEN : Color.RED);
            g.drawString(cycle.toString(), 10, y);
            y += 20;
        }
    }

    public void renderActions(@NotNull Graphics g, int width) {
        int y = 10;
        g.setColor(Color.BLACK);
        renderString("Actions:", y, g, width);
        y += 20;
        for (Action action : actions) {
            g.setColor(action.getColor());
            renderString(action.toString(), y, g, width);
            y += 20;
        }
    }

    private int compareCycles(@NotNull Cycle a, @NotNull Cycle b) {
        int difference = -Boolean.compare(a.isAlive(), b.isAlive());
        if (difference != 0) {
            return difference;
        }
        return a.compareTo(b);
    }

    private void renderString(@NotNull String value, int y, @NotNull Graphics g, int width) {
        g.drawString(value, width - g.getFontMetrics().stringWidth(value) - 5, y);
    }

    @Override
    public String toString() {
        return "[" + cycles.stream().map(Object::toString).collect(Collectors.joining("\r\n")) + "]";
    }
}
