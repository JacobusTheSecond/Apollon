package apollon.feature;

import apollon.util.Util;
import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Feature {
    private Feature() {
    }

    public static void laplace(@NotNull Mat source, @NotNull Mat target) {
        Imgproc.GaussianBlur(source, target, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);
        Imgproc.cvtColor(target, target, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Laplacian(target, target, CvType.CV_8UC3, 3, 1, 0, Core.BORDER_DEFAULT);
    }

    public static void harris(@NotNull Mat source, @NotNull Mat target) {
        Imgproc.GaussianBlur(source, target, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);
        Imgproc.cvtColor(source, target, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cornerHarris(target, target, 2, 3, .04);
        Core.normalize(target, target, 0, 255, Core.NORM_MINMAX);
        Core.convertScaleAbs(target, target);
    }

    public static void canny(@NotNull Mat source, @NotNull Mat target, int threshold) {
        Imgproc.GaussianBlur(source, target, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);
        Imgproc.cvtColor(target, target, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(target, target, threshold, threshold * 3, 3, false);
    }

    public static void addSamples(@NotNull Mat matrix) {
        int radius = computeRadius(matrix);
        List<PointD> samples = sample(matrix, radius, false);
        for (PointD sample : samples) {
            Imgproc.circle(matrix, new Point(sample.x, sample.y), radius, new Scalar(255));
        }
    }

    private static int computeRadius(@NotNull Mat matrix) {
        return Math.max(matrix.cols(), matrix.rows()) / 100;
    }

    @NotNull
    public static List<PointD> sample(@NotNull Mat matrix) {
        return sample(matrix, computeRadius(matrix), true);
    }

    @NotNull
    public static List<PointD> sample(@NotNull Mat matrix, int radius, boolean scalePoints) {
        int width = matrix.cols();
        int height = matrix.rows();
        long sum = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                sum += matrix.get(y, x)[0];
            }
        }
        int average = (int) (sum / (width * height));
        List<PointD> points = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix.get(y, x)[0] > average) {
                    points.add(new PointD(x, y));
                }
            }
        }
        List<PointD> samples = new ArrayList<>();
        while (!points.isEmpty()) {
            int index = (int) (Math.random() * points.size());
            PointD sample = points.remove(index);
            samples.add(sample);
            points.removeIf(point -> point.subtract(sample).length() < 2 * radius);
        }
        int max = samples.stream().mapToDouble(point -> Math.max(point.x, point.y)).mapToInt(Util::round).max().orElse(-1);
        if (!scalePoints) {
            return samples;
        }
        double scale = max >= 0 ? 1000. / max : 1;
        return samples.stream().map(point -> new PointD(scale * point.x, scale * point.y)).collect(Collectors.toList());
    }

    @NotNull
    public static BufferedImage toImage(@NotNull Mat matrix) {
        BufferedImage image = new BufferedImage(matrix.width(), matrix.height(), matrix.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        matrix.get(0, 0, data);
        return image;
    }
}
