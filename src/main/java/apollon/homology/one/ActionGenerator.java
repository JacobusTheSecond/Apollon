package apollon.homology.one;

import apollon.GeometryUtil;
import apollon.homology.one.action.Action;
import apollon.homology.one.action.EdgeAction;
import apollon.homology.one.action.EdgeFaceAction;
import apollon.homology.one.action.FaceAction;
import apollon.voronoi.VEdge;
import apollon.voronoi.Voronoi;
import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.*;

public class ActionGenerator {
    private final List<Action> actions = new ArrayList<>();

    private final Voronoi voronoi;

    private final Graph graph;

    private boolean[] ignoredEdges;

    public ActionGenerator(@NotNull Voronoi voronoi, @NotNull Graph graph) {
        this.voronoi = voronoi;
        this.graph = graph;
    }

    @NotNull
    public List<Action> generate() {
        if (voronoi.isEmpty()) {
            return Collections.emptyList();
        }
        actions.clear();
        ignoredEdges = new boolean[voronoi.getEdgesCount()];
        voronoi.forEachVertex(this::computeVertex);
        voronoi.forEachEdge(this::computeEdge);
        actions.sort(Action::compareTo);
        return actions;
    }

    private void computeVertex(@NotNull PointD vertex, int index) {
        int[] edgeIndices = voronoi.getVertexEdgeIndices(index);
        if (edgeIndices.length != 3) {
            return;
        }
        VEdge[] edges = voronoi.getEdges(edgeIndices);
        PointD[] sites = getSites(edges);
        if (GeometryUtil.isInside(vertex, sites)) {
            actions.add(new FaceAction(new Circle(edgeIndices), sites[0].subtract(vertex).length()));
            return;
        }
        VEdge edge = Arrays.stream(edges).max(Comparator.naturalOrder()).orElseThrow(RuntimeException::new);
        ignoredEdges[edge.getIndex()] = true;
        actions.add(new EdgeFaceAction(edge.getSiteAIndex(), edge.getSiteBIndex(), edge.getIndex(), new Circle(edgeIndices), edge.getLength() / 2));
    }

    private void computeEdge(@NotNull VEdge edge) {
        if (!ignoredEdges[edge.getIndex()]) {
            actions.add(new EdgeAction(edge.getSiteAIndex(), edge.getSiteBIndex(), edge.getIndex(), edge.getLength() / 2));
        }
    }

    @NotNull
    private PointD[] getSites(@NotNull VEdge[] edges) {
        Set<Integer> sites = new HashSet<>();
        for (VEdge edge : edges) {
            sites.add(edge.getSiteAIndex());
            sites.add(edge.getSiteBIndex());
        }
        return voronoi.getSites(GeometryUtil.toArray(sites));
    }
}
