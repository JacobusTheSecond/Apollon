package apollon.dynamics.data.theta;

import apollon.dynamics.data.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.DoubleStream;

public abstract class Columns {
    private final List<double[]> columns = new ArrayList<>();

    private final List<String> names = new ArrayList<>();

    private final Data data;

    protected Columns(@NotNull Data data) {
        this.data = data;
    }

    public void add(@NotNull String name, @NotNull double[] column) {
        columns.add(column);
        names.add(name);
    }

    public void addAll(@NotNull Columns columns) {
        columns.forEachColumn(this::add);
    }

    @NotNull
    public Data getData() {
        return data;
    }

    public int getDimension() {
        return data.getDimension();
    }

    public int getSize() {
        return data.getSize();
    }

    public abstract void generate();

    public void addForEachVariable(@NotNull UnaryOperator<String> nameFunction, @NotNull DoubleUnaryOperator function) {
        forEachVariable((variable, column) -> add(nameFunction.apply(variable), apply(column, function)));
    }

    public void forEachVariable(@NotNull BiConsumer<String, double[]> operation) {
        getData().forEach(operation);
    }

    public void forEachColumn(@NotNull BiConsumer<String, double[]> operation) {
        for (int i = 0; i < columns.size(); i++) {
            operation.accept(names.get(i), columns.get(i));
        }
    }

    @NotNull
    public double[][] createTheta() {
        return columns.toArray(double[][]::new);
    }

    @NotNull
    public String[] createNames() {
        return names.toArray(String[]::new);
    }

    @Override
    public String toString() {
        return names.toString();
    }

    @NotNull
    public static double[] apply(@NotNull double[] column, @NotNull DoubleUnaryOperator function) {
        return DoubleStream.of(column).map(function).toArray();
    }

    @NotNull
    public static double[] apply(@NotNull double[] firstColumn, @NotNull double[] secondColumn, @NotNull DoubleBinaryOperator function) {
        double[] column = new double[firstColumn.length];
        for (int i = 0; i < column.length; i++) {
            column[i] = function.applyAsDouble(firstColumn[i], secondColumn[i]);
        }
        return column;
    }
}
