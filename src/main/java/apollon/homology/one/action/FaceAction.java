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
    public String toString() {
        return "Face: " + circle;
    }
}
