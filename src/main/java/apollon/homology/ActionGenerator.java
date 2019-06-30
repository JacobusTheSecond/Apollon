package apollon.homology;

import apollon.homology.action.Action;
import apollon.homology.action.EdgeAction;
import apollon.homology.action.EdgeFaceAction;
import apollon.homology.action.FaceAction;
import apollon.util.Util;
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
        repair();
        return actions;
    }

    private void computeVertex(@NotNull PointD vertex, int index) {
        int[] edgeIndices = voronoi.getVertexEdgeIndices(index);
        if (edgeIndices.length != 3) {
            return;
        }
        VEdge[] edges = voronoi.getEdges(edgeIndices);
        PointD[] sites = getSites(edges);
        if (Util.isInside(vertex, sites)) {
            actions.add(new FaceAction(createCircle(edges), sites[0].subtract(vertex).length()));
            return;
        }
        VEdge edge = Arrays.stream(edges).max(Comparator.naturalOrder()).orElseThrow(RuntimeException::new);
        ignoredEdges[edge.getIndex()] = true;
        actions.add(new EdgeFaceAction(getSite(edge.getSiteAIndex()), getSite(edge.getSiteBIndex()), edge.getIndex(), createCircle(edges), edge.getLength() / 2));
    }

    private void computeEdge(@NotNull VEdge edge) {
        if (!ignoredEdges[edge.getIndex()]) {
            actions.add(new EdgeAction(getSite(edge.getSiteAIndex()), getSite(edge.getSiteBIndex()), edge.getIndex(), edge.getLength() / 2));
        }
    }

    @NotNull
    private Circle createCircle(@NotNull VEdge[] edges) {
        Circle circle = new Circle();
        int current = edges[0].getOtherSite(edges[1]);
        int next;
        for (VEdge edge : edges) {
            next = edge.getOtherSite(current);
            circle.append(current <= next ? edge.getIndex() : Graph.inverse(edge.getIndex()));
            current = next;
        }
        return circle;
    }

    @NotNull
    private Site getSite(int site) {
        return graph.getSite(site);
    }

    @NotNull
    private PointD[] getSites(@NotNull VEdge[] edges) {
        Set<Integer> sites = new HashSet<>();
        for (VEdge edge : edges) {
            sites.add(edge.getSiteAIndex());
            sites.add(edge.getSiteBIndex());
        }
        return voronoi.getSites(Util.toArray(sites));
    }

    private void repair() {
        try {
            tryRepair();
        }
        catch (Exception e) {
            reorder();
            tryRepair();
        }
    }

    private void tryRepair() {
        int[] edgeIndices = new int[voronoi.getEdgesCount()];
        Arrays.fill(edgeIndices, -1);
        for (int i = 0; i < actions.size(); i++) {
            int index = i;
            actions.get(i).getAddedEdge().ifPresent(edge -> edgeIndices[edge] = index);
        }
        for (int i = 0; i < actions.size(); i++) {
            int index = i;
            Arrays.stream(actions.get(i).getRemovedEdges()).map(edge -> edgeIndices[edge]).max().ifPresent(edgeIndex -> {
                if (index < edgeIndex) {
                    move(index, edgeIndex, edgeIndices);
                }
            });
        }
    }

    private void move(int source, int target, @NotNull int[] edgeIndices) {
        if (target > source + 1) {
            throw new IllegalStateException("BIG MOVE: " + source + " -> " + target);
        }
        int edge = actions.get(target).getAddedEdge().orElseThrow();
        int faceEdge = actions.get(source).getAddedEdge().orElse(-1);
        actions.add(target, actions.remove(source));
        edgeIndices[edge]--;
        if (faceEdge >= 0) {
            edgeIndices[faceEdge]++;
        }
    }

    private void reorder() {
        Map<Action, ActionWrapper> wrappers = new HashMap<>();
        actions.forEach(action -> wrappers.put(action, new ActionWrapper(action)));

        Map<Integer, ActionWrapper> providers = new HashMap<>();
        actions.forEach(action -> action.getAddedEdge().ifPresent(edge -> providers.put(edge, wrappers.get(action))));

        wrappers.values().forEach(wrapper -> wrapper.initDependencies(providers));
        wrappers.values().forEach(ActionWrapper::initRadius);
        actions.sort(Comparator.comparing(wrappers::get));
    }

    private class ActionWrapper implements Comparable<ActionWrapper> {
        private final Set<ActionWrapper> dependencies = new HashSet<>();

        private final Action action;

        private boolean initialized = false;

        private double radius = -1;

        private ActionWrapper(@NotNull Action action) {
            this.action = action;
        }

        private void initDependencies(@NotNull Map<Integer, ActionWrapper> providers) {
            if (initialized) {
                return;
            }
            Arrays.stream(action.getRemovedEdges()).mapToObj(providers::get).filter(wrapper -> wrapper != this).forEach(wrapper -> {
                wrapper.initDependencies(providers);
                dependencies.add(wrapper);
                dependencies.addAll(wrapper.dependencies);
            });
            initialized = true;
        }

        private double initRadius() {
            if (radius >= 0) {
                return radius;
            }
            if (dependencies.isEmpty()) {
                this.radius = action.getRadius();
                return radius;
            }
            double radius = dependencies.stream().mapToDouble(ActionWrapper::initRadius).max().orElseThrow();
            this.radius = Math.max(action.getRadius(), radius);
            return this.radius;
        }

        @Override
        public int compareTo(@NotNull ActionGenerator.ActionWrapper o) {
            if (o == this) {
                return 0;
            }
            int difference = Double.compare(radius, o.radius);
            if (difference != 0) {
                return difference;
            }
            if (o.dependencies.contains(this)) {
                return -1;
            }
            if (dependencies.contains(o)) {
                return 1;
            }
            return Integer.compare(action.getIndex(), o.action.getIndex());
        }
    }
}
