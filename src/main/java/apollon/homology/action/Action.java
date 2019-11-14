package apollon.homology.action;

import apollon.homology.EdgeContainer;
import apollon.homology.Homology;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.OptionalInt;

public abstract class Action implements EdgeContainer, Comparable<Action> {
    private double radius;

    protected Action(double radius) {
        this.radius = radius;
    }

    public void setRadius(double radius) {
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

    public abstract void execute(@NotNull Homology homology);

    @NotNull
    public OptionalInt getAddedEdge() {
        return OptionalInt.empty();
    }

    @NotNull
    public int[] getRemovedEdges() {
        return new int[0];
    }

    @NotNull
    public abstract Color getColor();

    public abstract int getIndex();
}
