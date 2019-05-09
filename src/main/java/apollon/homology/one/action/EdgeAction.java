package apollon.homology.one.action;

import apollon.homology.one.HomologyOne;
import org.jetbrains.annotations.NotNull;

public class EdgeAction extends Action {
    private final int a;

    private final int b;

    private final int edge;

    public EdgeAction(int a, int b, int edge, double radius) {
        super(radius);
        this.a = a;
        this.b = b;
        this.edge = edge;
    }

    @Override
    public void execute(@NotNull HomologyOne homology) {
        homology.addEdge(a, b, edge, getRadius());
    }

    @Override
    public String toString() {
        return "Edge: " + edge + " (" + a + " - " + b + ")";
    }
}
