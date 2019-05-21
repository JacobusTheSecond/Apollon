package apollon.homology.one;

import apollon.util.Util;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.Pseudograph;
import org.kynosarges.tektosyne.geometry.PointD;

import java.awt.*;
import java.util.List;
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

    public void addEdge(@NotNull Site source, @NotNull Site target, int edge) {
        graph.addEdge(source, target, edge);
    }

    @NotNull
    public Optional<Circle> find(@NotNull Site source, @NotNull Site target) {
        GraphPath<Site, Integer> path = new DijkstraShortestPath<>(graph).getPath(source, target);
        if (path == null) {
            return Optional.empty();
        }
        Circle circle = new Circle();
        Site current = path.getStartVertex();
        Site next;
        for (int edge : path.getEdgeList()) {
            next = getOtherSite(edge, current);
            circle.append(graph.getEdgeSource(edge).equals(current) ? edge : Graph.inverse(edge));
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
        int[] edges = Util.toArray(graph.edgeSet());
        Arrays.sort(edges);
        return edges;
    }

    @NotNull
    private Site getOtherSite(int edge, @NotNull Site site) {
        return Graphs.getOppositeVertex(graph, edge, site);
    }

    public void render(@NotNull IntFunction<PointD> sitePoints, @NotNull List<Cycle> cycles, @NotNull Graphics g) {
        renderSites(sitePoints, g);
        renderEdges(sitePoints, cycles, g);
    }

    private void renderSites(@NotNull IntFunction<PointD> sitePoints, @NotNull Graphics g) {
        g.setColor(Color.BLACK);
        graph.vertexSet().forEach(vertex -> Util.draw(vertex.toString(), sitePoints.apply(vertex.getIndex()), g));
    }

    private void renderEdges(@NotNull IntFunction<PointD> sitePoints, @NotNull List<Cycle> cycles, @NotNull Graphics g) {
        Map<Set<Site>, Set<Integer>> multiEdges = new HashMap<>();
        graph.edgeSet().forEach(edge -> multiEdges.computeIfAbsent(getSites(edge), k -> new HashSet<>()).add(edge));
        multiEdges.forEach((sites, edges) -> renderEdge(sites, edges, sitePoints, cycles, g));
    }

    private void renderEdge(@NotNull Set<Site> sites, @NotNull Set<Integer> edges, @NotNull IntFunction<PointD> sitePoints, @NotNull List<Cycle> cycles, @NotNull Graphics g) {
        g.setColor(cycles.stream().anyMatch(cycle -> cycle.containsAny(edges)) ? Color.GREEN : Color.BLUE);
        if (sites.size() == 1) {
            renderLoop(toString(edges), sitePoints.apply(sites.iterator().next().getIndex()), g);
            return;
        }
        Site source = graph.getEdgeSource(edges.iterator().next());
        Site target = sites.stream().filter(site -> !site.equals(source)).findFirst().orElseThrow(RuntimeException::new);
        PointD a = sitePoints.apply(source.getIndex());
        PointD b = sitePoints.apply(target.getIndex());
        if (edges.stream().map(graph::getEdgeSource).noneMatch(site -> site.equals(target))) {
            Util.drawArrow(toString(edges), a, b, g);
            return;
        }
        int[] directed = edges.stream().mapToInt(Integer::intValue).sorted().map(edge -> graph.getEdgeSource(edge).equals(source) ? edge : -edge).toArray();
        Util.drawArrow(Arrays.toString(directed), a, b, g);
    }

    @NotNull
    private String toString(@NotNull Collection<Integer> edges) {
        return Arrays.toString(edges.stream().mapToInt(Integer::intValue).sorted().toArray());
    }

    private void renderLoop(@NotNull String name, @NotNull PointD point, @NotNull Graphics g) {
        Util.drawCircle(name, point.add(new PointD(Util.DIAMETER, 0)), Util.DIAMETER, g);
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
        Set<Integer> positiveEdges = new HashSet<>();
        sites.forEach(site -> graph.edgesOf(site).forEach(edge -> {
            oldEdges.put(edge, Graphs.getOppositeVertex(graph, edge, site));
            if (graph.getEdgeSource(edge).equals(site)) {
                positiveEdges.add(edge);
            }
        }));
        sites.forEach(graph::removeVertex);
        sites.forEach(site -> site.set(elderly));
        graph.addVertex(elderly);
        oldEdges.forEach((edge, site) -> {
            if (positiveEdges.contains(edge)) {
                graph.addEdge(elderly, site, edge);
            }
            else {
                graph.addEdge(site, elderly, edge);
            }
        });
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

    @NotNull
    public static String toString(int edge) {
        return edge >= 0 ? "" + edge : "-" + positive(edge);
    }
}
