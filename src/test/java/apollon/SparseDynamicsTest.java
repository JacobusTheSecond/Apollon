package apollon;

import apollon.dynamics.SparseDynamics;
import apollon.dynamics.data.DataSource;
import apollon.dynamics.data.Noise;
import apollon.dynamics.data.theta.ColumnGenerator;
import apollon.dynamics.data.theta.ThetaConfig;
import apollon.dynamics.data.theta.Variables;
import apollon.util.Util;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SparseDynamicsTest {
    @Test
    public void testLorenz() {
        double dt = .001;
        DataSource dataSource = DataSource.lorenz(10, 28, 8. / 3);
        double[][] data = dataSource.createData(dt, 100, new double[]{-8, 8, 27});
        Noise.gaussian(.01).apply(data);

        double[][] derivative = DataSource.createDerivative(dt, data);
        Noise.gaussian(.01).apply(derivative);

        ThetaConfig config = ThetaConfig.builder(dataSource.getVariables()).append(ColumnGenerator.one()).append(ColumnGenerator.polynom(2)).build();
        SparseDynamics dynamics = SparseDynamics.builder().iterations(10).theta(config).data(data, dataSource.getVariables()).derivative(derivative).threshold(.025).build();
        dynamics.compute();

        print(dataSource, dynamics);
        addPlot(data);
        addPlot(dynamics, dt, data.length);
        Util.plot3D();
    }

    @Test
    public void testGolf() {
        double dt = .001;
        DataSource dataSource = DataSource.goldBall3D(-9.81);
        double[][] data = dataSource.createData(dt, Double.MAX_VALUE, new double[]{0, 0, 1, 3, 4, 30}, state -> state[2] > 0);
        Noise.gaussian(.001).apply(data);
        int[][] indices = {{0, 1, 2}};

        double[][] derivative = DataSource.createDerivative(dt, data);
        Noise.gaussian(.001).apply(derivative);

        ThetaConfig config = ThetaConfig.builder(dataSource.getVariables()).append(ColumnGenerator.one()).append(ColumnGenerator.polynom(2)).build();
        SparseDynamics dynamics = SparseDynamics.builder().iterations(20).theta(config).data(data, dataSource.getVariables()).derivative(derivative).threshold(.05).build();
        dynamics.compute();

        print(dataSource, dynamics);
        addPlot(data, indices);
        addPlot(dynamics, dt, 5 * data.length, indices);
        Util.plot3D();
    }

    @Test
    public void testFootball() {
        Variables variables = new Variables("x1", "y1", "z1", "x2", "y2", "z2", "x3", "y3", "z3");
        int[][] indices = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}};

        File file = new File("C:\\Users\\Vince\\PycharmProjects\\Reinthal_Conradi_LAB\\out\\data.txt");
        double[][] data = SparseDynamics.loadData(file);

        double[] coefficients = IntStream.rangeClosed(1, 10).mapToDouble(i -> (double) i).toArray();
        ThetaConfig config = ThetaConfig.builder(variables).append(ColumnGenerator.one()).append(ColumnGenerator.polynom(3)).append(ColumnGenerator.sin(coefficients))
                .append(ColumnGenerator.cos(coefficients)).build();
        SparseDynamics dynamics = SparseDynamics.builder().iterations(50).theta(config).data(data, variables).derivative(1 / 480.).threshold(.05).build();
        dynamics.compute();

        print(dynamics);
        addPlot(data, indices);
        addPlot(dynamics, 1 / 480., data.length, indices);
        Util.plot3D();
    }

    private void print(@NotNull DataSource dataSource, @NotNull SparseDynamics dynamics) {
        System.out.println(dataSource + ":\n");
        print(dynamics);
    }

    private void print(@NotNull SparseDynamics dynamics) {
        double[][] xi = dynamics.getXi();
        System.out.println("Xi: " + Arrays.toString(dynamics.getNames()));
        for (int i = 0; i < xi.length; i++) {
            System.out.println("Xi " + i + ": " + Arrays.toString(xi[i]));
        }
        System.out.println("\nEquations:");
        for (String equation : dynamics.getEquations()) {
            System.out.println(equation);
        }
    }

    private void addPlot(@NotNull SparseDynamics dynamics, double dt, int size, @NotNull int[]... indices) {
        double[][] data = dynamics.createData(dt, size);
        addPlot(data, indices);
    }

    private void addPlot(double[][] data, @NotNull int[]... indices) {
        double[][] swappedData = Util.swapDimension(data);
        if (indices.length == 0) {
            indices = new int[][]{IntStream.range(0, swappedData.length).toArray()};
        }
        double[][][] dataSets = Stream.of(indices).map(index -> Util.swapDimension(IntStream.of(index).mapToObj(i -> swappedData[i]).toArray(double[][]::new)))
                .toArray(double[][][]::new);
        Util.addPlot3D(dataSets);
    }
}