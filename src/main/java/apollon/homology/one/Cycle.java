package apollon.homology.one;

import org.jetbrains.annotations.NotNull;

public class Cycle implements Comparable<Cycle> {
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
    public int compareTo(@NotNull Cycle o) {
        return Double.compare(born, o.born);
    }

    @Override
    public String toString() {
        return (isAlive() ? "Alive: " : "Dead: ") + circle.toString();
    }
}
