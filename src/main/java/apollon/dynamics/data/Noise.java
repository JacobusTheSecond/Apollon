package apollon.dynamics.data;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

@FunctionalInterface
public interface Noise {
    double noise();

    default void apply(@NotNull double[][] data) {
        for (double[] state : data) {
            apply(state);
        }
    }

    default void apply(@NotNull double[] state) {
        for (int i = 0; i < state.length; i++) {
            state[i] += noise();
        }
    }

    @NotNull
    static Noise gaussian() {
        return gaussian(1);
    }

    @NotNull
    static Noise gaussian(double factor) {
        Random random = new Random();
        return () -> random.nextGaussian() * factor;
    }
}
