package apollon.voronoi;

import apollon.util.Util;
import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.RectD;
import org.kynosarges.tektosyne.geometry.VoronoiResults;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

public class Voronoi {
    private PointD[] sites = null;

    private PointD[] vertices = null;

    private VEdge[] edges = null;

    private int[][] vertexEdges = null;

    private int sitesCount;

    private int edgesCount;

    public void compute(@NotNull Collection<PointD> sites, int x, int y, int width, int height) {
        if (sites.size() < 2) {
            clear();
            return;
        }
        this.sites = sites.toArray(new PointD[0]);
        sitesCount = this.sites.length;
        compute(x, y, width, height);
    }

    public int getSitesCount() {
        return sitesCount;
    }

    public int getEdgesCount() {
        return edgesCount;
    }

    private void clear() {
        sites = null;
        vertices = null;
        edges = null;
        sitesCount = 0;
    }

    private void compute(int x, int y, int width, int height) {
        VoronoiResults results = org.kynosarges.tektosyne.geometry.Voronoi.findAll(sites, new RectD(x, y, width, height));
        vertices = results.voronoiVertices;
        edges = Arrays.stream(results.voronoiEdges).map(edge -> new VEdge(this, edge)).sorted().toArray(VEdge[]::new);
        edgesCount = edges.length;
        IntStream.range(0, edgesCount).forEach(i -> edges[i].setIndex(i));
        vertexEdges = computeEdges(vertices.length, VEdge::getVertexAIndex, VEdge::getVertexBIndex);
    }

    @NotNull
    private int[][] computeEdges(int size, @NotNull ToIntFunction<VEdge> a, @NotNull ToIntFunction<VEdge> b) {
        Set[] edges = new Set[size];
        forEachEdge(edge -> {
            for (int index : new int[]{a.applyAsInt(edge), b.applyAsInt(edge)}) {
                //noinspection unchecked
                Set<Integer> indices = (Set<Integer>) edges[index];
                if (indices == null) {
                    indices = new HashSet<>();
                    edges[index] = indices;
                }
                indices.add(edge.getIndex());
            }
        });
        int[][] result = new int[size][];
        for (int i = 0; i < size; i++) {
            //noinspection unchecked
            Set<Integer> list = (Set<Integer>) edges[i];
            result[i] = list != null ? Util.toArray(list) : new int[0];
        }
        return result;
    }

    public void forEachVertex(@NotNull ObjIntConsumer<PointD> operation) {
        IntStream.range(0, vertices.length).forEach(i -> operation.accept(vertices[i], i));
    }

    public void forEachEdge(@NotNull Consumer<VEdge> operation) {
        forEachEdge((edge, i) -> operation.accept(edge));
    }

    public void forEachEdge(@NotNull ObjIntConsumer<VEdge> operation) {
        IntStream.range(0, edgesCount).forEach(i -> operation.accept(edges[i], i));
    }

    @NotNull
    public int[] getVertexEdgeIndices(int vertex) {
        return vertexEdges[vertex];
    }

    @NotNull
    public PointD getVertex(int vertex) {
        return vertices[vertex];
    }

    @NotNull
    public VEdge getEdge(int edge) {
        return edges[edge];
    }

    @NotNull
    public PointD getSite(int site) {
        return sites[site];
    }

    @NotNull
    public PointD[] getSites(int[] sites) {
        return IntStream.of(sites).mapToObj(this::getSite).toArray(PointD[]::new);
    }

    @NotNull
    public VEdge[] getEdges(@NotNull int[] edges) {
        return IntStream.of(edges).mapToObj(this::getEdge).toArray(VEdge[]::new);
    }

    public boolean isEmpty() {
        return vertices == null;
    }
}
