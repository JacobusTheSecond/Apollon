package apollon.homology.one;

import apollon.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Cycle implements EdgeContainer, Comparable<Cycle> {
    private final Circle circle;

    private final double born;

    private double died = Double.POSITIVE_INFINITY;

    public Cycle(@NotNull Circle circle, double born) {
        this.circle = circle;
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
    public Circle getCircle() {
        return circle;
    }

    @Override
    public void remove(@NotNull int[] edges) {
        circle.remove(edges);
    }

    @Override
    public void replace(int edge, @NotNull int[] edges) {
        circle.replace(edge, edges);
    }

    public void killIfEmpty(double radius) {
        if (circle.isEmpty()) {
            kill(radius);
        }
    }

    public boolean containsAny(@NotNull Set<Integer> edges) {
        return circle.containsAny(edges);
    }

    @Override
    public int compareTo(@NotNull Cycle o) {
        return Double.compare(born, o.born);
    }

    @Override
    public String toString() {
        if (isAlive()) {
            return "Alive: (" + Util.display(born) + ") " + circle;
        }
        return "Dead: (" + Util.display(born) + " - " + Util.display(died) + ") " + Util.display(died - born);
    }
}
