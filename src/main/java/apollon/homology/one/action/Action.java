package apollon.homology.one.action;

import apollon.homology.one.EdgeContainer;
import apollon.homology.one.HomologyOne;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public abstract class Action implements EdgeContainer, Comparable<Action> {
    private final double radius;

    protected Action(double radius) {
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public int compareTo(@NotNull Action o) {
        int difference = Double.compare(radius, o.radius);
        if (difference != 0) {
            return difference;
        }
        return Integer.compare(getIndex(), o.getIndex());
    }

    public abstract void execute(@NotNull HomologyOne homology);

    @NotNull
    public abstract Color getColor();

    protected abstract int getIndex();
}
