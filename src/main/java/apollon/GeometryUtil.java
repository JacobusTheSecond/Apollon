package apollon;

import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class GeometryUtil {
    public static final int RADIUS = 8;

    public static final int RADIUS_SQUARED = (int) Math.pow(RADIUS, 2);

    public static final int DIAMETER = 2 * RADIUS;

    private GeometryUtil() {}

    public static boolean isInside(@NotNull PointD point, @NotNull PointD[] triangle) {
        PointD a = triangle[0];
        PointD b = triangle[1].subtract(a);
        PointD c = triangle[2].subtract(a);
        double denominator = determinant(b, c);
        if (denominator == 0) {
            return false;
        }
        double detA = (determinant(point, c) - determinant(a, c)) / denominator;
        double detB = (determinant(a, b) - determinant(point, b)) / denominator;
        return detA > 0 && detB > 0 && detA + detB < 1;
    }

    private static double determinant(@NotNull PointD a, @NotNull PointD b) {
        return a.x * b.y - a.y * b.x;
    }

    @NotNull
    public static PointD convert(@NotNull Point point) {
        return new PointD(point.x, point.y);
    }

    @NotNull
    public static Point convert(@NotNull PointD point) {
        return new Point((int) Math.round(point.x), (int) Math.round(point.y));
    }

    @NotNull
    public static List<PointD> toPointD(@NotNull Collection<Point> points) {
        return points.stream().map(GeometryUtil::convert).collect(Collectors.toList());
    }

    @NotNull
    public static List<Point> toPoint(@NotNull Collection<PointD> points) {
        return points.stream().map(GeometryUtil::convert).collect(Collectors.toList());
    }

    public static void draw(@NotNull String name, @NotNull PointD a, @NotNull PointD b, @NotNull Graphics g) {
        draw(name, convert(a), convert(b), g);
    }

    public static void draw(@NotNull String name, @NotNull Point a, @NotNull Point b, @NotNull Graphics g) {
        g.drawString(name, (a.x + b.x) / 2 + GeometryUtil.RADIUS, (a.y + b.y) / 2);
        draw(a, b, g);
    }

    public static void draw(@NotNull PointD a, @NotNull PointD b, @NotNull Graphics g) {
        draw(convert(a), convert(b), g);
    }

    public static void draw(@NotNull Point a, @NotNull Point b, @NotNull Graphics g) {
        g.drawLine(a.x, a.y, b.x, b.y);
    }

    public static void draw(@NotNull String name, @NotNull PointD point, @NotNull Graphics g) {
        draw(name, convert(point), g);
    }

    public static void draw(@NotNull String name, @NotNull Point point, @NotNull Graphics g) {
        g.drawString(name, point.x + GeometryUtil.RADIUS, point.y);
        draw(point, g);
    }

    public static void draw(@NotNull PointD point, @NotNull Graphics g) {
        draw(convert(point), g);
    }

    public static void draw(@NotNull Point point, @NotNull Graphics g) {
        g.fillOval(point.x - GeometryUtil.RADIUS, point.y - GeometryUtil.RADIUS, GeometryUtil.DIAMETER, GeometryUtil.DIAMETER);
    }

    @NotNull
    public static int[] toArray(@NotNull Collection<Integer> integers) {
        int[] array = new int[integers.size()];
        int index = 0;
        for (int integer : integers) {
            array[index++] = integer;
        }
        return array;
    }

    @NotNull
    public static List<Integer> toList(@NotNull int[] integers) {
        List<Integer> list = new ArrayList<>(integers.length);
        for (int integer : integers) {
            list.add(integer);
        }
        return list;
    }

    @NotNull
    public static Set<Integer> toSet(@NotNull int[] integers) {
        Set<Integer> set = new HashSet<>();
        for (int integer : integers) {
            set.add(integer);
        }
        return set;
    }

    public static void await(@NotNull CountDownLatch latch) {
        try {
            latch.await();
        }
        catch (Exception ignored) {}
    }
}