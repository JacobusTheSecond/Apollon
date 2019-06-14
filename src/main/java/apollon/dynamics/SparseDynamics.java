package apollon.dynamics;

import apollon.dynamics.data.Data;
import apollon.dynamics.data.EquationGenerator;
import apollon.dynamics.data.XiComputer;
import apollon.dynamics.data.theta.ThetaConfiguration;
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

public class SparseDynamics {
    private final ThetaConfiguration configuration;

    private final int iterations;

    private final double threshold;

    private final Data data;

    private final double[][] derivative;

    private double[][] theta;

    private double[][] xi;

    private String[] names;

    public SparseDynamics(@NotNull Builder builder) {
        configuration = builder.configuration;
        iterations = builder.iterations;
        threshold = builder.threshold;
        data = builder.data;
        derivative = builder.derivative;
    }

    public void compute() {
        computeTheta();
        computeXi();
    }

    @NotNull
    public double[][] getXi() {
        return xi;
    }

    @NotNull
    public String[] getEquations() {
        return new EquationGenerator(xi, data.getVariables(), names).createEquations();
    }

    private void computeTheta() {
        configuration.generate(data);
        theta = configuration.createTheta();
        names = configuration.createNames();
    }

    private void computeXi() {
        xi = new XiComputer(theta, derivative, threshold, iterations).compute();
    }

    @NotNull
    private static double[][] loadData(@NotNull File file) {
        LineIterator iterator = null;
        try {
            iterator = IOUtils.lineIterator(new FileInputStream(file), StandardCharsets.UTF_8);
            List<double[]> rows = new ArrayList<>();
            while (iterator.hasNext()) {
                parseRow(iterator.nextLine()).ifPresent(rows::add);
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
    private static Optional<double[]> parseRow(@NotNull String line) {
        if (StringUtils.isBlank(line)) {
            return Optional.empty();
        }
        return Optional.of(Arrays.stream(line.split(",")).mapToDouble(Double::parseDouble).toArray());
    }

    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ThetaConfiguration configuration = new ThetaConfiguration();

        private Data data;

        private double[][] derivative;

        private int iterations = 10;

        private double threshold = .05;

        @NotNull
        public Builder theta(@NotNull ThetaConfiguration configuration) {
            this.configuration = configuration;
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
        public Builder data(@NotNull File file, @NotNull String[] variables) {
            return data(loadData(file), variables);
        }

        @NotNull
        public Builder data(@NotNull double[][] data, @NotNull String[] variables) {
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
