package apollon.distance;

import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.flow.mincost.CapacityScalingMinimumCostFlow;
import org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem;
import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public class Wasserstein extends AbstractGraphDistance {
    private double p = 1;

    private int s, t;

    private int[] xY, xJ, hY;

    private int[] capacities;

    private boolean[] flow;

    public void setP(double p) {
        this.p = p;
    }

    public double getP() {
        return p;
    }

    @Override
    protected double compute() {
        int[] x = createVertices(getXPoints());
        int[] y = createVertices(getYPoints());

        s = createVertex();
        t = createVertex();
        int h = createVertex();
        int j = createVertex();

        createEdges(0, s, x);
        int[] sH = createEdges(0, s, h);

        xY = createEdges(0, x, y);
        xJ = createEdges(0, x, j);

        hY = createEdges(0, h, y);
        int[] hJ = createEdges(0, h, j);

        int[] jT = createEdges(0, j, t);
        createEdges(0, y, t);

        capacities = new int[getEdgeCount()];

        Arrays.fill(capacities, 1);
        setEdgeCapacities(getYCount(), sH);
        setEdgeCapacities(getXCount(), jT);
        setEdgeCapacities(Math.min(getXCount(), getYCount()), hJ);

        setEdgeWeights((a, b) -> factor(distance(a, b)), xY);
        setEdgeWeights((a, b) -> factor(distanceToDiagonal(a)), xJ);
        setEdgeWeights((a, b) -> factor(distanceToDiagonal(b)), hY);

        MinimumCostFlowProblem<Integer, Integer> problem = new MinimumCostFlowProblem.MinimumCostFlowProblemImpl<>(getGraph(), node -> {
            if (node == s) {
                return getSum();
            }
            if (node == t) {
                return -getSum();
            }
            return 0;
        }, edge -> capacities[edge]);

        this.flow = new boolean[getEdgeCount()];
        MinimumCostFlowAlgorithm.MinimumCostFlow<Integer> flow = new CapacityScalingMinimumCostFlow<Integer, Integer>().getMinimumCostFlow(problem);
        forEachEdge(edge -> this.flow[edge] = flow.getFlow(edge) > 0);
        return defactor(flow.getCost());
    }

    private void setEdgeCapacities(int value, @NotNull int[] edges) {
        IntStream.of(edges).forEach(edge -> capacities[edge] = value);
    }

    public void forEachXY(@NotNull BiConsumer<PointD, PointD> operation) {
        IntStream.of(xY).filter(edge -> flow[edge]).forEach(edge -> operation.accept(getSource(edge), getTarget(edge)));
    }

    public void forEachX(@NotNull BiConsumer<PointD, PointD> operation) {
        IntStream.of(xJ).filter(edge -> flow[edge]).forEach(edge -> {
            PointD x = getSource(edge);
            double mid = (x.x + x.y) / 2;
            PointD d = new PointD(mid, mid);
            operation.accept(x, d);
        });
    }

    public void forEachY(@NotNull BiConsumer<PointD, PointD> operation) {
        IntStream.of(hY).filter(edge -> flow[edge]).forEach(edge -> {
            PointD y = getTarget(edge);
            double mid = (y.x + y.y) / 2;
            PointD d = new PointD(mid, mid);
            operation.accept(y, d);
        });
    }
}
