package apollon.util;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Util {
    public static final int RADIUS = 8;

    public static final int RADIUS_SQUARED = (int) Math.pow(RADIUS, 2);

    public static final int DIAMETER = 2 * RADIUS;

    private Util() {}

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
        return detA >= 0 && detB >= 0 && detA + detB <= 1;
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
        return new Point(round(point.x), round(point.y));
    }

    @NotNull
    public static List<PointD> toPointD(@NotNull Collection<Point> points) {
        return points.stream().map(Util::convert).collect(Collectors.toList());
    }

    @NotNull
    public static List<Point> toPoint(@NotNull Collection<PointD> points) {
        return points.stream().map(Util::convert).collect(Collectors.toList());
    }

    public static void drawArrow(@NotNull String name, @NotNull PointD a, @NotNull PointD b, @NotNull Graphics g) {
        draw(name, a, b, g);
        PointD dir = b.subtract(a).normalize();
        PointD normal = new PointD(RADIUS * dir.y, -RADIUS * dir.x);
        dir = new PointD(dir.x * DIAMETER, dir.y * DIAMETER);
        int[] x = new int[]{round(b.x), round(b.x - dir.x + normal.x), round(b.x - dir.x - normal.x)};
        int[] y = new int[]{round(b.y), round(b.y - dir.y + normal.y), round(b.y - dir.y - normal.y)};
        g.fillPolygon(x, y, 3);
    }

    public static void draw(@NotNull String name, @NotNull PointD a, @NotNull PointD b, @NotNull Graphics g) {
        draw(name, convert(a), convert(b), g);
    }

    public static void draw(@NotNull String name, @NotNull Point a, @NotNull Point b, @NotNull Graphics g) {
        g.drawString(name, (a.x + b.x) / 2 + RADIUS, (a.y + b.y) / 2);
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
        g.drawString(name, point.x + RADIUS, point.y);
        draw(point, g);
    }

    public static void draw(@NotNull PointD point, @NotNull Graphics g) {
        draw(convert(point), g);
    }

    public static void draw(@NotNull Point point, @NotNull Graphics g) {
        g.fillOval(point.x - RADIUS, point.y - RADIUS, DIAMETER, DIAMETER);
    }

    public static void drawCircle(@NotNull String name, @NotNull PointD point, int radius, @NotNull Graphics g) {
        drawCircle(name, convert(point), radius, g);
    }

    public static void drawCircle(@NotNull String name, @NotNull Point point, int radius, @NotNull Graphics g) {
        g.drawString(name, point.x, point.y - radius - RADIUS);
        drawCircle(point, radius, g);
    }

    public static void drawCircle(@NotNull PointD point, int radius, @NotNull Graphics g) {
        drawCircle(convert(point), radius, g);
    }

    public static void drawCircle(@NotNull Point point, int radius, @NotNull Graphics g) {
        g.drawOval(point.x - radius, point.y - radius, 2 * radius, 2 * radius);
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

    public static <T> int findMax(@NotNull List<T> list, @NotNull Predicate<T> predicate) {
        return findMax(list.size(), list::get, predicate);
    }

    public static <T> int findMax(@NotNull T[] array, @NotNull Predicate<T> predicate) {
        return findMax(array.length, index -> array[index], predicate);
    }

    public static <T> int findMax(int length, @NotNull IntFunction<T> function, @NotNull Predicate<T> predicate) {
        return findMax(length, index -> predicate.test(function.apply(index)));
    }

    public static int findMax(int length, @NotNull IntPredicate predicate) {
        if (length == 0) {
            return -1;
        }
        int min = 0;
        int max = length - 1;
        int index;
        while (min < max) {
            index = (min + max) / 2;
            if (predicate.test(index)) {
                min = index + 1;
            }
            else {
                max = index - 1;
            }
        }
        return predicate.test(min) ? min : min - 1;
    }

    public static <T> int findMin(@NotNull List<T> list, @NotNull Predicate<T> predicate) {
        return findMin(list.size(), list::get, predicate);
    }

    public static <T> int findMin(@NotNull T[] array, @NotNull Predicate<T> predicate) {
        return findMin(array.length, index -> array[index], predicate);
    }

    public static <T> int findMin(int length, @NotNull IntFunction<T> function, @NotNull Predicate<T> predicate) {
        return findMin(length, index -> predicate.test(function.apply(index)));
    }

    public static int findMin(int length, @NotNull IntPredicate predicate) {
        if (length == 0) {
            return -1;
        }
        int min = 0;
        int max = length - 1;
        int index;
        while (min < max) {
            index = (min + max) / 2;
            if (predicate.test(index)) {
                max = index - 1;
            }
            else {
                min = index + 1;
            }
        }
        if (predicate.test(min)) {
            return min;
        }
        return min == length - 1 ? -1 : min + 1;
    }

    public static int round(double value) {
        return (int) Math.round(value);
    }

    @NotNull
    public static String display(double value) {
        if (Double.isInfinite(value)) {
            return "" + value;
        }
        return "" + (double) round(1000 * value) / 1000;
    }

    @NotNull
    public static PointD load(@NotNull String value) {
        int comma = value.indexOf(',');
        double x = Double.parseDouble(value.substring(0, comma));
        double y = Double.parseDouble(value.substring(comma + 1));
        return new PointD(x, y);
    }

    @NotNull
    public static String save(@NotNull PointD point) {
        return point.x + "," + point.y;
    }

    public static void save(@NotNull Component component, @NotNull String data) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(component) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            OutputStream stream = new FileOutputStream(chooser.getSelectedFile());
            IOUtils.write(data, stream, StandardCharsets.UTF_8);
            stream.flush();
            stream.close();
        }
        catch (Exception ignored) {}
    }

    @NotNull
    public static Optional<List<String>> load(@NotNull Component component) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(component) != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }
        List<String> lines;
        try {
            InputStream stream = new FileInputStream(chooser.getSelectedFile());
            lines = IOUtils.readLines(stream, StandardCharsets.UTF_8);
            stream.close();
        }
        catch (Exception e) {
            return Optional.empty();
        }
        return Optional.of(lines);
    }
}
