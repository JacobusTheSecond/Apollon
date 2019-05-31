package apollon.feature;

import org.jetbrains.annotations.NotNull;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Feature {
    private Feature() {}

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

    @NotNull
    public static BufferedImage toImage(@NotNull Mat matrix) {
        BufferedImage image = new BufferedImage(matrix.width(), matrix.height(), matrix.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        matrix.get(0, 0, data);
        return image;
    }
}
