package apollon.homology.one.action;

import apollon.homology.one.Graph;
import apollon.homology.one.HomologyOne;
import apollon.homology.one.Site;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class EdgeAction extends Action {
    private final Site a;

    private final Site b;

    private final int edge;

    public EdgeAction(@NotNull Site a, @NotNull Site b, int edge, double radius) {
        super(radius);
        this.a = a;
        this.b = b;
        this.edge = edge;
    }

    @Override
    public void execute(@NotNull HomologyOne homology) {
        homology.addEdgeAndCycle(a, b, edge, getRadius());
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
        return "Edge: " + edge + " (" + a + " - " + b + ")";
    }
}
