package apollon.homology.one.action;

import apollon.homology.one.HomologyOne;
import org.jetbrains.annotations.NotNull;

public abstract class Action implements Comparable<Action> {
    private final double radius;

    protected Action(double radius) {
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public int compareTo(@NotNull Action o) {
        return Double.compare(radius, o.radius);
    }

    public abstract void execute(@NotNull HomologyOne homology);
}
