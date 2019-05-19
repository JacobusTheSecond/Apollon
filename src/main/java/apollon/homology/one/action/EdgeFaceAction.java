package apollon.homology.one.action;

import apollon.homology.one.Circle;
import apollon.homology.one.HomologyOne;
import apollon.homology.one.Site;
import org.jetbrains.annotations.NotNull;

public class EdgeFaceAction extends Action {
    private final Circle circle;

    private final Site a;

    private final Site b;

    private final int edge;

    public EdgeFaceAction(@NotNull Site a, @NotNull Site b, int edge, @NotNull Circle circle, double radius) {
        super(radius);
        this.circle = circle;
        this.a = a;
        this.b = b;
        this.edge = edge;
    }

    @Override
    public void execute(@NotNull HomologyOne homology) {
        homology.addEdge(a, b, edge);
        homology.addRelation(circle, getRadius());
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
        return "EdgeFace: " + circle;
    }
}
