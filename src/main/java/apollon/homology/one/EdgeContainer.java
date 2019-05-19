package apollon.homology.one;

import org.jetbrains.annotations.NotNull;

public interface EdgeContainer {
    void remove(@NotNull int... edges);

    void replace(int edge, @NotNull int... edges);
}
