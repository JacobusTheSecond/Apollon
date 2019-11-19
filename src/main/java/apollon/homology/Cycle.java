package apollon.homology;

import apollon.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Cycle implements EdgeContainer, Comparable<Cycle> {
    private final Circuit circuit;

    private final double born;

    private double died = Double.POSITIVE_INFINITY;

    public Cycle(@NotNull Circuit circuit, double born) {
        this.circuit = circuit;
        this.born = born;
    }

    public double getBorn() {
        return born;
    }

    public double getDied() {
        return died;
    }

    public boolean wasLiving() {
        return born != died;
    }

    public boolean isAlive() {
        return died == Double.POSITIVE_INFINITY;
    }

    public void kill(double radius) {
        died = radius;
    }

    @NotNull
    public Circuit getCircuit() {
        return circuit;
    }

    @Override
    public void remove(@NotNull int[] edges) {
        circuit.remove(edges);
    }

    @Override
    public void replace(int edge, @NotNull int[] edges) {
        circuit.replace(edge, edges);
    }

    public void killIfEmpty(double radius) {
        if (circuit.isEmpty()) {
            kill(radius);
        }
    }

    public boolean containsAny(@NotNull Set<Integer> edges) {
        return circuit.containsAny(edges);
    }

    @Override
    public int compareTo(@NotNull Cycle o) {
        return Double.compare(born, o.born);
    }

    @Override
    public String toString() {
        if (isAlive()) {
            return "Alive: (" + Util.display(born) + ") " + circuit;
        }
        return "Dead: (" + Util.display(born) + " - " + Util.display(died) + ") " + Util.display(died - born);
    }
}
