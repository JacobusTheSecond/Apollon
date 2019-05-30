package apollon.homology;

import apollon.util.Pointer;
import org.jetbrains.annotations.NotNull;

public class Site extends Pointer<Site> {
    private Site component = null;

    public Site(int index) {
        super(index);
    }

    public void setComponent(@NotNull Site component) {
        Site target = component.getComponent();
        Site current = this;
        Site next;
        do {
            next = current.component;
            current.component = target;
        } while (next != null);
    }

    @NotNull
    public Site getComponent() {
        Site current = this;
        while (current.hasComponent()) {
            current = current.component;
        }
        return current;
    }

    public boolean hasComponent() {
        return component != null;
    }

    @NotNull
    @Override
    protected String name() {
        return "Site";
    }

    @NotNull
    @Override
    protected Site getThis() {
        return this;
    }
}
