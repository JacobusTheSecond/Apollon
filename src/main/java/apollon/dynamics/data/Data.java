package apollon.dynamics.data;

import apollon.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class Data {
    private final double[][] rawData;

    private final double[][] data;

    private final String[] variables;

    private final int dimension;

    private final int size;

    public Data(@NotNull double[][] data, @NotNull String[] variables) {
        this.rawData = data;
        this.data = Util.swapDimension(rawData);
        this.variables = variables;
        dimension = this.data.length;
        size = this.data[0].length;
    }

    @NotNull
    public double[][] getData() {
        return data;
    }

    @NotNull
    public String[] getVariables() {
        return variables;
    }

    public int getDimension() {
        return dimension;
    }

    public int getSize() {
        return size;
    }

    @NotNull
    public double[][] createDerivative(double dt) {
        return DataSource.createDerivative(dt, rawData);
    }

    public double get(int variable, int time) {
        return data[variable][time];
    }

    @NotNull
    public double[] getColumn(int index) {
        return data[index];
    }

    @NotNull
    public String getVariable(int index) {
        return variables[index];
    }

    public void forEach(@NotNull BiConsumer<String, double[]> operation) {
        for (int i = 0; i < getDimension(); i++) {
            operation.accept(getVariable(i), getColumn(i));
        }
    }

    @Override
    public String toString() {
        return "[" + getDimension() + ", " + getSize() + "]";
    }
}
