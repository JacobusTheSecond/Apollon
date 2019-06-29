package apollon.homology.action;

import apollon.homology.Graph;
import apollon.homology.Homology;
import apollon.homology.Site;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.OptionalInt;

public class EdgeAction extends Action {
    private final Site source;

    private final Site target;

    private final int edge;

    public EdgeAction(@NotNull Site a, @NotNull Site b, int edge, double radius) {
        super(radius);
        if (a.index() > b.index()) {
            Site temp = b;
            b = a;
            a = temp;
        }
        source = a;
        target = b;
        this.edge = edge;
    }

    @Override
    public void execute(@NotNull Homology homology) {
        homology.addEdgeAndCycle(source, target, edge, getRadius());
    }

    @NotNull
    @Override
    public Color getColor() {
        return Color.CYAN;
    }

    @Override
    protected int getIndex() {
        return 0;
    }

    @NotNull
    @Override
    public OptionalInt getAddedEdge() {
        return OptionalInt.of(edge);
    }

    @Override
    public void remove(@NotNull int... edges) {
        if (ArrayUtils.contains(edges, edge)) {
            throw new IllegalStateException("Cannot remove edge before adding it: " + edge);
        }
    }

    @Override
    public void replace(int edge, @NotNull int... edges) {
        if (Graph.equals(this.edge, edge)) {
            throw new IllegalStateException("Cannot replace edge before adding it: " + this.edge);
        }
    }

    @Override
    public String toString() {
        return "Edge: " + edge + " (" + source + " - " + target + ")";
    }
}
