package apollon.homology.one.action;

import apollon.homology.one.Circle;
import apollon.homology.one.HomologyOne;
import apollon.homology.one.Site;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class EdgeFaceAction extends Action {
    private final Circle circle;

    private final Site source;

    private final Site target;

    private final int edge;

    public EdgeFaceAction(@NotNull Site a, @NotNull Site b, int edge, @NotNull Circle circle, double radius) {
        super(radius);
        this.circle = circle;
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
    public void execute(@NotNull HomologyOne homology) {
        homology.addEdge(source, target, edge);
        homology.addRelation(circle, getRadius());
    }

    @NotNull
    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    protected int getIndex() {
        return 2;
    }

    @Override
    public void remove(@NotNull int... edges) {
        circle.remove(edges);
    }

    @Override
    public void replace(int edge, @NotNull int... edges) {
        circle.replace(edge, edges);
    }

    @Override
    public String toString() {
        return "EdgeFace: " + edge + " (" + source + " - " + target + ") " + circle;
    }
}
