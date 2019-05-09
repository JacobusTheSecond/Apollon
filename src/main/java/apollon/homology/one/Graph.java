package apollon.homology.one;

import apollon.GeometryUtil;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.Multigraph;
import org.kynosarges.tektosyne.geometry.PointD;

import java.awt.*;
import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class Graph {
    private final Multigraph<Integer, Integer> graph = new Multigraph<>(null, null, false);

    public void clear() {
        graph.removeAllVertices(new HashSet<>(graph.vertexSet()));
    }

    public void init(int size) {
        clear();
        IntStream.range(0, size).forEach(graph::addVertex);
    }

    public void addEdge(int a, int b, int edge) {
        graph.addEdge(a, b, edge);
    }

    @NotNull
    public Optional<Circle> find(int a, int b) {
        return Optional.ofNullable(new DijkstraShortestPath<>(graph).getPath(a, b)).map(GraphPath::getEdgeList).map(GeometryUtil::toArray).map(Circle::new);
    }

    @NotNull
    public int[] getSites() {
        return GeometryUtil.toArray(graph.vertexSet());
    }

    @NotNull
    public int[] getEdgeIndices() {
        int[] edges = GeometryUtil.toArray(graph.edgeSet());
        Arrays.sort(edges);
        return edges;
    }

    public int getSameSite(int a, int b) {
        int site = graph.getEdgeSource(a);
        if (site == graph.getEdgeSource(b) || site == graph.getEdgeTarget(b)) {
            return site;
        }
        return graph.getEdgeTarget(a);
    }

    public int getOtherSite(int edge, int site) {
        return Graphs.getOppositeVertex(graph, edge, site);
    }

    public void render(@NotNull Graphics g, @NotNull IntFunction<PointD> sitePoints) {
        g.setColor(Color.CYAN);
        graph.vertexSet().forEach(vertex -> GeometryUtil.draw("" + vertex, sitePoints.apply(vertex), g));

        g.setColor(Color.CYAN);
        Map<Set<Integer>, Set<Integer>> multiEdges = new HashMap<>();
        graph.edgeSet().forEach(edge -> multiEdges.computeIfAbsent(getSites(edge), k -> new HashSet<>()).add(edge));
        multiEdges.forEach((sites, edges) -> {
            Iterator<Integer> iterator = sites.iterator();
            int a = iterator.next();
            int b = iterator.next();
            GeometryUtil.draw(edges.toString(), sitePoints.apply(a), sitePoints.apply(b), g);
        });
    }

    @NotNull
    private Set<Integer> getSites(int edge) {
        return new HashSet<>(Arrays.asList(graph.getEdgeSource(edge), graph.getEdgeTarget(edge)));
    }

    @Override
    public String toString() {
        return graph.toString();
    }
}
