package apollon.dynamics.data.theta;

import apollon.dynamics.data.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ThetaConfiguration {
    private final List<ColumnGenerator> generators = new ArrayList<>();

    private Columns columns;

    @NotNull
    public ThetaConfiguration add(@NotNull ColumnGenerator generator) {
        generators.add(generator);
        return this;
    }

    public void generate(@NotNull Data data) {
        columns = ColumnGenerator.linear().generate(data);
        columns.generate();
        List<Columns> columns = new ArrayList<>();
        generators.forEach(generator -> columns.add(generator.generate(data)));
        columns.forEach(Columns::generate);
        columns.forEach(this.columns::addAll);
    }

    @NotNull
    public double[][] createTheta() {
        return columns.createTheta();
    }

    @NotNull
    public String[] createNames() {
        return columns.createNames();
    }
}
