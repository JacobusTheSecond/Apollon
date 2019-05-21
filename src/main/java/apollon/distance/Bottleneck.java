package apollon.distance;

import apollon.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public class Bottleneck extends AbstractGraphDistance {
    private int s, t;

    private int[] x, y;

    private int[] sToX, sToH, xToY, hToY, yToT;

    private Edge[] edges;

    private boolean[] flow;

    private int max;

    @Override
    protected double compute() {
        x = createVertices(getXPoints());
        y = createVertices(getYPoints());

        s = createVertex();
        t = createVertex();
        int h = isDifferent() ? createVertex() : -1;

        sToX = createEdges(1, s, x);
        sToH = isDifferent() ? createEdges(y.length - x.length, s, h) : new int[0];
        xToY = createEdges(1, x, y);
        hToY = isDifferent() ? createEdges(1, h, y) : new int[0];
        yToT = createEdges(1, y, t);

        edges = computeSortedEdges();

        //TODO Apply easy bounds
        max = Util.findMin(edges.length, this::hasFlow);
        addRemoveEdges(max);
        MaximumFlowAlgorithm.MaximumFlow<Integer> flow = new PushRelabelMFImpl<>(getGraph()).getMaximumFlow(s, t);
        this.flow = new boolean[edges.length];
        for (int i = 0; i < max; i++) {
            this.flow[i] = flow.getFlow(edges[i].getEdge()) > 0;
        }
        return edges[max].getCost();
    }

    protected boolean isDifferent() {
        return x.length < y.length;
    }

    private boolean hasFlow(int index) {
        addRemoveEdges(index);
        return new PushRelabelMFImpl<>(getGraph()).calculateMinCut(s, t) == y.length;
    }

    private void addRemoveEdges(int index) {
        for (int i = 0; i < edges.length; i++) {
            if (i <= index) {
                edges[i].add();
            }
            else {
                edges[i].remove();
            }
        }
    }

    @NotNull
    private Edge[] computeSortedEdges() {
        Edge[] edges = new Edge[xToY.length + hToY.length];
        for (int i = 0; i < xToY.length; i++) {
            int edge = xToY[i];
            edges[i] = new Edge(edge, getSourceIndex(edge), getTargetIndex(edge), false);
        }
        for (int i = 0; i < hToY.length; i++) {
            int edge = hToY[i];
            edges[i + xToY.length] = new Edge(edge, getSourceIndex(edge), getTargetIndex(edge), true);
        }
        Arrays.sort(edges);
        return edges;
    }

    public void forEachEdge(@NotNull BiConsumer<PointD, PointD> operation) {
        IntStream.range(0, edges.length).filter(index -> flow[index]).mapToObj(index -> edges[index]).forEach(edge -> edge.run(operation));
    }

    public void forMaxEdge(@NotNull BiConsumer<PointD, PointD> operation) {
        edges[max].run(operation);
    }

    public class Edge implements Comparable<Edge> {
        private final double cost;

        private final int source;

        private final int target;

        private final int edge;

        private final boolean h;

        private boolean active = true;

        public Edge(int edge, int source, int target, boolean h) {
            this.edge = edge;
            this.source = source;
            this.target = target;
            this.h = h;
            cost = h ? distanceToDiagonal(target) : distance(source, target);
        }

        public int getEdge() {
            return edge;
        }

        public double getCost() {
            return cost;
        }

        public void remove() {
            if (active) {
                removeEdge(edge);
                active = false;
            }
        }

        public void add() {
            if (!active) {
                addEdge(source, target, edge, 1);
                active = true;
            }
        }

        public void run(@NotNull BiConsumer<PointD, PointD> operation) {
            if (h) {
                PointD y = get(target);
                double mid = (y.x + y.y) / 2;
                PointD d = new PointD(mid, mid);
                operation.accept(y, d);
            }
            else {
                operation.accept(get(source), get(target));
            }
        }

        @Override
        public int compareTo(@NotNull Bottleneck.Edge o) {
            return Double.compare(cost, o.cost);
        }
    }
}
