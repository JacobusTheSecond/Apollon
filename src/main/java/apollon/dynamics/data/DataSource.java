package apollon.dynamics.data;

import apollon.dynamics.data.theta.Variables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface DataSource {
    @NotNull Variables getVariables();

    @NotNull double[] derivative(double t, @NotNull double[] state);

    default void apply(double t, double dt, @NotNull double[] state) {
        double[] derivative = derivative(t, state);
        for (int i = 0; i < state.length; i++) {
            state[i] += dt * derivative[i];
        }
    }

    @NotNull
    static double[] derivative(double dt, @NotNull double[] current, @NotNull double[] next) {
        double[] difference = new double[current.length];
        for (int j = 0; j < current.length; j++) {
            difference[j] = (next[j] - current[j]) / dt;
        }
        return difference;
    }

    @NotNull
    default double[][] createData(double dt, double maxT, @NotNull double[] initialState) {
        return createData(dt, maxT, initialState, state -> true);
    }

    @NotNull
    default double[][] createData(double dt, double maxT, @NotNull double[] initialState, @NotNull Predicate<double[]> predicate) {
        return createData(0, dt, maxT, initialState, predicate);
    }

    @NotNull
    default double[][] createData(double t0, double dt, double maxT, @NotNull double[] initialState) {
        return createData(t0, dt, maxT, initialState, state -> true);
    }

    @NotNull
    default double[][] createData(double t0, double dt, double maxT, @NotNull double[] initialState, @NotNull Predicate<double[]> predicate) {
        List<double[]> data = new ArrayList<>();
        double t = t0;
        double[] state = Arrays.copyOf(initialState, initialState.length);
        while (t <= maxT && predicate.test(state)) {
            data.add(Arrays.copyOf(state, state.length));
            apply(t, dt, state);
            t += dt;
        }
        return data.toArray(double[][]::new);
    }

    @NotNull
    static double[][] createDerivative(double dt, @NotNull double[][] data) {
        double[][] derivative = new double[data.length][];
        for (int i = 0; i < derivative.length - 1; i++) {
            derivative[i] = derivative(dt, data[i], data[i + 1]);
        }
        derivative[data.length - 1] = new double[derivative[0].length];
        return derivative;
    }

    @NotNull
    static DataSource lorenz(double delta, double rho, double beta) {
        String display = "Lorenz[" + delta + ", " + rho + ", " + beta + "]\ndx = " + -delta + "*x + " + delta + "*y\ndy = " + rho + "*x - y - x*z\ndz = " + -beta + "*z + " + "x*y";
        return of((t, state) -> new double[]{delta * (state[1] - state[0]), state[0] * (rho - state[2]) - state[1], state[0] * state[1] - beta * state[2]}, display, "x", "y", "z");
    }

    @NotNull
    static DataSource goldBall1D(double gravity) {
        String display = "1D-GolfBall[" + gravity + "]\ndx = vx\ndvx = -9.81";
        return of((t, state) -> new double[]{state[1], gravity}, display, "x", "vx");
    }

    @NotNull
    static DataSource goldBall1DAirResistance(double gravity, double radius) {
        String display = "1D-GolfBall-AirResistance[" + gravity + "]\ndx = vx\ndvx = -9.81 + k * signum(vx) * vx^2";
        double k = 1.2 * .3 * Math.PI * radius * radius;
        return of((t, state) -> new double[]{state[1], gravity - k * Math.signum(state[1]) * state[1] * state[1]}, display, "x", "vx");
    }

    @NotNull
    static DataSource goldBall2D(double gravity) {
        String display = "2D-GolfBall[" + gravity + "]\ndx = vx\ndy = vy\ndvx = 0\ndvy = -9.81";
        return of((t, state) -> new double[]{state[2], state[3], 0, gravity}, display, "x", "y", "vx", "vy");
    }

    @NotNull
    static DataSource goldBall3D(double gravity) {
        String display = "3D-GolfBall[" + gravity + "]\ndx = vx\ndy = vy\ndz = vz\ndvx = 0\ndvy = 0\ndvz = -9.81";
        return of((t, state) -> new double[]{state[3], state[4], state[5], 0, 0, gravity}, display, "x", "y", "z", "vx", "vy", "vz");
    }

    @NotNull
    static DataSource of(@NotNull BiFunction<Double, double[], double[]> derivative, @Nullable String display, @NotNull String... variables) {
        return new DataSource() {
            @NotNull
            @Override
            public Variables getVariables() {
                return new Variables(variables);
            }

            @NotNull
            @Override
            public double[] derivative(double t, @NotNull double[] state) {
                return derivative.apply(t, state);
            }

            @Override
            public String toString() {
                return display != null ? display : super.toString();
            }
        };
    }
}
