package apollon.distance;

import apollon.util.Util;
import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.Collection;

public interface Distance {
    void compute(@NotNull Collection<PointD> x, @NotNull Collection<PointD> y);

    boolean isComputed();

    double getDistance();

    default void compute(@NotNull double[][] x, @NotNull double[][] y) {
        compute(Util.convert(x), Util.convert(y));
    }
}
