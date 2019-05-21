package apollon.voronoi;

import apollon.util.Util;
import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.VoronoiEdge;

import java.awt.*;

public class VEdge implements Comparable<VEdge> {
    private Voronoi voronoi;

    private final int siteA;

    private final int siteB;

    private final int vertexA;

    private final int vertexB;

    private final double length;

    private int index = -1;

    public VEdge(@NotNull Voronoi voronoi, @NotNull VoronoiEdge edge) {
        this.voronoi = voronoi;
        siteA = edge.site1;
        siteB = edge.site2;
        vertexA = edge.vertex1;
        vertexB = edge.vertex2;
        length = voronoi.getSite(edge.site1).subtract(voronoi.getSite(edge.site2)).length();
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public int getSiteAIndex() {
        return siteA;
    }

    public int getSiteBIndex() {
        return siteB;
    }

    public boolean hasSite(int site) {
        return getSiteAIndex() == site || getSiteBIndex() == site;
    }

    public boolean hasVertex(int vertex) {
        return getVertexAIndex() == vertex || getVertexBIndex() == vertex;
    }

    @NotNull
    public PointD getDSiteA() {
        return voronoi.getSite(getSiteAIndex());
    }

    @NotNull
    public PointD getDSiteB() {
        return voronoi.getSite(getSiteBIndex());
    }

    @NotNull
    public Point getSiteA() {
        return Util.convert(getDSiteA());
    }

    @NotNull
    public Point getSiteB() {
        return Util.convert(getDSiteB());
    }

    @NotNull
    public PointD[] getDSites() {
        return new PointD[]{getDSiteA(), getDSiteB()};
    }

    @NotNull
    public Point[] getSites() {
        return new Point[]{getSiteA(), getSiteB()};
    }

    public int getVertexAIndex() {
        return vertexA;
    }

    public int getVertexBIndex() {
        return vertexB;
    }

    @NotNull
    public PointD getDVertexA() {
        return voronoi.getVertex(getVertexAIndex());
    }

    @NotNull
    public PointD getDVertexB() {
        return voronoi.getVertex(getVertexBIndex());
    }

    @NotNull
    public Point getVertexA() {
        return Util.convert(getDVertexA());
    }

    @NotNull
    public Point getVertexB() {
        return Util.convert(getDVertexB());
    }

    @NotNull
    public PointD[] getDVertices() {
        return new PointD[]{getDVertexA(), getDVertexB()};
    }

    @NotNull
    public Point[] getVertices() {
        return new Point[]{getVertexA(), getVertexB()};
    }

    public double getLength() {
        return length;
    }

    public int getOtherSite(int site) {
        return getSiteAIndex() == site ? getSiteBIndex() : getSiteAIndex();
    }

    public int getOtherSite(@NotNull VEdge edge) {
        if (getSiteAIndex() == edge.getSiteAIndex() || getSiteAIndex() == edge.getSiteBIndex()) {
            return getSiteBIndex();
        }
        return getSiteAIndex();
    }

    @Override
    public int compareTo(@NotNull VEdge o) {
        return Double.compare(getLength(), o.getLength());
    }
}
