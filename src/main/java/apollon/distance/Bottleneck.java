package apollon.distance;

import apollon.GeometryUtil;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.flow.GusfieldGomoryHuCutTree;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.kynosarges.tektosyne.geometry.PointD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.BiConsumer;

public class Bottleneck extends AbstractGraphDistance {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bottleneck.class);

    private int s, t;

    private int[] x, y;

    private int[] sToX, sToH, xToY, hToY, yToT;

    private Edge[] edges;

    private int max;

    public Bottleneck() {
        super(SimpleWeightedGraph::new);
    }

    @Override
    protected double compute() {
        x = createVertices(getXPoints());
        y = createVertices(getYPoints());

        s = createVertex();
        t = createVertex();
        int h = createVertex();

        sToX = createEdges(1, s, x);
        sToH = createEdges(y.length, s, h);
        xToY = createEdges(1, x, y);
        hToY = createEdges(1, h, y);
        yToT = createEdges(1, y, t);

        edges = computeSortedEdges();

        //TODO Apply easy bounds
        max = GeometryUtil.findMin(edges.length, this::hasFlow);
        if (max == -1) {
            LOGGER.warn("AAAAAH");
            return 0;
        }
        return edges[max].getCost();
    }

    private boolean hasFlow(int index) {
        for (int i = 0; i < edges.length; i++) {
            if (i <= index) {
                edges[i].add();
            }
            else {
                edges[i].remove();
            }
        }
        return new GusfieldGomoryHuCutTree<>(getGraph()).calculateMinCut(s, t) == y.length;
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
        // for (int i = 0; i < max; i++) {
        //     edges[i].run(operation);
        // }
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
                addEdge(source, target, edge, cost);
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
