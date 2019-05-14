package apollon.distance;

import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.Collection;

public interface Distance {
    void compute(@NotNull Collection<PointD> x, @NotNull Collection<PointD> y);

    boolean isComputed();

    double getDistance();
}
