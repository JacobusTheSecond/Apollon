package apollon.homology.one;

import apollon.util.Pointer;
import org.jetbrains.annotations.NotNull;

public class Site extends Pointer<Site> {
    public Site(int index) {
        super(index);
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
