package apollon.feature;

import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

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

    public static void addSamples(@NotNull Mat matrix, int radius) {
        List<PointD> samples = sample(matrix, radius);
        for (PointD sample : samples) {
            Imgproc.circle(matrix, new Point(sample.x, sample.y), radius, new Scalar(255));
        }
    }

    @NotNull
    public static List<PointD> sample(@NotNull Mat matrix, int radius) {
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
        return samples;
    }

    @NotNull
    public static BufferedImage toImage(@NotNull Mat matrix) {
        BufferedImage image = new BufferedImage(matrix.width(), matrix.height(), matrix.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        matrix.get(0, 0, data);
        return image;
    }
}
