package apollon;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class VanEckTest {
    private final List<Integer> indices = new ArrayList<>();

    @Test
    public void testVanEck() {
        int max = 0;
        int value = 0;
        int newValue;
        for (int i = 0; i < 100000; i++) {
            System.out.print(value + ", ");
            newValue = computeNext(i, value);
            max = Math.max(newValue, max);
            put(i, value);
            value = newValue;
        }
        System.out.println("...");
        System.out.println("Max: " + max);
    }

    private int computeNext(int index, int value) {
        if (indices.size() <= value || indices.get(value) == null) {
            return 0;
        }
        return index - indices.get(value);
    }

    private void put(int index, int value) {
        if (value < indices.size()) {
            indices.set(value, index);
            return;
        }
        for (int i = indices.size(); i < value; i++) {
            indices.add(null);
        }
        indices.add(index);
    }
}
