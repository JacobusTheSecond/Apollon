package apollon.dynamics.data;

import apollon.dynamics.data.theta.Variables;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class EquationGenerator {
    private final double[][] xi;

    private final Variables variables;

    private final String[] names;

    public EquationGenerator(@NotNull double[][] xi, @NotNull Variables variables, @NotNull String[] names) {
        this.xi = xi;
        this.variables = variables;
        this.names = names;
    }

    @NotNull
    public String[] createEquations() {
        String[] equations = new String[xi.length];
        for (int i = 0; i < xi.length; i++) {
            equations[i] = createEquation(i, xi[i]);
        }
        return equations;
    }

    @NotNull
    private String createEquation(int index, @NotNull double[] xi) {
        StringBuilder equation = new StringBuilder("d").append(variables.get(index)).append(" = ");
        AtomicBoolean empty = new AtomicBoolean(true);
        for (int i = 0; i < xi.length; i++) {
            double value = xi[i];
            if (value != 0) {
                if (!empty.get()) {
                    equation.append(" + ");
                }
                createTerm(value, names[i]).ifPresent(term -> {
                    equation.append(term);
                    empty.set(false);
                });
            }
        }
        if (empty.get()) {
            equation.append("0");
        }
        return equation.toString();
    }

    @NotNull
    private Optional<String> createTerm(double value, @NotNull String name) {
        if (value == 1) {
            return Optional.of(name);
        }
        if (value == -1) {
            return Optional.of("-" + name);
        }
        if (value == 0) {
            return Optional.empty();
        }
        return Optional.of(value + " * " + name);
    }
}
