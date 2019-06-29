package apollon.dynamics.data;

import apollon.util.Util;
import org.junit.Assert;
import org.junit.Test;

public class DataSourceTest {
    @Test
    public void testLorentz() {
        double dt = .001;
        DataSource dataSource = DataSource.lorenz(10, 28, 8. / 3);
        double[][] data = dataSource.createData(dt, 100, new double[]{-8, 8, 27});
        Util.plot3D(data);
        Noise.gaussian(.01).apply(data);
        Util.plot3D(data);
        double[][] derivative = DataSource.createDerivative(dt, data);
        for (int i = 0; i < data.length - 1; i++) {
            double[] current = data[i];
            double[] next = data[i + 1];
            double[] dx = derivative[i];
            for (int j = 0; j < current.length; j++) {
                Assert.assertEquals(next[j] - current[j], dt * dx[j], 1e-10);
            }
        }
    }

    @Test
    public void testGolfBall2D() {
        DataSource dataSource = DataSource.goldBall2D(-9.81);
        double[][] data = dataSource.createData(.01, Double.MAX_VALUE, new double[]{0, 1, 10, 10}, state -> state[1] > 0);
        Util.plot2D(data);
    }

    @Test
    public void testGolfBall3D() {
        DataSource dataSource = DataSource.goldBall3D(-9.81);
        double[][] data = dataSource.createData(.01, Double.MAX_VALUE, new double[]{0, 0, 1, 10, 0, 10}, state -> state[2] > 0);
        Util.plot3D(data);
    }
}