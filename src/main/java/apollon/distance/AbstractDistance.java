package apollon.distance;

import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractDistance implements Distance {
    private final List<PointD> xPoints = new ArrayList<>();

    private final List<PointD> yPoints = new ArrayList<>();

    private boolean computed = false;

    private double distance;

    @Override
    public void compute(@NotNull Collection<PointD> x, @NotNull Collection<PointD> y) {
        clear();
        xPoints.addAll(x);
        yPoints.addAll(y);
        distance = compute();
        computed = true;
    }

    @Override
    public boolean isComputed() {
        return computed;
    }

    @Override
    public double getDistance() {
        return distance;
    }

    @NotNull
    protected List<PointD> getXPoints() {
        return xPoints;
    }

    @NotNull
    public List<PointD> getYPoints() {
        return yPoints;
    }

    @NotNull
    protected PointD getX(int index) {
        return xPoints.get(index);
    }

    @NotNull
    protected PointD getY(int index) {
        return yPoints.get(index);
    }

    protected int getXCount() {
        return xPoints.size();
    }

    protected int getYCount() {
        return yPoints.size();
    }

    protected int getSum() {
        return getXCount() + getYCount();
    }

    public void clear() {
        computed = false;
        xPoints.clear();
        yPoints.clear();
    }

    protected abstract double compute();
}
