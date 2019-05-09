package apollon;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public abstract class AbstractApp implements View.Listener {
    private final View view;

    private BufferedImage image;

    public AbstractApp(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        view = new View(image, this);
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
        int oldWidth = getWidth();
        int oldHeight = getHeight();
        if (width > oldWidth || height > oldHeight) {
            BufferedImage image = new BufferedImage(Math.max(oldWidth, width), Math.max(oldHeight, height), BufferedImage.TYPE_INT_RGB);
            image.getGraphics().drawImage(getImage(), 0, 0, null);
            this.image = image;
            view.setImage(image);
            resized();
        }
    }

    protected void resized() {}
}
