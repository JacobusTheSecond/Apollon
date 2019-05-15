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
    private int s, t;

    private int[] x, y;

    private int[] sToX, sToH, xToY, hToY, yToT;

    private int[] capacities;

    private boolean[] flow;

    @Override
    protected double compute() {
        x = createVertices(getXPoints());
        y = createVertices(getYPoints());

        s = createVertex();
        t = createVertex();
        int h = isDifferent() ? createVertex() : -1;

        sToX = createEdges(0, s, x);
        sToH = isDifferent() ? createEdges(0, s, h) : new int[0];
        xToY = createEdges(0, x, y);
        hToY = isDifferent() ? createEdges(0, h, y) : new int[0];
        yToT = createEdges(0, y, t);

        capacities = new int[getEdgeCount()];

        Arrays.fill(capacities, 1);
        setEdgeCapacities(y.length - x.length, sToH);

        setEdgeWeights((a, b) -> factor(distance(a, b)), xToY);
        setEdgeWeights((a, b) -> factor(distanceToDiagonal(b)), hToY);

        MinimumCostFlowProblem<Integer, Integer> problem = new MinimumCostFlowProblem.MinimumCostFlowProblemImpl<>(getGraph(), node -> {
            if (node == s) {
                return y.length;
            }
            if (node == t) {
                return -y.length;
            }
            return 0;
        }, edge -> capacities[edge]);

        this.flow = new boolean[getEdgeCount()];
        MinimumCostFlowAlgorithm.MinimumCostFlow<Integer> flow = new CapacityScalingMinimumCostFlow<Integer, Integer>().getMinimumCostFlow(problem);
        forEachEdge(edge -> this.flow[edge] = flow.getFlow(edge) > 0);
        return defactor(flow.getCost());
    }

    protected boolean isDifferent() {
        return x.length < y.length;
    }

    private void setEdgeCapacities(int value, @NotNull int[] edges) {
        IntStream.of(edges).forEach(edge -> capacities[edge] = value);
    }

    public void forEachXY(@NotNull BiConsumer<PointD, PointD> operation) {
        IntStream.of(xToY).filter(edge -> flow[edge]).forEach(edge -> operation.accept(getSource(edge), getTarget(edge)));
    }

    public void forEachY(@NotNull BiConsumer<PointD, PointD> operation) {
        IntStream.of(hToY).filter(edge -> flow[edge]).forEach(edge -> {
            PointD y = getTarget(edge);
            double mid = (y.x + y.y) / 2;
            PointD d = new PointD(mid, mid);
            operation.accept(y, d);
        });
    }
}
