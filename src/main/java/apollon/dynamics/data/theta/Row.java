package apollon.dynamics.data.theta;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Row {
    private final Map<String, double[]> columns = new HashMap<>();

    private final double[][] data;

    public Row(@NotNull double[][] data) {
        this.data = data;
    }

    public void set(@NotNull String id, @NotNull double[] value) {
        columns.put(id, value);
    }

    public int size() {
        return data[0].length;
    }

    public double[] get(int index) {
        return data[index];
    }

    public double[] get(@NotNull String id) {
        return Optional.ofNullable(columns.get(id)).orElseThrow(() -> new IllegalStateException("Column not computed yet: " + id));
    }

    @NotNull
    public double[][] generate(@NotNull List<Column> columns) {
        return columns.stream().map(Column::getId).map(this::get).toArray(double[][]::new);
    }
}
