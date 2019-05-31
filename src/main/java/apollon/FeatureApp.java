package apollon;

import apollon.app.AbstractApp;
import apollon.app.View;
import apollon.feature.Feature;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class FeatureApp extends AbstractApp {
    private final Mat target = new Mat();

    private BufferedImage image;

    private Mat source;

    private int threshold = 0;

    public FeatureApp() {
        render();
        show();
    }

    @Override
    public void keyPressed(int code, int modifiers, @NotNull View view) {
        switch (code) {
            case KeyEvent.VK_O:
                open();
                return;
            case KeyEvent.VK_L:
                laplace();
                return;
            case KeyEvent.VK_H:
                harris();
                return;
            case KeyEvent.VK_C:
                canny();
                return;
            case KeyEvent.VK_ADD:
                changeThreshold(1);
                return;
            case KeyEvent.VK_SUBTRACT:
                changeThreshold(-1);
                return;
            case KeyEvent.VK_ESCAPE:
                close();
        }
    }

    private void changeThreshold(int delta) {
        threshold = Math.max(0, Math.min(100, threshold + delta));
        render();
    }

    public void open() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(getView()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            source = Imgcodecs.imread(file.getAbsolutePath());
            image = Feature.toImage(source);
            render();
        }
        catch (Exception ignored) {}
    }

    private void laplace() {
        Feature.laplace(source, target);
        update();
    }

    private void harris() {
        Feature.harris(source, target);
        update();
    }

    private void canny() {
        Feature.canny(source, target, threshold);
        update();
    }

    private void update() {
        image = Feature.toImage(target);
        render();
    }

    @Override
    protected void resized() {
        render();
    }

    private void render() {
        draw(this::render);
    }

    private void render(@NotNull Graphics g) {
        getView().setTitle("Threshold: " + threshold);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
    }

    public static void main(String[] args) {
        new FeatureApp().await();
        System.exit(0);
    }
}
