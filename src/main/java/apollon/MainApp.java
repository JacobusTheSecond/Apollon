package apollon;

import apollon.distance.Bottleneck;
import apollon.distance.Distance;
import apollon.distance.Wasserstein;
import apollon.feature.Feature;
import apollon.homology.Homology;
import apollon.util.Util;
import apollon.voronoi.Voronoi;
import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);

    private final JFrame frame = new JFrame();

    public static void main(String[] args) {
        Util.init();
        new MainApp().execute();
        System.exit(0);
    }

    private void execute() {
        Image a = new Image();
        if (a.isEmpty()) {
            return;
        }
        a.computeHomology();

        Image b = new Image();
        if (b.isEmpty()) {
            return;
        }
        b.computeHomology();

        double[] wasserstein = a.wassersteinTo(b);
        double[] bottleneck = a.bottleneckTo(b);
        LOGGER.info("Wasserstein: {}, {}", wasserstein[0], wasserstein[1]);
        LOGGER.info("Bottleneck: {}, {}", bottleneck[0], bottleneck[1]);
    }

    private class Image {
        private final List<PointD> points = new ArrayList<>();

        private int width;

        private int height;

        private double[][] homologyZero;

        private double[][] homologyOne;

        public Image() {
            points.addAll(loadImage());
            width = points.stream().mapToDouble(point -> point.x).mapToInt(Util::round).max().orElse(0);
            height = points.stream().mapToDouble(point -> point.y).mapToInt(Util::round).max().orElse(0);
        }

        @NotNull
        private List<PointD> loadImage() {
            JFileChooser chooser = Util.choose();
            if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
                return Collections.emptyList();
            }
            File file = chooser.getSelectedFile();
            Mat image = Imgcodecs.imread(file.getAbsolutePath());
            Feature.canny(image, image, 200);
            return Feature.sample(image);
        }

        public boolean isEmpty() {
            return points.isEmpty();
        }

        public void computeHomology() {
            Voronoi voronoi = new Voronoi();
            voronoi.compute(points, -width, -height, 3 * width, 3 * height);
            Homology homology = new Homology(voronoi);
            homology.compute();
            homology.executeActions();
            homologyZero = homology.plotZero();
            homologyOne = homology.plotOne();
        }

        @NotNull
        private double[] distanceTo(@NotNull Image image, @NotNull Distance distance) {
            distance.compute(homologyZero, image.homologyZero);
            double zero = distance.getDistance();
            distance.compute(homologyOne, image.homologyOne);
            double one = distance.getDistance();
            return new double[]{zero, one};
        }

        @NotNull
        public double[] wassersteinTo(@NotNull Image image) {
            return distanceTo(image, new Wasserstein());
        }

        @NotNull
        public double[] bottleneckTo(@NotNull Image image) {
            return distanceTo(image, new Bottleneck());
        }
    }
}
