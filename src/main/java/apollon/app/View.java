package apollon.app;

import apollon.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class View extends JFrame {
    private final CountDownLatch latch = new CountDownLatch(1);

    private final JMenuBar menuBar = new JMenuBar();

    private final JPanel canvas;

    private Listener listener;

    private Image image;

    private int width;

    private int height;

    public View() {
        this(new Listener() {});
    }

    public View(int width, int height) {
        this(width, height, new Listener() {});
    }

    public View(@NotNull BufferedImage image) {
        this(image, image.getWidth(null), image.getHeight(null));
    }

    public View(@NotNull BufferedImage image, int width, int height) {
        this(image, width, height, new Listener() {});
    }

    public View(@NotNull BufferedImage image, @NotNull Listener listener) {
        this(image, image.getWidth(null), image.getHeight(null), listener);
    }

    public View(@NotNull BufferedImage image, int width, int height, @NotNull Listener listener) {
        this(width, height, listener);
        setImage(image);
    }

    public View(@NotNull Listener listener) {
        this(800, 600, listener);
        setExtendedState(MAXIMIZED_BOTH);
    }

    public View(int width, int height, @NotNull Listener listener) {
        super("View");
        this.width = width;
        this.height = height;
        this.listener = listener;
        canvas = createCanvas();
        init();
    }

    @NotNull
    private JPanel createCanvas() {
        return new JPanel() {
            @Override
            public void paint(Graphics g) {
                getImage().ifPresent(image -> g.drawImage(image, 0, 0, null));
            }
        };
    }

    @NotNull
    private Optional<Image> getImage() {
        return Optional.ofNullable(image);
    }

    private void init() {
        initContainer();
        initMenu();
        pack();
        initListeners();
        setLocationRelativeTo(null);
    }

    private void initContainer() {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        canvas.setPreferredSize(new Dimension(width, height));
        container.add(canvas, BorderLayout.CENTER);
    }

    private void initListeners() {
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                listener.mousePressed(e.getX(), e.getY(), e.getButton(), View.this);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                listener.mouseReleased(e.getX(), e.getY(), e.getButton(), View.this);
            }
        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                listener.mouseDrag(e.getX(), e.getY(), e.getModifiersEx(), View.this);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                listener.mouseMove(e.getX(), e.getY(), View.this);
            }
        });
        canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSize();
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                listener.keyPressed(e.getExtendedKeyCode(), e.getModifiersEx(), View.this);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                listener.keyReleased(e.getExtendedKeyCode(), e.getModifiersEx(), View.this);
            }
        });
    }

    private void initMenu() {
        setJMenuBar(menuBar);
        menuBar.setPreferredSize(new Dimension(width, 21));
    }

    public void addMenu(@NotNull JMenu menu) {
        menuBar.add(menu);
        revalidate();
    }

    public void setImage(@Nullable BufferedImage image) {
        this.image = image;
    }

    public void setListener(@NotNull Listener listener) {
        this.listener = listener;
    }

    private void updateSize() {
        this.width = canvas.getWidth();
        this.height = canvas.getHeight();
        listener.resize(width, height, this);
    }

    public void render() {
        canvas.repaint();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            latch.countDown();
        }
    }

    public void close() {
        setVisible(false);
    }

    public void await() {
        Util.await(latch);
    }

    public interface Listener {
        default void resize(int width, int height, @NotNull View view) {}

        default void mousePressed(int x, int y, int button, @NotNull View view) {}

        default void mouseReleased(int x, int y, int button, @NotNull View view) {}

        default void mouseMove(int x, int y, @NotNull View view) {}

        default void mouseDrag(int x, int y, int modifiers, @NotNull View view) {}

        default void keyPressed(int code, int modifiers, @NotNull View view) {}

        default void keyReleased(int code, int modifiers, @NotNull View view) {}
    }
}
