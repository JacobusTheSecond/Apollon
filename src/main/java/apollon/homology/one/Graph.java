package apollon.homology.one;

import apollon.util.GeometryUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.Pseudograph;
import org.kynosarges.tektosyne.geometry.PointD;

import java.awt.*;
import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class Graph {
    private final Pseudograph<Site, Integer> graph = new Pseudograph<>(null, null, false);

    public void clear() {
        graph.removeAllVertices(new HashSet<>(graph.vertexSet()));
    }

    public void init(int size) {
        clear();
        IntStream.range(0, size).mapToObj(Site::new).forEach(graph::addVertex);
    }

    public void addEdge(@NotNull Site a, @NotNull Site b, int edge) {
        graph.addEdge(a, b, edge);
    }

    @NotNull
    public Optional<Circle> find(@NotNull Site a, @NotNull Site b) {
        GraphPath<Site, Integer> path = new DijkstraShortestPath<>(graph).getPath(a, b);
        if (path == null) {
            return Optional.empty();
        }
        Circle circle = new Circle();
        Site current = path.getStartVertex();
        Site next;
        for (int edge : path.getEdgeList()) {
            next = getOtherSite(edge, current);
            circle.append(current.index() <= next.index() ? edge : Graph.inverse(edge));
            current = next;
        }
        return Optional.of(circle);
    }

    @NotNull
    public Set<Site> getSites() {
        return graph.vertexSet();
    }

    @NotNull
    public Site getSite(int index) {
        return getSites().stream().filter(site -> site.getIndex() == index).findFirst().orElseThrow(RuntimeException::new);
    }

    @NotNull
    public int[] getEdgeIndices() {
        int[] edges = GeometryUtil.toArray(graph.edgeSet());
        Arrays.sort(edges);
        return edges;
    }

    @NotNull
    public Site getOtherSite(int edge, @NotNull Site site) {
        return Graphs.getOppositeVertex(graph, edge, site);
    }

    public void render(@NotNull Graphics g, @NotNull IntFunction<PointD> sitePoints) {
        g.setColor(Color.CYAN);
        graph.vertexSet().forEach(vertex -> GeometryUtil.draw("" + vertex, sitePoints.apply(vertex.getIndex()), g));

        g.setColor(Color.CYAN);
        Map<Set<Site>, Set<Integer>> multiEdges = new HashMap<>();
        graph.edgeSet().forEach(edge -> multiEdges.computeIfAbsent(getSites(edge), k -> new HashSet<>()).add(edge));
        multiEdges.forEach((sites, edges) -> {
            if (sites.size() == 1) {
                drawLoop(edges.toString(), sitePoints.apply(sites.iterator().next().getIndex()), g);
                return;
            }
            Iterator<Site> iterator = sites.iterator();
            Site a = iterator.next();
            Site b = iterator.next();
            GeometryUtil.draw(edges.toString(), sitePoints.apply(a.getIndex()), sitePoints.apply(b.getIndex()), g);
        });
    }

    private void drawLoop(@NotNull String name, @NotNull PointD point, @NotNull Graphics g) {
        GeometryUtil.drawCircle(name, point.add(new PointD(GeometryUtil.RADIUS, -GeometryUtil.RADIUS)), 10, g);
    }

    @NotNull
    private Set<Site> getSites(@NotNull int... edges) {
        return IntStream.of(edges).mapToObj(this::getSites).reduce(new HashSet<>(), (a, b) -> {
            a.addAll(b);
            return a;
        });
    }

    @NotNull
    private Set<Site> getSites(int edge) {
        edge = positive(edge);
        return new HashSet<>(Arrays.asList(graph.getEdgeSource(edge), graph.getEdgeTarget(edge)));
    }

    @Override
    public String toString() {
        return graph.toString();
    }

    public boolean hasNoLoops(@NotNull Circle circle) {
        return circle.stream().noneMatch(this::isLoop);
    }

    private boolean isNonLoop(int edge) {
        return !isLoop(edge);
    }

    private boolean isLoop(int edge) {
        edge = positive(edge);
        return graph.getEdgeSource(edge).equals(graph.getEdgeTarget(edge));
    }

    public boolean hasOnlyLoops(@NotNull Circle circle) {
        return circle.stream().noneMatch(this::isNonLoop);
    }

    public void remove(@NotNull int... edges) {
        Set<Site> sites = getSites(edges);
        Site elderly = Collections.min(sites);
        IntStream.of(edges).map(Graph::positive).forEach(graph::removeEdge);
        Map<Integer, Site> oldEdges = new HashMap<>();
        sites.forEach(site -> graph.edgesOf(site).forEach(edge -> oldEdges.put(edge, Graphs.getOppositeVertex(graph, edge, site))));
        sites.forEach(graph::removeVertex);
        sites.forEach(site -> site.set(elderly));
        graph.addVertex(elderly);
        oldEdges.forEach((edge, target) -> graph.addEdge(elderly, target, edge));
    }

    public int getSingleNonLoop(@NotNull Circle circle) {
        for (int edge : circle.getSingleEdges()) {
            if (isNonLoop(edge)) {
                return edge;
            }
        }
        throw new RuntimeException("Cannot find single non-loop: " + circle);
    }

    @NotNull
    public int[] getNonLoops(@NotNull Circle circle) {
        return circle.stream().filter(this::isNonLoop).map(Graph::positive).toArray();
    }

    public static boolean equals(int a, int b) {
        return a == b || a == inverse(b);
    }

    @NotNull
    public static int[] inverse(@NotNull int... edges) {
        int[] inverse = IntStream.of(edges).map(Graph::inverse).toArray();
        ArrayUtils.reverse(inverse);
        return inverse;
    }

    public static int inverse(int edge) {
        return -edge - 1;
    }

    public static int positive(int edge) {
        return edge >= 0 ? edge : inverse(edge);
    }
}
