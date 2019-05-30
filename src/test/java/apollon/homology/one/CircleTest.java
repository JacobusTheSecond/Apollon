package apollon.homology.one;

import apollon.homology.Circle;
import org.junit.Assert;
import org.junit.Test;

public class CircleTest {
    @Test
    public void testRevalidate() {
        Circle circle = new Circle(1, 2, 5, -6, -6, 5, 3, 4, -4, -3, -2);
        circle.revalidate();
        Assert.assertArrayEquals(new int[]{4}, circle.getEdges());

        circle = new Circle(1, -2);
        circle.revalidate();
        Assert.assertArrayEquals(new int[0], circle.getEdges());

        circle = new Circle(1, -2, 3);
        circle.revalidate();
        Assert.assertArrayEquals(new int[]{3}, circle.getEdges());

        circle = new Circle(1, -2, 3, -4);
        circle.revalidate();
        Assert.assertArrayEquals(new int[0], circle.getEdges());
    }

    @Test
    public void testRemove() {
        Circle circle = new Circle(1, 2, 3, 1, 1, -2, 2, -2, 2, 3, -2);
        circle.remove(1);
        Assert.assertArrayEquals(new int[]{2, 3, 2, 2, 3}, circle.getEdges());
    }

    @Test
    public void testReplace() {
        Circle circle = new Circle(0, 1, 1, 2, -2, 1, 2, -2, -2);
        circle.replace(1, 3, 4);
        Assert.assertArrayEquals(new int[]{0, 3, 4, 3, 4, 2, 2, -5, -4, -5, -4}, circle.getEdges());
    }

    @Test
    public void testSingleEdge() {
        Assert.assertEquals(2, new Circle(1, 2, 3, 4, 1, 3, 4, 5, 6, -5, -6, 6).getSingleEdge());
    }

    @Test
    public void testInverse() {
        Assert.assertArrayEquals(new int[]{-2, -7, 5, 4, -7, -6, -5, -4, -2, -5, -4}, new Circle(1, 2, 3, 4, 1, 3, 4, 5, 6, -5, -6, 6).getInverse(2));
        Assert.assertArrayEquals(new int[]{3, 4, 1, 3, 4, 5, 6, -5, -6, 6, 1}, new Circle(1, 2, 3, 4, 1, 3, 4, 5, 6, -5, -6, 6).getInverse(-2));
    }
}