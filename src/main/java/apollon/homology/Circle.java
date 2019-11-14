package apollon.homology;

import apollon.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

public class Circle implements EdgeContainer, Iterable<Integer> {
    private final List<Integer> edges = new ArrayList<>();

    public Circle() {}

    public Circle(@NotNull int... edges) {
        IntStream.of(edges).forEach(this::append);
    }

    @NotNull
    public Circle append(int edge) {
        edges.add(edge);
        return this;
    }

    @NotNull
    public int[] getEdges() {
        return edges.stream().mapToInt(Integer::intValue).toArray();
    }

    @NotNull
    public int[] getEdgeIndices() {
        return edges.stream().mapToInt(Integer::intValue).map(Graph::positive).toArray();
    }

    public int get(int index) {
        return edges.get(index);
    }

    @Override
    public void replace(int edge, @NotNull int... edges) {
        if (edges.length == 0) {
            remove(edges);
            return;
        }
        boolean modified = false;
        int inverse = Graph.inverse(edge);
        List<Integer> positive = Util.toList(edges);
        List<Integer> negative = Util.toList(Graph.inverse(edges));
        for (int i = 0; i < size(); i++) {
            int replace = get(i);
            if (replace == edge || replace == inverse) {
                this.edges.remove(i);
                this.edges.addAll(i, replace == edge ? positive : negative);
                modified = true;
                i += edges.length - 1;
            }
        }
        if (modified) {
            revalidate();
        }
    }

    @Override
    public void remove(@NotNull int... edges) {
        Set<Integer> set = Util.toSet(edges);
        IntStream.of(edges).map(Graph::inverse).forEach(set::add);
        if (this.edges.removeAll(set)) {
            revalidate();
        }
    }

    public void revalidate() {
        for (int i = 0; i < size(); i++) {
            int j = (i + 1) % size();
            if (get(i) == Graph.inverse(get(j))) {
                edges.remove(Math.max(i, j));
                edges.remove(Math.min(i, j));
                i = Math.max(0, i - (j < i ? 3 : 2));
            }
        }
    }

    public int size() {
        return edges.size();
    }

    public boolean isEmpty() {
        return edges.isEmpty();
    }

    @NotNull
    @Override
    public Iterator<Integer> iterator() {
        return edges.iterator();
    }

    @NotNull
    public IntStream stream() {
        return edges.stream().mapToInt(Integer::intValue);
    }

    public boolean containsAny(@NotNull Set<Integer> edges) {
        return stream().map(Graph::positive).distinct().anyMatch(edges::contains);
    }

    @Override
    public String toString() {
        return Arrays.toString(stream().mapToObj(Graph::toString).toArray(String[]::new));
    }

    @NotNull
    public int[] getSingleEdges() {
        return stream().filter(edge -> count(edge) == 1).toArray();
    }

    public int getSingleEdge() {
        int[] edges = getSingleEdges();
        if (edges.length > 0) {
            return edges[0];
        }
        throw new IllegalStateException("Cannot create relation: " + this);
    }

    private int count(int edge) {
        return (int) stream().filter(e -> Graph.equals(e, edge)).count();
    }

    @NotNull
    public int[] getInverse(int edge) {
        int[] edges = new int[size() - 1];
        int index = this.edges.indexOf(edge);
        boolean inverse = index == -1;
        if (inverse) {
            index = this.edges.indexOf(Graph.inverse(edge));
        }
        int[] lower = Util.toArray(this.edges.subList(0, index));
        int[] upper = Util.toArray(this.edges.subList(index + 1, size()));
        if (inverse) {
            if (upper.length > 0) {
                System.arraycopy(upper, 0, edges, 0, upper.length);
            }
            if (lower.length > 0) {
                System.arraycopy(lower, 0, edges, upper.length, lower.length);
            }
        }
        else {
            if (lower.length > 0) {
                System.arraycopy(Graph.inverse(lower), 0, edges, 0, lower.length);
            }
            if (upper.length > 0) {
                System.arraycopy(Graph.inverse(upper), 0, edges, lower.length, upper.length);
            }
        }
        return edges;
    }
}
