package apollon;

import apollon.app.AbstractApp;
import apollon.app.View;
import apollon.feature.Harris;
import apollon.homology.one.HomologyOne;
import apollon.homology.zero.HomologyZero;
import apollon.voronoi.Voronoi;
import com.panayotis.gnuplot.GNUPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class HomologyApp extends AbstractApp {
    private final List<PointD> points = new ArrayList<>();

    private final Voronoi voronoi = new Voronoi();

    private final HomologyZero homologyZero = new HomologyZero(voronoi);

    private final HomologyOne homologyOne = new HomologyOne(voronoi);

    private boolean drawVertexEdges = true;

    private boolean drawVertices = true;

    private boolean drawSiteEdges = true;

    private int radius = GeometryUtil.RADIUS;

    private int selected = -1;

    public HomologyApp() {
        super(1920, 1080);
        render();
    }

    @Override
    public void mousePressed(int x, int y, int button, @NotNull View view) {
        int index = findPoint(x, y);
        if (index >= 0) {
            pointPressed(index, button);
            return;
        }
        pressed(x, y, button);
    }

    private int findPoint(int x, int y) {
        for (int i = 0; i < points.size(); i++) {
            PointD point = points.get(i);
            if (Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2) < GeometryUtil.RADIUS_SQUARED) {
                return i;
            }
        }
        return -1;
    }

    private void pointPressed(int index, int button) {
        if (button == MouseEvent.BUTTON1 || button == MouseEvent.BUTTON3) {
            if (button == MouseEvent.BUTTON1) {
                selected = index;
            }
            else {
                points.remove(index);
                update();
            }
            render();
        }
    }

    private void pressed(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1) {
            points.add(new PointD(x, y));
            update();
            render();
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button, @NotNull View view) {
        if (button == MouseEvent.BUTTON1 && selected >= 0) {
            selected = -1;
            render();
        }
    }

    @Override
    public void mouseDrag(int x, int y, int modifiers, @NotNull View view) {
        if ((modifiers & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
            updateSelected(x, y);
        }
    }

    @NotNull
    private Optional<PointD> getSelected() {
        return selected >= 0 ? Optional.of(points.get(selected)) : Optional.empty();
    }

    private void updateSelected(int x, int y) {
        if (selected >= 0) {
            points.set(selected, new PointD(x, y));
            update();
            render();
        }
    }

    @Override
    public void keyPressed(int code, @NotNull View view) {
        switch (code) {
            case KeyEvent.VK_ESCAPE:
                close();
                return;
            case KeyEvent.VK_ADD:
                changeRadius(1);
                return;
            case KeyEvent.VK_SUBTRACT:
                changeRadius(-1);
                return;
            case KeyEvent.VK_PLUS:
                changeRadius(true);
                return;
            case KeyEvent.VK_MINUS:
                changeRadius(false);
                return;
            case KeyEvent.VK_DELETE:
                clear();
                return;
            case KeyEvent.VK_V:
                drawVertices = !drawVertices;
                render();
                return;
            case KeyEvent.VK_E:
                drawVertexEdges = !drawVertexEdges;
                render();
                return;
            case KeyEvent.VK_D:
                drawSiteEdges = !drawSiteEdges;
                render();
                return;
            case KeyEvent.VK_SPACE:
                homologyOne.executeNextAction();
                render();
                return;
            case KeyEvent.VK_ENTER:
                homologyOne.executeActions();
                render();
                return;
            case KeyEvent.VK_R:
                homologyOne.compute();
                render();
                return;
            case KeyEvent.VK_P:
                plot();
                return;
            case KeyEvent.VK_O:
                load();
        }
    }

    private void load() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(getView()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        List<PointD> points = Harris.extract(file.getAbsolutePath(), getWidth(), getHeight());
        clear();
        this.points.addAll(points);
        update();
        render();
    }

    private void plot() {
        double[][] data = homologyOne.plot();
        if (data.length == 0) {
            return;
        }
        GNUPlot gnuPlot = new GNUPlot("C:\\Program Files\\gnuplot\\bin\\gnuplot.exe");
        DataSetPlot plot = new DataSetPlot(homologyOne.plot());
        gnuPlot.addPlot(plot);
        DataSetPlot line = new DataSetPlot(IntStream.range(0, 50).map(i -> 10 * i).mapToObj(i -> new double[]{i, i}).toArray(double[][]::new));
        line.setPlotStyle(new PlotStyle(Style.LINES));
        gnuPlot.addPlot(line);
        gnuPlot.plot();
    }

    private void clear() {
        points.clear();
        selected = -1;
        update();
        render();
    }

    private void changeRadius(int delta) {
        radius = Math.max(GeometryUtil.RADIUS, radius + delta);
        render();
    }

    private void changeRadius(boolean up) {
        if (!voronoi.isEmpty()) {
            radius = voronoi.nextRadius(radius, up);
            render();
        }
    }

    @Override
    protected void resized() {
        update();
        render();
    }

    private void render() {
        updateTitle();
        draw(this::render);
    }

    private void updateTitle() {
        StringBuilder title = new StringBuilder("Radius: " + radius);
        getSelected().ifPresent(selected -> title.append(", Position: ").append(selected.x).append(", ").append(selected.y));
        getView().setTitle(title.toString());
    }

    private void render(@NotNull Graphics g) {
        renderBackground(g);
        renderPoints(g);
        renderVoronoi(g);
        renderHomology(g);
    }

    private void renderBackground(@NotNull Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void renderPoints(@NotNull Graphics g) {
        for (int i = 0; i < points.size(); i++) {
            PointD point = points.get(i);
            g.setColor(selected >= 0 && i == selected ? Color.DARK_GRAY : Color.BLACK);
            GeometryUtil.draw(point, g);
            renderCircle(point, g);
        }
    }

    private void renderCircle(@NotNull PointD point, @NotNull Graphics g) {
        if (radius > GeometryUtil.RADIUS) {
            g.setColor(Color.GREEN);
            GeometryUtil.drawCircle(point, radius, g);
        }
    }

    private void renderVoronoi(@NotNull Graphics g) {
        if (voronoi.isEmpty()) {
            return;
        }
        if (drawVertices) {
            g.setColor(Color.BLUE);
            voronoi.forEachVertex((vertex, index) -> GeometryUtil.draw(vertex, g));
        }

        if (drawVertexEdges) {
            g.setColor(Color.BLACK);
            voronoi.forEachEdge(edge -> GeometryUtil.draw(edge.getVertexA(), edge.getVertexB(), g));
        }

        if (drawSiteEdges) {
            g.setColor(Color.RED);
            voronoi.forEachEdge((edge, index) -> GeometryUtil.draw("" + edge.getLength(), edge.getSiteA(), edge.getSiteB(), g));
        }
    }

    private void renderHomology(@NotNull Graphics g) {
        homologyZero.render(g);
        homologyOne.render(g);
    }

    private void update() {
        voronoi.compute(points, getWidth(), getHeight());
        homologyZero.compute();
        homologyOne.compute();
    }

    public static void main(String[] args) {
        new HomologyApp().await();
        System.exit(0);
    }
}
