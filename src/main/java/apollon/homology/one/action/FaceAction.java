package apollon.homology.one.action;

import apollon.homology.one.Circle;
import apollon.homology.one.HomologyOne;
import org.jetbrains.annotations.NotNull;

public class FaceAction extends Action {
    private final Circle circle;

    public FaceAction(@NotNull Circle circle, double radius) {
        super(radius);
        this.circle = circle;
    }

    @Override
    public void execute(@NotNull HomologyOne homology) {
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
        return "Face: " + circle;
    }
}
