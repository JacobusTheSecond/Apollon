package apollon;

import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.flow.mincost.CapacityScalingMinimumCostFlow;
import org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem;
import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;

public class Wasserstein {
    private static final double SQRT_2 = Math.sqrt(2);

    private static final int FACTOR = 100;

    private final List<PointD> xPoints = new ArrayList<>();

    private final List<PointD> yPoints = new ArrayList<>();

    private final AtomicInteger vertices = new AtomicInteger();

    private final AtomicInteger edges = new AtomicInteger();

    private final SimpleDirectedWeightedGraph<Integer, Integer> graph = new SimpleDirectedWeightedGraph<>(vertices::getAndIncrement, edges::getAndIncrement);

    private int s, t;

    private int[] x, y;

    private int[] sToX, sToH, xToY, hToY, yToT;

    private int[] capacities;

    private boolean[] flow;

    private boolean computed = false;

    public void compute(@NotNull Collection<PointD> x, @NotNull Collection<PointD> y) {
        clear();
        if (x.size() <= y.size()) {
            xPoints.addAll(x);
            yPoints.addAll(y);
        }
        else {
            xPoints.addAll(y);
            yPoints.addAll(x);
        }
        compute();
    }

    private void clear() {
        computed = false;
        xPoints.clear();
        yPoints.clear();
        vertices.set(0);
        edges.set(0);
        graph.removeAllVertices(new HashSet<>(graph.vertexSet()));
    }

    private void compute() {
        x = createVertices(xPoints);
        y = createVertices(yPoints);

        s = graph.addVertex();
        t = graph.addVertex();
        int h = graph.addVertex();

        sToX = createEdges(s, x);
        sToH = createEdges(s, h);
        xToY = createEdges(x, y);
        hToY = createEdges(h, y);
        yToT = createEdges(y, t);

        capacities = new int[edges.get()];

        Arrays.fill(capacities, 1);
        setEdgeCapacities(y.length - x.length, sToH);

        setEdgeWeights(0, sToX);
        setEdgeWeights(0, sToH);
        setEdgeWeights(0, yToT);
        setEdgeWeights((a, b) -> get(a).subtract(get(b)).length(), xToY);
        setEdgeWeights((a, b) -> distanceToDiagonal(get(b)), hToY);

        MinimumCostFlowProblem<Integer, Integer> problem = new MinimumCostFlowProblem.MinimumCostFlowProblemImpl<>(graph, node -> {
            if (node == s) {
                return y.length;
            }
            if (node == t) {
                return -y.length;
            }
            return 0;
        }, edge -> capacities[edge]);

        this.flow = new boolean[edges.get()];
        MinimumCostFlowAlgorithm.MinimumCostFlow<Integer> flow = new CapacityScalingMinimumCostFlow<Integer, Integer>().getMinimumCostFlow(problem);
        for (int edge : graph.edgeSet()) {
            this.flow[edge] = flow.getFlow(edge) > 0;
        }
        computed = true;
    }

    @NotNull
    private PointD get(int vertex) {
        if (vertex < x.length) {
            return xPoints.get(vertex);
        }
        return yPoints.get(vertex - x.length);
    }

    private double distanceToDiagonal(@NotNull PointD point) {
        return Math.abs(point.y - point.x) / SQRT_2;
    }

    @NotNull
    private int[] createVertices(@NotNull List<PointD> points) {
        int[] vertices = new int[points.size()];
        IntStream.range(0, vertices.length).forEach(i -> vertices[i] = graph.addVertex());
        return vertices;
    }

    @NotNull
    private int[] createEdges(int start, @NotNull int... end) {
        int[] edges = new int[end.length];
        int size = 0;
        for (int b : end) {
            edges[size++] = graph.addEdge(start, b);
        }
        return edges;
    }

    @NotNull
    private int[] createEdges(@NotNull int[] start, @NotNull int... end) {
        int[] edges = new int[start.length * end.length];
        int size = 0;
        for (int a : start) {
            for (int b : end) {
                edges[size++] = graph.addEdge(a, b);
            }
        }
        return edges;
    }

    private void setEdgeCapacities(int value, @NotNull int[] edges) {
        IntStream.of(edges).forEach(edge -> capacities[edge] = value);
    }

    private void setEdgeCapacities(@NotNull IntBinaryOperator function, @NotNull int[] edges) {
        for (int edge : edges) {
            int a = graph.getEdgeSource(edge);
            int b = graph.getEdgeTarget(edge);
            capacities[edge] = function.applyAsInt(a, b);
        }
    }

    private void setEdgeWeights(int value, @NotNull int[] edges) {
        IntStream.of(edges).forEach(edge -> setWeight(edge, value));
    }

    private void setEdgeWeights(@NotNull Metric metric, @NotNull int[] edges) {
        for (int edge : edges) {
            int a = graph.getEdgeSource(edge);
            int b = graph.getEdgeTarget(edge);
            setWeight(edge, metric.of(a, b));
        }
    }

    private void setWeight(int edge, double weight) {
        graph.setEdgeWeight(edge, Math.round(FACTOR * weight));
    }

    public boolean isComputed() {
        return computed;
    }

    public void forEachXY(@NotNull BiConsumer<PointD, PointD> operation) {
        IntStream.of(xToY).filter(edge -> flow[edge]).forEach(edge -> {
            PointD x = get(graph.getEdgeSource(edge));
            PointD y = get(graph.getEdgeTarget(edge));
            operation.accept(x, y);
        });
    }

    public void forEachY(@NotNull BiConsumer<PointD, PointD> operation) {
        IntStream.of(hToY).filter(edge -> flow[edge]).forEach(edge -> {
            PointD y = get(graph.getEdgeTarget(edge));
            double mid = (y.x + y.y) / 2;
            PointD d = new PointD(mid, mid);
            operation.accept(y, d);
        });
    }

    @FunctionalInterface
    interface Metric {
        double of(int a, int b);
    }
}
