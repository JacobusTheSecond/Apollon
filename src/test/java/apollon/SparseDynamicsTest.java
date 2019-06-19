package apollon;

import apollon.dynamics.SparseDynamics;
import apollon.dynamics.data.DataSource;
import apollon.dynamics.data.Noise;
import apollon.dynamics.data.theta.ColumnGenerator;
import apollon.dynamics.data.theta.ThetaConfiguration;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;

public class SparseDynamicsTest {
    @Test
    public void testLorenz() {
        double dt = .001;
        DataSource dataSource = DataSource.lorenz(10, 28, 8. / 3);
        double[][] data = dataSource.createData(dt, 100, new double[]{-8, 8, 27});
        Noise.gaussian(.01).apply(data);
        double[][] derivative = dataSource.createDerivative(dt, data);
        Noise.gaussian(.01).apply(derivative);
        ThetaConfiguration configuration = new ThetaConfiguration().add(ColumnGenerator.polynoms(2));
        SparseDynamics dynamics = SparseDynamics.builder().iterations(10).theta(configuration).data(data, dataSource.getVariables()).derivative(derivative).threshold(.025).build();
        dynamics.compute();
        print(dataSource, dynamics);
    }

    @Test
    public void testGolf() {
        double dt = .001;
        DataSource dataSource = DataSource.goldBall1DAirResistance(-9.81, 1);
        double[][] data = dataSource.createData(dt, Double.MAX_VALUE, new double[]{1, 100}, state -> state[0] > 0);
        Noise.gaussian(.001).apply(data);
        double[][] derivative = dataSource.createDerivative(dt, data);
        ColumnGenerator signumSquare = ColumnGenerator
                .function(variable -> "signum(" + variable + ") * " + variable + "^2", variable -> Math.signum(variable) * variable * variable);
        ThetaConfiguration configuration = new ThetaConfiguration().add(signumSquare);
        SparseDynamics dynamics = SparseDynamics.builder().iterations(5).theta(configuration).data(data, dataSource.getVariables()).derivative(derivative).threshold(.05).build();
        dynamics.compute();
        print(dataSource, dynamics);
    }

    private void print(@NotNull DataSource dataSource, @NotNull SparseDynamics dynamics) {
        System.out.println(dataSource + ":\n");
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
}