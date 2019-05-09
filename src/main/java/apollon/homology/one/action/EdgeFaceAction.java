package apollon.homology.one.action;

import apollon.homology.one.Circle;
import apollon.homology.one.HomologyOne;
import org.jetbrains.annotations.NotNull;

public class EdgeFaceAction extends Action {
    private final Circle circle;

    private final int a;

    private final int b;

    private final int edge;

    public EdgeFaceAction(int a, int b, int edge, @NotNull Circle circle, double radius) {
        super(radius);
        this.circle = circle;
        this.a = a;
        this.b = b;
        this.edge = edge;
    }

    @Override
    public void execute(@NotNull HomologyOne homology) {
        homology.addEdge(a, b, edge, getRadius());
        homology.addRelation(circle, getRadius());
    }

    @Override
    public String toString() {
        return "EdgeFace: " + circle;
    }
}
