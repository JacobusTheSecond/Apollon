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

    private int[] xY, xJ, hY;

    private Edge[] edges;

    private boolean[] flow;

    private int max;

    @Override
    protected double compute() {
        int[] x = createVertices(getXPoints());
        int[] y = createVertices(getYPoints());

        s = createVertex();
        t = createVertex();
        int h = createVertex();
        int j = createVertex();

        createEdges(1, s, x);
        createEdges(getYCount(), s, h);

        xY = createEdges(1, x, y);
        xJ = createEdges(1, x, j);

        hY = createEdges(1, h, y);
        createEdges(Math.max(getXCount(), getYCount()), h, j);

        createEdges(1, y, t);
        createEdges(getXCount(), j, t);

        edges = computeSortedEdges();

        max = Util.findMin(edges.length, this::hasFlow);
        addRemoveEdges(max);
        MaximumFlowAlgorithm.MaximumFlow<Integer> flow = new PushRelabelMFImpl<>(getGraph()).getMaximumFlow(s, t);
        this.flow = new boolean[edges.length];
        for (int i = 0; i < max; i++) {
            this.flow[i] = flow.getFlow(edges[i].getEdge()) > 0;
        }
        return edges[max].getCost();
    }

    private boolean hasFlow(int index) {
        addRemoveEdges(index);
        return new PushRelabelMFImpl<>(getGraph()).calculateMinCut(s, t) == getSum();
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
        Edge[] edges = new Edge[xY.length + xJ.length + hY.length];
        int size = 0;
        for (int edge : xY) {
            edges[size++] = new Edge(edge, getSourceIndex(edge), getTargetIndex(edge));
        }
        for (int edge : xJ) {
            edges[size++] = new Edge(edge, getSourceIndex(edge), getTargetIndex(edge), false);
        }
        for (int edge : hY) {
            edges[size++] = new Edge(edge, getSourceIndex(edge), getTargetIndex(edge), true);
        }
        Arrays.sort(edges);
        //TODO Apply easy bounds here
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

        private final boolean hOrJ;

        private final boolean h;

        private boolean active = true;

        public Edge(int edge, int source, int target) {
            this.edge = edge;
            this.source = source;
            this.target = target;
            hOrJ = false;
            h = false;
            cost = distance(source, target);
        }

        public Edge(int edge, int source, int target, boolean h) {
            this.edge = edge;
            this.source = source;
            this.target = target;
            this.h = h;
            hOrJ = true;
            cost = distanceToDiagonal(h ? target : source);
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
            if (!hOrJ) {
                operation.accept(get(source), get(target));
                return;
            }
            PointD y = get(h ? target : source);
            double mid = (y.x + y.y) / 2;
            PointD d = new PointD(mid, mid);
            operation.accept(y, d);
        }

        @Override
        public int compareTo(@NotNull Bottleneck.Edge o) {
            return Double.compare(cost, o.cost);
        }
    }
}
