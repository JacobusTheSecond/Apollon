package apollon.app;

import apollon.GeometryUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;

public class View extends JFrame {
    private final CountDownLatch latch = new CountDownLatch(1);

    private final JPanel canvas;

    private Listener listener;

    private Image image;

    private int width;

    private int height;

    public View(@NotNull Image image) {
        this(image, image.getWidth(null), image.getHeight(null));
    }

    public View(@NotNull Image image, int width, int height) {
        this(image, width, height, new Listener() {});
    }

    public View(@NotNull Image image, @NotNull Listener listener) {
        this(image, image.getWidth(null), image.getHeight(null), listener);
    }

    public View(@NotNull Image image, int width, int height, @NotNull Listener listener) {
        super("View");
        this.width = width;
        this.height = height;
        this.listener = listener;
        this.image = image;
        canvas = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g.drawImage(View.this.image, 0, 0, null);
            }
        };

        init();

        setVisible(true);
    }

    private void init() {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.setPreferredSize(new Dimension(width, height));
        container.add(canvas, BorderLayout.CENTER);
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSize(e.getComponent().getWidth(), e.getComponent().getHeight());
            }
        });

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
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                listener.keyPressed(e.getExtendedKeyCode(), View.this);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                listener.keyReleased(e.getExtendedKeyCode(), View.this);
            }
        });
        pack();
    }

    public void setImage(@NotNull BufferedImage image) {
        this.image = image;
    }

    public void setListener(@NotNull Listener listener) {
        this.listener = listener;
    }

    private void updateSize(int width, int height) {
        this.width = width;
        this.height = height;
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
        GeometryUtil.await(latch);
    }

    public interface Listener {
        default void resize(int width, int height, @NotNull View view) {}

        default void mousePressed(int x, int y, int button, @NotNull View view) {}

        default void mouseReleased(int x, int y, int button, @NotNull View view) {}

        default void mouseMove(int x, int y, @NotNull View view) {}

        default void mouseDrag(int x, int y, int modifiers, @NotNull View view) {}

        default void keyPressed(int code, @NotNull View view) {}

        default void keyReleased(int code, @NotNull View view) {}
    }
}
