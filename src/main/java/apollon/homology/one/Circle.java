package apollon.homology.one;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Circle {
    private final int[] edges;

    public Circle(@NotNull int... edges) {
        this.edges = edges;
    }

    public Circle(int edge, @NotNull int... edges) {
        this.edges = Arrays.copyOf(edges, edges.length + 1);
        this.edges[edges.length] = edge;
    }

    @NotNull
    public int[] getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return Arrays.toString(edges);
    }
}
