package apollon.dynamics.data.theta;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

public class Variables extends ArrayList<String> {
    public Variables(@NotNull String...variables) {
        Collections.addAll(this, variables);
    }
}
