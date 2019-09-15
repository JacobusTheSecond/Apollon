package apollon.dynamics;

import apollon.dynamics.data.Data;
import apollon.dynamics.data.EquationGenerator;
import apollon.dynamics.data.XiComputer;
import apollon.dynamics.data.theta.ThetaConfig;
import apollon.dynamics.data.theta.Variables;
import apollon.util.Util;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class SparseDynamics {
    private final ThetaConfig config;

    private final int iterations;

    private final double threshold;

    private final Data data;

    private final double[][] derivative;

    private double[][] theta;

    private double[][] xi;

    private String[] names;

    public SparseDynamics(@NotNull Builder builder) {
        config = builder.config;
        iterations = builder.iterations;
        threshold = builder.threshold;
        data = builder.data;
        derivative = builder.derivative;
    }

    public void compute() {
        computeTheta();
        computeXi();
    }

    public int getDimension() {
        return data.getDimension();
    }

    @NotNull
    public double[][] createData(double dt, int size) {
        double[][] data = new double[size][getDimension()];
        data[0] = this.data.getRow(0);
        for (int i = 1; i < size; i++) {
            data[i] = computeStep(data[i - 1], dt);
        }
        return data;
    }

    @NotNull
    private double[] computeStep(@NotNull double[] state, double dt) {
        return IntStream.range(0, state.length).mapToDouble(i -> state[i] + dt * computeDerivative(state, i)).toArray();
    }

    private double computeDerivative(@NotNull double[] state, int index) {
        double[] theta = config.generate(state);
        double[] xi = getXi()[index];
        return IntStream.range(0, theta.length).mapToDouble(i -> theta[i] * xi[i]).sum();
    }

    @NotNull
    public double[][] getXi() {
        return xi;
    }

    @NotNull
    public String[] getNames() {
        return names;
    }

    @NotNull
    public String[] getEquations() {
        return new EquationGenerator(xi, data.getVariables(), names).createEquations();
    }

    private void computeTheta() {
        theta = config.generate(data.getData());
        names = config.getNames();
    }

    private void computeXi() {
        xi = new XiComputer(theta, derivative, threshold, iterations).compute();
    }

    @NotNull
    public static double[][] loadData(@NotNull File file, @NotNull int... indices) {
        LineIterator iterator = null;
        try {
            iterator = IOUtils.lineIterator(new FileInputStream(file), StandardCharsets.UTF_8);
            List<double[]> rows = new ArrayList<>();
            while (iterator.hasNext()) {
                parseRow(iterator.nextLine(), indices).ifPresent(rows::add);
            }
            return rows.toArray(double[][]::new);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    @NotNull
    private static Optional<double[]> parseRow(@NotNull String line, @NotNull int... indices) {
        if (StringUtils.isBlank(line)) {
            return Optional.empty();
        }
        return Optional.of(Arrays.stream(line.split(",")).map(String::trim).mapToDouble(Double::parseDouble).toArray()).map(array -> filterIndices(array, indices));
    }

    @NotNull
    private static double[] filterIndices(@NotNull double[] data, @NotNull int... indices) {
        if (indices.length == 0) {
            return data;
        }
        return Arrays.stream(indices).mapToDouble(index -> data[index]).toArray();
    }

    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ThetaConfig config;

        private Data data;

        private double[][] derivative;

        private int iterations = 10;

        private double threshold = .05;

        @NotNull
        public Builder theta(@NotNull ThetaConfig config) {
            this.config = config;
            return this;
        }

        @NotNull
        public Builder iterations(int iterations) {
            this.iterations = iterations;
            return this;
        }

        @NotNull
        public Builder threshold(double threshold) {
            this.threshold = threshold;
            return this;
        }

        @NotNull
        public Builder data(@NotNull File file, @NotNull Variables variables, @NotNull int... indices) {
            return data(loadData(file, indices), variables);
        }

        @NotNull
        public Builder data(@NotNull double[][] data, @NotNull Variables variables) {
            return data(new Data(data, variables));
        }

        @NotNull
        public Builder data(@NotNull Data data) {
            this.data = data;
            return this;
        }

        @NotNull
        public Builder derivative(@NotNull File file) {
            return derivative(loadData(file));
        }

        @NotNull
        public Builder derivative(@NotNull double[][] derivative) {
            this.derivative = derivative;
            return this;
        }

        @NotNull
        public Builder derivative(double dt) {
            this.derivative = data.createDerivative(dt);
            return this;
        }

        @NotNull
        public SparseDynamics build() {
            if (data == null) {
                throw new IllegalStateException("Cannot create dynamics without data.");
            }
            if (derivative == null) {
                derivative = Util.derivate(data.getData());
            }
            return new SparseDynamics(this);
        }
    }
}
