package apollon.dynamics.data;

import apollon.util.Util;
import org.apache.commons.lang3.ArrayUtils;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class XiComputer {
    private static final Logger LOGGER = LoggerFactory.getLogger(XiComputer.class);

    private final double[][] theta;

    private final double[][] derivative;

    private final double threshold;

    private final int iterations;

    private final int dimension;

    private final int columns;

    private final int size;

    public XiComputer(@NotNull double[][] theta, @NotNull double[][] derivative, double threshold, int iterations) {
        this.theta = theta;
        this.derivative = Util.swapDimension(derivative);
        this.threshold = threshold;
        this.iterations = iterations;
        dimension = this.derivative.length;
        columns = theta.length;
        size = derivative.length;
    }

    @NotNull
    public double[][] compute() {
        double[][] xi = new double[dimension][];
        for (int i = 0; i < dimension; i++) {
            LOGGER.info("Dimension {}/{}", i + 1, dimension);
            xi[i] = compute(derivative[i]);
        }
        return xi;
    }

    @NotNull
    private double[] compute(@NotNull double[] derivative) {
        double[] xi = solveLeastSquares(derivative);
        for (int k = 0; k < iterations; k++) {
            LOGGER.info("- Iteration {}/{}", k + 1, iterations);
            int[] smallIndices = computeSmallIndices(xi);
            for (int index : smallIndices) {
                xi[index] = 0;
            }
            int[] bigIndices = computeInverse(smallIndices);
            if (bigIndices.length < dimension) {
                break;
            }
            double[] result = solveLeastSquares(derivative, bigIndices);
            for (int i = 0; i < bigIndices.length; i++) {
                xi[bigIndices[i]] = result[i];
            }
        }
        return xi;
    }

    @NotNull
    private int[] computeSmallIndices(@NotNull double[] xi) {
        int[] indices = new int[columns];
        int size = 0;
        for (int i = 0; i < columns; i++) {
            if (Math.abs(xi[i]) < threshold) {
                indices[size++] = i;
            }
        }
        return Arrays.copyOf(indices, size);
    }

    @NotNull
    private int[] computeInverse(@NotNull int[] smallIndices) {
        int[] indices = new int[columns - smallIndices.length];
        int size = 0;
        for (int i = 0, j = 0; i < columns; i++) {
            if (j < smallIndices.length && i == smallIndices[j]) {
                j++;
                continue;
            }
            indices[size++] = i;
        }
        return indices;
    }

    @NotNull
    private double[] solveLeastSquares(@NotNull double[] data) {
        return solveLeastSquares(theta, data);
    }

    @NotNull
    private double[] solveLeastSquares(@NotNull double[] data, @NotNull int[] columns) {
        double[][] theta = new double[columns.length][size];
        for (int i = 0; i < columns.length; i++) {
            System.arraycopy(this.theta[columns[i]], 0, theta[i], 0, size);
        }
        return solveLeastSquares(theta, data);
    }

    @NotNull
    private double[] solveLeastSquares(@NotNull double[][] theta, @NotNull double[] data) {
        SimpleMatrix a = new SimpleMatrix(Util.swapDimension(theta));
        SimpleMatrix x = new SimpleMatrix(theta.length, 1);
        SimpleMatrix b = new SimpleMatrix(size, 1, false, data);
        LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.leastSquares(size, theta.length);
        if (!solver.setA(a.getDDRM())) {
            System.out.println("AAAAH!");
        }
        solver.solve(b.getDDRM(), x.getDDRM());
        double[] result = x.getDDRM().data;
        if (ArrayUtils.contains(result, Double.NaN)) {
            System.out.println("NaN!!!");
        }
        return result;
    }
}
