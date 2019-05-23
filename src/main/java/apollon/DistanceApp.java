package apollon;

import apollon.app.AbstractApp;
import apollon.app.View;
import apollon.distance.Bottleneck;
import apollon.distance.Wasserstein;
import apollon.util.Util;
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

    private final List<PointD> x = new ArrayList<>();

    private final List<PointD> y = new ArrayList<>();

    private boolean drawWasserstein = true;

    private boolean drawBottleneck = true;

    public DistanceApp() {
        render();
        show();
    }

    @Override
    public void mousePressed(int x, int y, int button, @NotNull View view) {
        if (button == MouseEvent.BUTTON1) {
            this.x.add(new PointD(x, y));
        }
        else if (button == MouseEvent.BUTTON3) {
            this.y.add(new PointD(x, y));
        }
        clearDistances();
        render();
    }

    @Override
    public void keyPressed(int code, int modifiers, @NotNull View view) {
        switch (code) {
            case KeyEvent.VK_SPACE:
                compute();
                return;
            case KeyEvent.VK_DELETE:
                clear();
                return;
            case KeyEvent.VK_W:
                drawWasserstein = !drawWasserstein;
                render();
                return;
            case KeyEvent.VK_B:
                drawBottleneck = !drawBottleneck;
                render();
                return;
            case KeyEvent.VK_O:
                load(true);
                return;
            case KeyEvent.VK_P:
                load(false);
        }
    }

    private void load(boolean x) {
        List<PointD> points = x ? this.x : y;
        Util.load(getView()).ifPresent(lines -> {
            clear(points);
            lines.stream().map(Util::load).forEach(points::add);
            render();
        });
    }

    private void clear(@NotNull List<PointD> points) {
        points.clear();
        clearDistances();
        render();
    }

    private void clear() {
        x.clear();
        y.clear();
        clearDistances();
        render();
    }

    private void clearDistances() {
        wasserstein.clear();
        bottleneck.clear();
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
        Util.draw(new PointD(), new PointD(max, max), g);
    }

    private void renderPoints(@NotNull Graphics g) {
        g.setColor(Color.RED);
        x.forEach(point -> Util.draw(point, g));
        g.setColor(Color.BLUE);
        y.forEach(point -> Util.draw(point, g));
    }

    private void renderWasserstein(@NotNull Graphics g) {
        if (!wasserstein.isComputed()) {
            return;
        }
        if (drawWasserstein) {
            g.setColor(Color.GREEN);
            wasserstein.forEachXY((x, y) -> Util.draw("" + x.subtract(y).length(), x, y, g));
            wasserstein.forEachY((x, y) -> Util.draw("" + x.subtract(y).length(), x, y, g));
        }
        g.setColor(Color.BLACK);
        g.drawString("Wasserstein: " + wasserstein.getDistance(), 5, 10);
    }

    private void renderBottleneck(@NotNull Graphics g) {
        if (!bottleneck.isComputed()) {
            return;
        }
        if (drawBottleneck) {
            g.setColor(Color.BLUE);
            bottleneck.forEachEdge((x, y) -> Util.draw("" + x.subtract(y).length(), x, y, g));
            g.setColor(Color.RED);
            bottleneck.forMaxEdge((x, y) -> Util.draw("" + x.subtract(y).length(), x, y, g));
        }
        g.setColor(Color.BLACK);
        g.drawString("Bottleneck: " + bottleneck.getDistance(), 5, 25);
    }

    private void compute() {
        wasserstein.compute(x, y);
        bottleneck.compute(x, y);
        render();
    }

    public static void main(String[] args) {
        new DistanceApp().await();
        System.exit(0);
    }
}
