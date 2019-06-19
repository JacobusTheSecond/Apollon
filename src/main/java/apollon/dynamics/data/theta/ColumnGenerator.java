package apollon.dynamics.data.theta;

import apollon.dynamics.data.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ColumnGenerator {
    private final Function<Data, Columns> generator;

    private Columns columns;

    public ColumnGenerator(@NotNull Function<Data, Columns> generator) {
        this.generator = generator;
    }

    @NotNull
    public Columns generate(@NotNull Data data) {
        if (columns == null) {
            columns = generator.apply(data);
        }
        return columns;
    }

    @NotNull
    public static ColumnGenerator linear() {
        return create(data -> new Columns(data) {
            @Override
            public void generate() {
                double[] ones = new double[getSize()];
                Arrays.fill(ones, 1);
                add("1", ones);
                addForEachVariable(UnaryOperator.identity(), DoubleUnaryOperator.identity());
            }
        });
    }

    @NotNull
    public static ColumnGenerator polynoms(int order) {
        if (order < 2) {
            throw new IllegalArgumentException("Use linear() for linear terms");
        }
        return create(data -> new Columns(data) {
            @Override
            public void generate() {
                generate(order);
            }

            private void generate(int order) {
                if (order > 2) {
                    generate(order - 1);
                }
                generate(0, 0, order, new int[order]);
            }

            private void generate(int start, int index, int order, @NotNull int[] indices) {
                if (order == 0) {
                    add(indices);
                    return;
                }
                for (int i = start; i < getDimension(); i++) {
                    indices[index] = i;
                    generate(i, index + 1, order - 1, indices);
                }
            }

            private void add(@NotNull int[] indices) {
                String name = IntStream.of(indices).mapToObj(getData()::getVariable).collect(Collectors.joining("*"));
                double[] row = new double[getSize()];
                for (int i = 0; i < row.length; i++) {
                    double product = 1;
                    for (int index : indices) {
                        product *= getData().get(index, i);
                    }
                    row[i] = product;
                }
                add(name, row);
            }
        });
    }

    @NotNull
    public static ColumnGenerator trigonometry(int maxCoefficient) {
        return create(data -> new Columns(data) {
            @Override
            public void generate() {
                for (int i = 1; i <= maxCoefficient; i++) {
                    int k = i;
                    addForEachVariable(variable -> "sin(" + k + "*" + variable + ")", Math::sin);
                    addForEachVariable(variable -> "cos(" + k + "*" + variable + ")", Math::cos);
                }
            }
        });
    }

    @NotNull
    public static ColumnGenerator signum() {
        return function(variable -> "signum(" + variable + ")", Math::signum);
    }

    @NotNull
    public static ColumnGenerator signum(@NotNull ColumnGenerator argument) {
        return function(argument, name -> "signum(" + name + ")", Math::signum);
    }

    @NotNull
    public static ColumnGenerator absolute() {
        return function(variable -> "|" + variable + "|", Math::abs);
    }

    @NotNull
    public static ColumnGenerator absolute(@NotNull ColumnGenerator argument) {
        return function(argument, name -> "|" + name + "|", Math::abs);
    }

    @NotNull
    public static ColumnGenerator function(@NotNull UnaryOperator<String> nameFunction, @NotNull DoubleUnaryOperator function) {
        return create(data -> new Columns(data) {
            @Override
            public void generate() {
                addForEachVariable(nameFunction, function);
            }
        });
    }

    @NotNull
    public static ColumnGenerator function(@NotNull ColumnGenerator argument, @NotNull UnaryOperator<String> nameFunction, @NotNull DoubleUnaryOperator function) {
        return create(data -> new Columns(data) {
            @Override
            public void generate() {
                argument.generate(data).forEachColumn((name, column) -> add(nameFunction.apply(name), apply(column, function)));
            }
        });
    }

    @NotNull
    public static ColumnGenerator function(@NotNull ColumnGenerator firstArgument, @NotNull ColumnGenerator secondArgument,
                                           @NotNull BiFunction<String, String, String> nameFunction, @NotNull DoubleBinaryOperator function) {
        return create(data -> new Columns(data) {
            @Override
            public void generate() {
                Columns firstColumns = firstArgument.generate(data);
                Columns secondColumns = secondArgument.generate(data);
                firstColumns.forEachColumn((firstName, firstColumn) -> secondColumns
                        .forEachColumn((secondName, secondColumn) -> add(nameFunction.apply(firstName, secondName), apply(firstColumn, secondColumn, function))));
            }
        });
    }

    @NotNull
    private static ColumnGenerator create(@NotNull Function<Data, Columns> generator) {
        return new ColumnGenerator(generator);
    }
}
