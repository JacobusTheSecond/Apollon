package apollon.homology.action;

import apollon.homology.Circuit;
import apollon.homology.Homology;
import apollon.homology.Site;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.OptionalInt;

public class EdgeFaceAction extends Action {
    private final Circuit circuit;

    private final Site source;

    private final Site target;

    private final int edge;

    public EdgeFaceAction(@NotNull Site a, @NotNull Site b, int edge, @NotNull Circuit circuit, double radius) {
        super(radius);
        this.circuit = circuit;
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
        homology.addEdge(source, target, edge, false, getRadius());
        if (!homology.isContractEdges() || source.equals(target)) {
            homology.addRelation(circuit, getRadius());
        }
    }

    @NotNull
    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public int getIndex() {
        return 2;
    }

    @NotNull
    @Override
    public OptionalInt getAddedEdge() {
        return OptionalInt.of(edge);
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
        return "EdgeFace: " + edge + " (" + source + " - " + target + ") " + circuit;
    }
}
