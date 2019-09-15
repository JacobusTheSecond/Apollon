package apollon.dynamics.data.theta;

import apollon.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ThetaConfig {
    private final List<Column> columns = new ArrayList<>();

    public void add(@NotNull Collection<Column> columns) {
        columns.forEach(this::add);
    }

    public void add(@NotNull Column column) {
        columns.add(column);
    }

    @NotNull
    public double[] generate(@NotNull double[] data) {
        return Util.swapDimension(generate(Util.swapDimension(new double[][]{data})))[0];
    }

    @NotNull
    public double[][] generate(@NotNull double[][] data) {
        Row row = new Row(data);
        columns.forEach(column -> row.set(column.getId(), column.compute(row)));
        return row.generate(columns);
    }

    @NotNull
    public String[] getNames() {
        return columns.stream().map(Column::getId).toArray(String[]::new);
    }

    @NotNull
    public static Builder builder(@NotNull Variables variables) {
        return new Builder(variables);
    }

    public static class Builder {
        private final List<ColumnGenerator> generators = new ArrayList<>();

        private final Variables variables;

        public Builder(@NotNull Variables variables) {
            this.variables = variables;
        }

        @NotNull
        public Builder append(@NotNull ColumnGenerator generator) {
            generators.add(generator);
            return this;
        }

        @NotNull
        public ThetaConfig build() {
            ThetaConfig config = new ThetaConfig();
            generators.forEach(ColumnGenerator::clear);
            generators.forEach(generator -> generator.generate(variables));
            generators.stream().map(ColumnGenerator::getColumns).forEach(config::add);
            return config;
        }
    }
}
