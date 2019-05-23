package apollon;

import apollon.app.AbstractApp;
import apollon.app.View;
import apollon.feature.Harris;
import apollon.homology.one.HomologyOne;
import apollon.homology.zero.HomologyZero;
import apollon.util.Util;
import apollon.voronoi.Voronoi;
import com.panayotis.gnuplot.GNUPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import org.jetbrains.annotations.NotNull;
import org.kynosarges.tektosyne.geometry.PointD;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class HomologyApp extends AbstractApp {
    private final List<PointD> points = new ArrayList<>();

    private final Voronoi voronoi = new Voronoi();

    private final HomologyZero homologyZero = new HomologyZero(voronoi);

    private final HomologyOne homologyOne = new HomologyOne(voronoi);

    private boolean drawVoronoiEdges;

    private boolean drawVoronoiVertices;

    private boolean drawDelaunayEdges;

    private boolean drawCycles = true;

    private boolean drawActions = true;

    private int selected = -1;

    public HomologyApp() {
        init();
        render();
    }

    private void init() {
        addMenu(createFileMenu());
        addMenu(createEditMenu());
        addMenu(createViewMenu());
    }

    @NotNull
    private JMenu createFileMenu() {
        JMenu menu = new JMenu("File");
        menu.setMnemonic('F');
        menu.add(createOpenMenuItem());
        menu.add(createSaveMenuItem());
        menu.addSeparator();
        menu.add(createImportMenuItem());
        menu.add(createExportMenuItem());
        menu.addSeparator();
        menu.add(createCloseMenuItem());
        return menu;
    }

    @NotNull
    private JMenuItem createOpenMenuItem() {
        JMenuItem item = new JMenuItem("Open");
        item.setMnemonic('O');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        item.addActionListener(e -> open());
        return item;
    }

    @NotNull
    private JMenuItem createSaveMenuItem() {
        JMenuItem item = new JMenuItem("Save");
        item.setMnemonic('S');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        item.addActionListener(e -> save());
        return item;
    }

    @NotNull
    private JMenuItem createImportMenuItem() {
        JMenuItem item = new JMenuItem("Import");
        item.setMnemonic('I');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
        item.addActionListener(e -> importImage());
        return item;
    }

    @NotNull
    private JMenuItem createExportMenuItem() {
        JMenuItem item = new JMenuItem("Export");
        item.setMnemonic('E');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
        item.addActionListener(e -> export());
        return item;
    }

    @NotNull
    private JMenu createEditMenu() {
        JMenu menu = new JMenu("Edit");
        menu.setMnemonic('E');
        menu.add(createNextActionMenuItem());
        menu.add(createAllActionsMenuItem());
        menu.add(createResetMenuItem());
        menu.addSeparator();
        menu.add(createClearMenuItem());
        menu.add(createRandomizeMenuItem());
        return menu;
    }

    @NotNull
    private JMenuItem createNextActionMenuItem() {
        JMenuItem item = new JMenuItem("Execute next action - Space");
        item.setMnemonic('n');
        item.addActionListener(e -> {
            homologyOne.executeNextAction();
            render();
        });
        return item;
    }

    @NotNull
    private JMenuItem createAllActionsMenuItem() {
        JMenuItem item = new JMenuItem("Execute all actions");
        item.setMnemonic('a');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        item.addActionListener(e -> {
            homologyOne.executeActions();
            render();
        });
        return item;
    }

    @NotNull
    private JMenuItem createResetMenuItem() {
        JMenuItem item = new JMenuItem("Reset");
        item.setMnemonic('R');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        item.addActionListener(e -> {
            homologyOne.compute();
            render();
        });
        return item;
    }

    @NotNull
    private JMenuItem createClearMenuItem() {
        JMenuItem item = new JMenuItem("Clear");
        item.setMnemonic('C');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        item.addActionListener(e -> clear());
        return item;
    }

    @NotNull
    private JMenuItem createRandomizeMenuItem() {
        JMenuItem item = new JMenuItem("Add random points - Ctrl+R");
        item.setMnemonic('p');
        item.addActionListener(e -> addRandomPoints());
        return item;
    }

    @NotNull
    private JMenu createViewMenu() {
        JMenu menu = new JMenu("View");
        menu.setMnemonic('V');
        menu.add(createCyclesMenuItem());
        menu.add(createActionsMenuItem());
        menu.addSeparator();
        menu.add(createDelaunayMenuItem());
        menu.add(createVoronoiMenuItem());
        menu.add(createVerticesMenuItem());
        menu.addSeparator();
        menu.add(createPlotMenuItem());
        return menu;
    }

    @NotNull
    private JMenuItem createCyclesMenuItem() {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem("Show cycles");
        item.setSelected(true);
        item.setMnemonic('c');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        item.addChangeListener(e -> {
            drawCycles = item.isSelected();
            render();
        });
        return item;
    }

    @NotNull
    private JMenuItem createActionsMenuItem() {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem("Show actions");
        item.setSelected(true);
        item.setMnemonic('a');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        item.addChangeListener(e -> {
            drawActions = item.isSelected();
            render();
        });
        return item;
    }

    @NotNull
    private JMenuItem createDelaunayMenuItem() {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem("Show Delaunay edges");
        item.setMnemonic('D');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
        item.addChangeListener(e -> {
            drawDelaunayEdges = item.isSelected();
            render();
        });
        return item;
    }

    @NotNull
    private JMenuItem createVoronoiMenuItem() {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem("Show Voronoi edges");
        item.setMnemonic('E');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        item.addChangeListener(e -> {
            drawVoronoiEdges = item.isSelected();
            render();
        });
        return item;
    }

    @NotNull
    private JMenuItem createVerticesMenuItem() {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem("Show Voronoi Vertices");
        item.setMnemonic('V');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        item.addChangeListener(e -> {
            drawVoronoiVertices = item.isSelected();
            render();
        });
        return item;
    }

    @NotNull
    private JMenuItem createPlotMenuItem() {
        JMenuItem item = new JMenuItem("Plot");
        item.setMnemonic('P');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        item.addActionListener(e -> plot());
        return item;
    }

    @Override
    public void keyPressed(int code, int modifiers, @NotNull View view) {
        if (code == KeyEvent.VK_SPACE) {
            homologyOne.executeNextAction();
            render();
        }
        else if (code == KeyEvent.VK_R && (modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
            addRandomPoints();
        }
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

    private boolean isPoint(@NotNull PointD point) {
        Point p = Util.convert(point);
        return findPoint(p.x, p.y) != -1;
    }

    private int findPoint(int x, int y) {
        for (int i = 0; i < points.size(); i++) {
            PointD point = points.get(i);
            if (Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2) < Util.RADIUS_SQUARED) {
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

    private void open() {
        Util.load(getView()).ifPresent(lines -> {
            clear();
            lines.stream().map(Util::load).forEach(points::add);
            update();
            render();
        });
    }

    private void export() {
        save(Stream.of(homologyOne.plot()).map(point -> new PointD(point[0], point[1])).map(Util::save).collect(Collectors.joining("\n")));
    }

    private void save() {
        save(points.stream().map(Util::save).collect(Collectors.joining("\n")));
    }

    private void save(@NotNull String data) {
        Util.save(getView(), data);
    }

    private void importImage() {
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

    private void addRandomPoints() {
        PointD point;
        for (int i = 0; i < 10; i++) {
            do {
                point = new PointD(Math.random() * getWidth(), Math.random() * getHeight());
            } while (isPoint(point));
            points.add(point);
        }
        update();
        render();
    }

    private void clear() {
        points.clear();
        selected = -1;
        update();
        render();
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
        StringBuilder title = new StringBuilder("Sites: " + points.size());
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
        g.setColor(Color.DARK_GRAY);
        points.forEach(point -> Util.draw(point, g));
    }

    private void renderVoronoi(@NotNull Graphics g) {
        if (voronoi.isEmpty()) {
            return;
        }
        if (drawVoronoiVertices) {
            g.setColor(Color.BLUE);
            voronoi.forEachVertex((vertex, index) -> Util.draw(vertex, g));
        }

        if (drawVoronoiEdges) {
            g.setColor(Color.BLACK);
            voronoi.forEachEdge(edge -> Util.draw(edge.getVertexA(), edge.getVertexB(), g));
        }

        if (drawDelaunayEdges) {
            g.setColor(Color.RED);
            voronoi.forEachEdge((edge, index) -> Util.draw("" + Util.display(edge.getLength()), edge.getSiteA(), edge.getSiteB(), g));
        }
    }

    private void renderHomology(@NotNull Graphics g) {
        homologyOne.render(g);
        if (drawCycles) {
            homologyOne.renderCycles(g);
        }
        if (drawActions) {
            homologyOne.renderActions(g, getWidth());
        }
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
