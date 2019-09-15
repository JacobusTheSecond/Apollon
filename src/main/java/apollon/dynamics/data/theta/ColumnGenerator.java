package apollon.dynamics.data.theta;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ColumnGenerator {
    private final List<Column> columns = new ArrayList<>();

    private final Generator generator;

    private boolean generated;

    private ColumnGenerator(@NotNull Generator generator) {
        this.generator = generator;
    }

    public void clear() {
        columns.clear();
        generated = false;
    }

    public void generate(@NotNull Variables variables) {
        if (generated) {
            return;
        }
        columns.addAll(generator.generate(variables));
        generated = true;
    }

    @NotNull
    public List<Column> getColumns() {
        return columns;
    }

    @NotNull
    public static ColumnGenerator one() {
        return of(variables -> Collections.singletonList(Column.one()));
    }

    @NotNull
    public static ColumnGenerator variables() {
        return variableFunction(Column::variable);
    }

    @NotNull
    public static ColumnGenerator signum() {
        return variableFunction(Column::signum);
    }

    @NotNull
    public static ColumnGenerator absolute() {
        return variableFunction(Column::absolute);
    }

    @NotNull
    public static ColumnGenerator polynom(int order) {
        if (order <= 0) {
            return one();
        }
        if (order == 1) {
            return variables();
        }
        return of(variables -> {
            List<List<Column>> orderColumns = createOrderList(order);
            for (int index = 0; index < variables.size(); index++) {
                List<List<Column>> orderTerms = createProducts(createPowers(variables, index, order), orderColumns, order);
                for (int i = 0; i < order; i++) {
                    orderColumns.get(i).addAll(orderTerms.get(i));
                }
            }
            List<Column> columns = new ArrayList<>();
            orderColumns.forEach(columns::addAll);
            return columns;
        });
    }

    @NotNull
    public static ColumnGenerator polynom(@NotNull ColumnGenerator terms, int order) {
        if (order <= 0) {
            return one();
        }
        if (order == 1) {
            return terms;
        }
        return of(variables -> {
            terms.generate(variables);
            List<List<Column>> orderColumns = createOrderList(order);
            for (Column term : terms.getColumns()) {
                List<List<Column>> orderTerms = createProducts(createPowers(term, order), orderColumns, order);
                for (int i = 0; i < order; i++) {
                    orderColumns.get(i).addAll(orderTerms.get(i));
                }
            }
            List<Column> columns = new ArrayList<>();
            orderColumns.forEach(columns::addAll);
            return columns;
        });
    }

    @NotNull
    private static List<List<Column>> createProducts(@NotNull Column[] powers, @NotNull List<List<Column>> orderColumns, int order) {
        List<List<Column>> newTerms = createOrderList(order);
        for (int i = 0; i < order; i++) {
            Column power = powers[i];
            newTerms.get(i).add(power);
            for (int j = 0; j < order && i + j + 2 <= order; j++) {
                List<Column> terms = newTerms.get(i + j + 1);
                orderColumns.get(j).forEach(column -> terms.add(Column.multiply(power.getId(), column.getId())));
            }
        }
        return newTerms;
    }

    @NotNull
    private static Column[] createPowers(@NotNull Variables variables, int index, int order) {
        Column[] powers = new Column[order];
        powers[0] = Column.variable(variables, index);
        for (int i = 1; i < order; i++) {
            powers[i] = Column.power(variables, index, i + 1);
        }
        return powers;
    }

    @NotNull
    private static Column[] createPowers(@NotNull Column term, int order) {
        Column[] powers = new Column[order];
        powers[0] = term;
        for (int i = 1; i < order; i++) {
            powers[i] = Column.power(term.getId(), i + 1);
        }
        return powers;
    }

    @NotNull
    private static List<List<Column>> createOrderList(int order) {
        List<List<Column>> list = new ArrayList<>();
        IntStream.range(0, order).forEach(i -> list.add(new ArrayList<>()));
        return list;
    }

    @NotNull
    public static ColumnGenerator signum(@NotNull ColumnGenerator terms) {
        return unaryFunction(terms, (variables, first) -> terms.getColumns().stream().map(Column::getId).map(Column::signum).collect(Collectors.toList()));
    }

    @NotNull
    public static ColumnGenerator absolute(@NotNull ColumnGenerator terms) {
        return unaryFunction(terms, (variables, first) -> terms.getColumns().stream().map(Column::getId).map(Column::absolute).collect(Collectors.toList()));
    }

    @NotNull
    public static ColumnGenerator sin(@NotNull double... coefficients) {
        return sin(variables(), coefficients);
    }

    @NotNull
    public static ColumnGenerator sin(@NotNull ColumnGenerator terms, @NotNull double... coefficients) {
        return unaryFunction(terms, (variables, first) -> {
            List<Column> columns = new ArrayList<>();
            for (Column term : terms.getColumns()) {
                for (double coefficient : coefficients) {
                    columns.add(Column.sin(coefficient, term.getId()));
                }
            }
            return columns;
        });
    }

    @NotNull
    public static ColumnGenerator cos(@NotNull double... coefficients) {
        return cos(variables(), coefficients);
    }

    @NotNull
    public static ColumnGenerator cos(@NotNull ColumnGenerator terms, @NotNull double... coefficients) {
        return unaryFunction(terms, (variables, first) -> {
            List<Column> columns = new ArrayList<>();
            for (Column term : terms.getColumns()) {
                for (double coefficient : coefficients) {
                    columns.add(Column.cos(coefficient, term.getId()));
                }
            }
            return columns;
        });
    }

    @NotNull
    public static ColumnGenerator variableFunction(@NotNull BiFunction<Variables, Integer, Column> generator) {
        return of(variables -> IntStream.range(0, variables.size()).mapToObj(index -> generator.apply(variables, index)).collect(Collectors.toList()));
    }

    @NotNull
    public static ColumnGenerator unaryFunction(@NotNull ColumnGenerator first, @NotNull UnaryGenerator generator) {
        return of(variables -> {
            first.generate(variables);
            return generator.generate(variables, first.getColumns());
        });
    }

    @NotNull
    public static ColumnGenerator binaryFunction(@NotNull ColumnGenerator first, @NotNull ColumnGenerator second, @NotNull BinaryGenerator generator) {
        return of(variables -> {
            first.generate(variables);
            second.generate(variables);
            return generator.generate(variables, first.getColumns(), second.getColumns());
        });
    }

    @NotNull
    public static ColumnGenerator of(@NotNull Generator generator) {
        return new ColumnGenerator(generator);
    }

    @FunctionalInterface
    public interface Generator {
        @NotNull List<Column> generate(@NotNull Variables variables);
    }

    @FunctionalInterface
    public interface UnaryGenerator {
        @NotNull List<Column> generate(@NotNull Variables variables, @NotNull List<Column> first);
    }

    @FunctionalInterface
    public interface BinaryGenerator {
        @NotNull List<Column> generate(@NotNull Variables variables, @NotNull List<Column> first, @NotNull List<Column> second);
    }
}
