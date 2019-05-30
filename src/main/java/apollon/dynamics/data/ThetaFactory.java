package apollon.dynamics.data;

import apollon.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ThetaFactory {
    private final List<double[]> columns = new ArrayList<>();

    private final List<String> names = new ArrayList<>();

    private final double[][] data;

    private final String[] variables;

    private final int dimension;

    private final int size;

    public ThetaFactory(@NotNull double[][] data, @NotNull String[] variables) {
        this.data = data;
        this.variables = variables;
        size = data.length;
        dimension = data[0].length;
    }

    public void addPolynoms(int order) {
        if (order > 0) {
            addPolynoms(order - 1);
        }
        addPoly(0, 0, order, new int[order]);
    }

    private void addPoly(int start, int index, int order, @NotNull int[] indices) {
        if (order == 0) {
            addPolyColumn(indices);
            addPolyName(indices);
            return;
        }
        for (int i = start; i < dimension; i++) {
            indices[index] = i;
            addPoly(i, index + 1, order - 1, indices);
        }
    }

    private void addPolyColumn(@NotNull int[] indices) {
        double[] row = new double[size];
        for (int i = 0; i < row.length; i++) {
            double product = 1;
            for (int index : indices) {
                product *= data[i][index];
            }
            row[i] = product;
        }
        columns.add(row);
    }

    private void addPolyName(@NotNull int[] indices) {
        if (indices.length == 0) {
            names.add("1");
            return;
        }
        names.add(IntStream.of(indices).mapToObj(index -> variables[index]).collect(Collectors.joining("*")));
    }

    public void addTrigonometry(int maxCoefficient) {
        for (int k = 0; k <= maxCoefficient; k++) {
            addTrig(k);
        }
    }

    private void addTrig(int k) {
        addColumns(value -> Math.sin(k * value));
        addNames("sin(" + k + "*", ")");
        addColumns(value -> Math.cos(k * value));
        addNames("cos(" + k + "*", ")");
    }

    private void addColumns(@NotNull DoubleUnaryOperator operator) {
        for (int i = 0; i < dimension; i++) {
            double[] column = new double[size];
            for (int j = 0; j < size; j++) {
                column[j] = operator.applyAsDouble(data[j][i]);
            }
            columns.add(column);
        }
    }

    private void addNames(@NotNull String prefix, @NotNull String suffix) {
        Arrays.stream(variables).map(variable -> prefix + variable + suffix).forEach(names::add);
    }

    @NotNull
    public double[][] createTheta() {
        return Util.swapDimension(columns.toArray(double[][]::new));
    }

    @NotNull
    public String[] createNames() {
        return names.toArray(String[]::new);
    }
}
