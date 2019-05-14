package apollon;

import apollon.app.AbstractApp;
import apollon.app.View;
import apollon.distance.Bottleneck;
import apollon.distance.Wasserstein;
import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class DistanceApp extends AbstractApp {
    private final Wasserstein wasserstein = new Wasserstein();

    private final Bottleneck bottleneck = new Bottleneck();

    private final List<Point> x = new ArrayList<>();

    private final List<Point> y = new ArrayList<>();

    public DistanceApp() {
        super(1920, 1080);
        render();
    }

    @Override
    public void mousePressed(int x, int y, int button, @NotNull View view) {
        if (button == MouseEvent.BUTTON1) {
            this.x.add(new Point(x, y));
        }
        else if (button == MouseEvent.BUTTON3) {
            this.y.add(new Point(x, y));
        }
        render();
    }

    @Override
    public void keyPressed(int code, @NotNull View view) {
        switch (code) {
            case KeyEvent.VK_SPACE:
                compute();
                return;
            case KeyEvent.VK_DELETE:
                clear();
        }
    }

    private void clear() {
        x.clear();
        y.clear();
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
        renderBackground(g);
        renderDiagonal(g);
        renderPoints(g);
        renderWasserstein(g);
        renderBottleneck(g);
    }

    private void renderBackground(@NotNull Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void renderDiagonal(@NotNull Graphics g) {
        g.setColor(Color.BLACK);
        int max = Math.max(getWidth(), getHeight());
        GeometryUtil.draw(new PointD(), new PointD(max, max), g);
    }

    private void renderPoints(@NotNull Graphics g) {
        g.setColor(Color.RED);
        x.forEach(point -> GeometryUtil.draw(point, g));
        g.setColor(Color.BLUE);
        y.forEach(point -> GeometryUtil.draw(point, g));
    }

    private void renderWasserstein(@NotNull Graphics g) {
        if (!wasserstein.isComputed()) {
            return;
        }
        g.setColor(Color.GREEN);
        wasserstein.forEachXY((x, y) -> GeometryUtil.draw(x, y, g));
        wasserstein.forEachY((x, y) -> GeometryUtil.draw(x, y, g));

        g.setColor(Color.BLACK);
        g.drawString("Wasserstein: " + wasserstein.getDistance(), 5, 10);
    }

    private void renderBottleneck(@NotNull Graphics g) {
        if (!bottleneck.isComputed()) {
            return;
        }
        g.setColor(Color.BLUE);
        bottleneck.forEachEdge((x, y) -> GeometryUtil.draw(x, y, g));
        g.setColor(Color.RED);
        bottleneck.forMaxEdge((x, y) -> GeometryUtil.draw(x, y, g));

        g.setColor(Color.BLACK);
        g.drawString("Bottleneck: " + bottleneck.getDistance(), 5, 25);
    }

    private void compute() {
        List<PointD> x = GeometryUtil.toPointD(this.x);
        List<PointD> y = GeometryUtil.toPointD(this.y);
        wasserstein.compute(x, y);
        bottleneck.compute(x, y);
        render();
    }

    public static void main(String[] args) {
        new DistanceApp().await();
        System.exit(0);
    }
}
