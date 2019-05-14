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
        checkMax(-1, values, index -> false);
        checkMax(0, values, index -> values[index] < 1);
        checkMax(1, values, index -> values[index] < 2);
        checkMax(5, values, index -> values[index] < 6);
        checkMax(6, values, index -> true);

        int[] two = new int[]{0, 1};
        checkMax(-1, two, index -> false);
        checkMax(0, two, index -> two[index] < 1);
        checkMax(1, two, index -> true);

        int[] one = new int[1];
        checkMax(-1, one, index -> false);
        checkMax(0, one, index -> true);

        checkMax(-1, new int[0], index -> false);
    }

    private void checkMax(int index, @NotNull int[] array, @NotNull IntPredicate predicate) {
        Assert.assertEquals(index, GeometryUtil.findMax(array.length, predicate));
        Assert.assertEquals(index, GeometryUtil.findMax(ArrayUtils.toObject(array), predicate::test));
        Assert.assertEquals(index, GeometryUtil.findMax(GeometryUtil.toList(array), predicate::test));
    }

    @Test
    public void testFindMin() {
        int[] values = new int[]{0, 1, 2, 3, 4, 5, 6};
        checkMin(-1, values, index -> false);
        checkMin(6, values, index -> values[index] > 5);
        checkMin(5, values, index -> values[index] > 4);
        checkMin(1, values, index -> values[index] > 0);
        checkMin(0, values, index -> true);

        int[] two = new int[]{0, 1};
        checkMin(-1, two, index -> false);
        checkMin(1, two, index -> two[index] > 0);
        checkMin(0, two, index -> true);

        int[] one = new int[1];
        checkMin(-1, one, index -> false);
        checkMin(0, one, index -> true);

        checkMin(-1, new int[0], index -> false);
    }

    private void checkMin(int index, @NotNull int[] array, @NotNull IntPredicate predicate) {
        Assert.assertEquals(index, GeometryUtil.findMin(array.length, predicate));
        Assert.assertEquals(index, GeometryUtil.findMin(ArrayUtils.toObject(array), predicate::test));
        Assert.assertEquals(index, GeometryUtil.findMin(GeometryUtil.toList(array), predicate::test));
    }
}
