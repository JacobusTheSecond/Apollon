package apollon.feature;

import nu.pattern.OpenCV;
import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

public class Harris {
    static {
        OpenCV.loadLocally();
    }

    private static final int THRESHOLD = 200;

    private Harris() {}

    @NotNull
    public static List<PointD> extract(@NotNull String fileName, int width, int height) {
        Mat image = Imgcodecs.imread(fileName);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        return harris(image, width, height);
    }

    @NotNull
    private static List<PointD> harris(@NotNull Mat mat, int width, int height) {
        int blockSize = 9;
        int apertureSize = 5;
        double k = 0.1;

        Mat harris = new Mat();
        Mat norm = new Mat();
        Mat scaled = new Mat();
        Imgproc.cornerHarris(mat, harris, blockSize, apertureSize, k);
        Core.normalize(harris, norm, 0, 255, Core.NORM_MINMAX, CvType.CV_32FC1, new Mat());
        Core.convertScaleAbs(norm, scaled);
        List<PointD> points = new ArrayList<>();
        double widthFactor = (double) width / norm.cols();
        double heightFactor = (double) height / norm.rows();
        for (int y = 0; y < norm.rows(); y++) {
            for (int x = 0; x < norm.cols(); x++) {
                if ((int) norm.get(y, x)[0] > THRESHOLD) {
                    points.add(new PointD(x * widthFactor, y * heightFactor));
                }
            }
        }
        return points;
    }

    @NotNull
    private static BufferedImage toImage(@NotNull Mat mat) {
        BufferedImage image = new BufferedImage(mat.width(), mat.height(), mat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        mat.get(0, 0, data);
        return image;
    }
}
