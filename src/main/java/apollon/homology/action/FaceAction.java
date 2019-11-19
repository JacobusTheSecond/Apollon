package apollon.homology.action;

import apollon.homology.Circuit;
import apollon.homology.Homology;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class FaceAction extends Action {
    private final Circuit circuit;

    public FaceAction(@NotNull Circuit circuit, double radius) {
        super(radius);
        this.circuit = circuit;
    }

    @Override
    public void execute(@NotNull Homology homology) {
        homology.addRelation(circuit, getRadius());
    }

    @NotNull
    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public int getIndex() {
        return 1;
    }

    @NotNull
    @Override
    public int[] getRemovedEdges() {
        return circuit.getEdgeIndices();
    }

    @Override
    public void remove(@NotNull int... edges) {
        circuit.remove(edges);
    }

    @Override
    public void replace(int edge, @NotNull int... edges) {
        circuit.replace(edge, edges);
    }

    @Override
    public String toString() {
        return "Face: " + circuit;
    }
}
