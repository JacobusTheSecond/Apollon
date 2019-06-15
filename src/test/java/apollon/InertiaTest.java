package apollon;

import apollon.util.Util;
import org.junit.Test;

public class InertiaTest {
    @Test
    public void testCylinder() {
        double r = .25;
        double h = .5;
        computeInertia(r, h);
    }

    private void computeInertia(double r, double h) {
        double mass = Math.PI * r * r * h;
        double xy = mass * (3 * r * r + h * h) / 12;
        double z = mass * r * r / 2;
        mass = round(mass);
        xy = round(xy);
        z = round(z);
        System.out.println("<mass value=\"" + mass + "\"/>");
        System.out.println("<inertia ixx=\"" + xy + "\" ixy=\"0\" ixz=\"0\" iyy=\"" + xy + "\" iyz=\"0\" izz=\"" + z + "\"/>");
    }

    private double round(double value) {
        return Util.round(value * 10000) / 10000.;
    }
}
