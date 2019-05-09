package apollon.homology.zero;

import apollon.voronoi.VEdge;
import apollon.voronoi.Voronoi;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;

public class HomologyZero {
    private final Voronoi voronoi;

    private double[] homology;

    private int[] component;

    private double radius;

    public HomologyZero(@NotNull Voronoi voronoi) {
        this.voronoi = voronoi;
    }

    public void compute() {
        if (voronoi.isEmpty()) {
            return;
        }
        init(voronoi.getSitesCount());
        voronoi.forEachEdge(this::add);
        finish();
    }

    private void init(int components) {
        homology = new double[components];
        component = new int[components];
        for (int i = 0; i < components; i++) {
            component[i] = i;
        }
        radius = 0;
    }

    private void add(@NotNull VEdge edge) {
        int a = edge.getSiteAIndex();
        int b = edge.getSiteBIndex();
        int componentA = find(a);
        int componentB = find(b);
        radius = edge.getLength() / 2;
        if (componentA == componentB) {
            return;
        }
        if (componentA < componentB) {
            component[b] = componentA;
            homology[componentB] = radius;
            return;
        }
        component[a] = componentB;
        homology[componentA] = radius;
    }

    private void finish() {
        homology[find(0)] = Double.POSITIVE_INFINITY;
    }

    private int find(int index) {
        while (index != component[index]) {
            index = component[index];
        }
        return index;
    }

    public void render(@NotNull Graphics g) {
        if (!voronoi.isEmpty()) {
            g.setColor(Color.BLACK);
            g.drawString("Hom0: " + this, 0, 10);
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(homology);
    }
}