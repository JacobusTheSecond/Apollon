package apollon.homology.one;

import apollon.homology.Circuit;
import org.junit.Assert;
import org.junit.Test;

public class CircuitTest {
    @Test
    public void testRevalidate() {
        Circuit circuit = new Circuit(1, 2, 5, -6, -6, 5, 3, 4, -4, -3, -2);
        circuit.revalidate();
        Assert.assertArrayEquals(new int[]{4}, circuit.getEdges());

        circuit = new Circuit(1, -2);
        circuit.revalidate();
        Assert.assertArrayEquals(new int[0], circuit.getEdges());

        circuit = new Circuit(1, -2, 3);
        circuit.revalidate();
        Assert.assertArrayEquals(new int[]{3}, circuit.getEdges());

        circuit = new Circuit(1, -2, 3, -4);
        circuit.revalidate();
        Assert.assertArrayEquals(new int[0], circuit.getEdges());
    }

    @Test
    public void testRemove() {
        Circuit circuit = new Circuit(1, 2, 3, 1, 1, -2, 2, -2, 2, 3, -2);
        circuit.remove(1);
        Assert.assertArrayEquals(new int[]{2, 3, 2, 2, 3}, circuit.getEdges());
    }

    @Test
    public void testReplace() {
        Circuit circuit = new Circuit(0, 1, 1, 2, -2, 1, 2, -2, -2);
        circuit.replace(1, 3, 4);
        Assert.assertArrayEquals(new int[]{0, 3, 4, 3, 4, 2, 2, -5, -4, -5, -4}, circuit.getEdges());
    }

    @Test
    public void testSingleEdge() {
        Assert.assertEquals(2, new Circuit(1, 2, 3, 4, 1, 3, 4, 5, 6, -5, -6, 6).getSingleEdge());
    }

    @Test
    public void testInverse() {
        Assert.assertArrayEquals(new int[]{-2, -7, 5, 4, -7, -6, -5, -4, -2, -5, -4}, new Circuit(1, 2, 3, 4, 1, 3, 4, 5, 6, -5, -6, 6).getInverse(2));
        Assert.assertArrayEquals(new int[]{3, 4, 1, 3, 4, 5, 6, -5, -6, 6, 1}, new Circuit(1, 2, 3, 4, 1, 3, 4, 5, 6, -5, -6, 6).getInverse(-2));
    }
}