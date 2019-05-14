package apollon.distance;

import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public abstract class AbstractGraphDistance extends AbstractDistance {
    private static final double SQRT_2 = Math.sqrt(2);

    private static final int FACTOR = 1000;

    private final AtomicInteger vertices = new AtomicInteger();

    private final AtomicInteger edges = new AtomicInteger();

    private final Graph<Integer, Integer> graph;

    protected AbstractGraphDistance(@NotNull BiFunction<Supplier<Integer>, Supplier<Integer>, Graph<Integer, Integer>> constructor) {
        graph = constructor.apply(vertices::getAndIncrement, edges::getAndIncrement);
    }

    @Override
    protected void clear() {
        super.clear();
        vertices.set(0);
        edges.set(0);
        graph.removeAllVertices(new HashSet<>(graph.vertexSet()));
    }

    @NotNull
    protected PointD get(int vertex) {
        if (vertex < getXCount()) {
            return getX(vertex);
        }
        return getY(vertex - getXCount());
    }

    protected int createVertex() {
        return graph.addVertex();
    }

    @NotNull
    protected Graph<Integer, Integer> getGraph() {
        return graph;
    }

    @NotNull
    protected int[] createVertices(@NotNull List<PointD> points) {
        int[] vertices = new int[points.size()];
        IntStream.range(0, vertices.length).forEach(i -> vertices[i] = graph.addVertex());
        return vertices;
    }

    @NotNull
    protected int[] createEdges(double weight, int start, @NotNull int... end) {
        int[] edges = new int[end.length];
        int size = 0;
        for (int b : end) {
            edges[size++] = createEdge(start, b, weight);
        }
        return edges;
    }

    @NotNull
    protected int[] createEdges(double weight, @NotNull int[] start, @NotNull int... end) {
        int[] edges = new int[start.length * end.length];
        int size = 0;
        for (int a : start) {
            for (int b : end) {
                edges[size++] = createEdge(a, b, weight);
            }
        }
        return edges;
    }

    private int createEdge(int start, int end, double weight) {
        int edge = graph.addEdge(start, end);
        graph.setEdgeWeight(edge, weight);
        return edge;
    }

    protected void removeEdge(int edge) {
        graph.removeEdge(edge);
    }

    protected void addEdge(int source, int target, int edge, double weight) {
        graph.addEdge(source, target, edge);
        setWeight(edge, weight);
    }

    protected void addEdge(int source, int target, int edge) {
        graph.addEdge(source, target, edge);
    }

    protected void setEdgeWeights(@NotNull Metric metric, @NotNull int[] edges) {
        for (int edge : edges) {
            int a = graph.getEdgeSource(edge);
            int b = graph.getEdgeTarget(edge);
            setWeight(edge, metric.of(a, b));
        }
    }

    protected void setWeight(int edge, double weight) {
        graph.setEdgeWeight(edge, weight);
    }

    protected double getWeight(int edge) {
        return graph.getEdgeWeight(edge);
    }

    protected void forEachEdge(@NotNull IntConsumer operation) {
        graph.edgeSet().forEach(operation::accept);
    }

    protected int getSourceIndex(int edge) {
        return graph.getEdgeSource(edge);
    }

    protected int getTargetIndex(int edge) {
        return graph.getEdgeTarget(edge);
    }

    @NotNull
    protected PointD getSource(int edge) {
        return get(getSourceIndex(edge));
    }

    @NotNull
    protected PointD getTarget(int edge) {
        return get(getTargetIndex(edge));
    }

    protected int getEdgeCount() {
        return edges.get();
    }

    protected int getVertexCount() {
        return vertices.get();
    }

    protected double distance(int a, int b) {
        return distance(get(a), get(b));
    }

    protected double distance(@NotNull PointD a, @NotNull PointD b) {
        return a.subtract(b).length();
    }

    protected double distanceToDiagonal(int vertex) {
        return distanceToDiagonal(get(vertex));
    }

    protected double distanceToDiagonal(@NotNull PointD point) {
        return Math.abs(point.y - point.x) / SQRT_2;
    }

    protected double factor(double value) {
        return Math.round(FACTOR * value);
    }

    protected double defactor(double value) {
        return value / FACTOR;
    }

    @FunctionalInterface
    interface Metric {
        double of(int a, int b);
    }
}
