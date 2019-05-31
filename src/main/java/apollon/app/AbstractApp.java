package apollon.app;

import nu.pattern.OpenCV;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public abstract class AbstractApp implements View.Listener {
    static {
        OpenCV.loadLocally();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception ignored) {}
    }

    private final View view;

    private BufferedImage image;

    public AbstractApp() {
        image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        view = new View(this);
    }

    public AbstractApp(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        view = new View(image, this);
    }

    public void show() {
        view.setVisible(true);
    }

    protected void addMenu(@NotNull JMenu menu) {
        view.addMenu(menu);
    }

    @NotNull
    public View getView() {
        return view;
    }

    @NotNull
    public BufferedImage getImage() {
        return image;
    }

    public void draw(@NotNull Consumer<Graphics> operation) {
        operation.accept(getImage().getGraphics());
        getView().render();
    }

    public void await() {
        getView().await();
    }

    public void close() {
        getView().close();
    }

    public int getWidth() {
        return getImage().getWidth();
    }

    public int getHeight() {
        return getImage().getHeight();
    }

    @Override
    public void resize(int width, int height, @NotNull View view) {
        BufferedImage oldImage = image;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.getGraphics().drawImage(oldImage, 0, 0, null);
        view.setImage(image);
        resized();
    }

    protected void resized() {}

    @NotNull
    protected JMenuItem createCloseMenuItem() {
        JMenuItem item = new JMenuItem("Exit");
        item.setMnemonic('E');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        item.addActionListener(e -> close());
        return item;
    }
}
