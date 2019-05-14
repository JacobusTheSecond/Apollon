package apollon;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.IntPredicate;

public class GeometryUtilTest {
    @Test
    public void testFindMax() {
        int[] values = new int[]{0, 1, 2, 3, 4, 5, 6};
        check(-1, values, index -> false);
        check(0, values, index -> values[index] < 1);
        check(1, values, index -> values[index] < 2);
        check(5, values, index -> values[index] < 6);
        check(6, values, index -> true);

        int[] two = new int[]{0, 1};
        check(-1, two, index -> false);
        check(0, two, index -> two[index] < 1);
        check(1, two, index -> true);

        int[] one = new int[1];
        check(-1, one, index -> false);
        check(0, one, index -> true);

        check(-1, new int[0], index -> false);
    }

    private void check(int index, @NotNull int[] array, @NotNull IntPredicate predicate) {
        Assert.assertEquals(index, GeometryUtil.findMax(array.length, predicate));
        Assert.assertEquals(index, GeometryUtil.findMax(ArrayUtils.toObject(array), predicate::test));
        Assert.assertEquals(index, GeometryUtil.findMax(GeometryUtil.toList(array), predicate::test));
        for (int i = 0; i < array.length; i++) {
            Assert.assertEquals(index, GeometryUtil.findMax(i, array.length, predicate));
        }
    }
}
