import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class DubinsPathCalculator {

    private double radius;
    private double pointSeparation;

    public static void main(String[] args) {

        double[] start = {0, 0, 0};
        double[] end = {0, 10, Math.PI / 2};
        double radius = 1.0;
        double stepSize = 0.05;


        DubinsPathCalculator calculator = new DubinsPathCalculator(radius, stepSize);


        double[][] path = calculator.dubinsPath(start, end);


        System.out.println("Points along Dubins Path:");
        for (double[] point : path) {
            System.out.println(  point[0] +" " + point[1]);
        }
    }

    public DubinsPathCalculator(double radius, double pointSeparation) {
        assert radius > 0 && pointSeparation > 0;
        this.radius = radius;
        this.pointSeparation = pointSeparation;
    }


    private static double[] ortho(double[] vect2d) {
        return new double[] {-vect2d[1], vect2d[0]};
    }


    private static double dist(double[] ptA, double[] ptB) {
        return Math.sqrt(Math.pow(ptA[0] - ptB[0], 2) + Math.pow(ptA[1] - ptB[1], 2));
    }
    public List<double[]> allOptions(double[] start, double[] end, boolean sort) {
        double[] center0Left = findCenter(start, 'L');
        double[] center0Right = findCenter(start, 'R');
        double[] center2Left = findCenter(end, 'L');
        double[] center2Right = findCenter(end, 'R');

        List<double[]> options = new ArrayList<>();
        options.add(lsl(start, end, center0Left, center2Left));
        options.add(rsr(start, end, center0Right, center2Right));
        options.add(rsl(start, end, center0Right, center2Left));
        options.add(lsr(start, end, center0Left, center2Right));
        options.add(rlr(start, end, center0Right, center2Right));
        options.add(lrl(start, end, center0Left, center2Left));

        if (sort) {
            options.sort((a, b) -> Double.compare(a[0], b[0]));
        }
        return options;
    }

    public double[][] dubinsPath(double[] start, double[] end) {
        List<double[]> options = allOptions(start, end, true);
        double[] shortestOption = options.get(0);
        double[] dubinsPath = {shortestOption[1], shortestOption[2], shortestOption[3]};
        boolean straight = (int) shortestOption[4] == 1;

        return generatePoints(start, end, dubinsPath, straight);
    }

    private double[][] generatePoints(double[] start, double[] end, double[] dubinsPath, boolean straight) {
        if (straight) {
            return generatePointsStraight(start, end, dubinsPath);
        } else {
            return generatePointsCurve(start, end, dubinsPath);
        }
    }


    private double[] findCenter(double[] point, char side) {
        assert side == 'L' || side == 'R';
        double angle = point[2] + (side == 'L' ? Math.PI / 2 : -Math.PI / 2);
        return new double[] {point[0] + Math.cos(angle) * radius, point[1] + Math.sin(angle) * radius};
    }


    public double[] lsl(double[] start, double[] end, double[] center0, double[] center2) {
        double straightDist = dist(center0, center2);
        double alpha = Math.atan2(center2[1] - center0[1], center2[0] - center0[0]);
        double beta2 = (end[2] - alpha + 2 * Math.PI) % (2 * Math.PI);
        double beta0 = (alpha - start[2] + 2 * Math.PI) % (2 * Math.PI);
        double totalLen = radius * (beta2 + beta0) + straightDist;
        return new double[] {totalLen, beta0, beta2, straightDist, 1};
    }

    public double[] rsr(double[] start, double[] end, double[] center0, double[] center2) {
        double straightDist = dist(center0, center2);
        double alpha = Math.atan2(center2[1] - center0[1], center2[0] - center0[0]);
        double beta2 = (-end[2] + alpha + 2 * Math.PI) % (2 * Math.PI);
        double beta0 = (-alpha + start[2] + 2 * Math.PI) % (2 * Math.PI);
        double totalLen = radius * (beta2 + beta0) + straightDist;
        return new double[] {totalLen, -beta0, -beta2, straightDist, 1};
    }
    public double[] lsr(double[] start, double[] end, double[] center0, double[] center2) {
        double[] medianPoint = {(center2[0] - center0[0]) / 2, (center2[1] - center0[1]) / 2};
        double psia = Math.atan2(medianPoint[1], medianPoint[0]);
        double halfIntercenter = dist(new double[]{0, 0}, medianPoint);
        if (halfIntercenter < radius) {
            return new double[] {Double.POSITIVE_INFINITY, 0, 0, 0, 1};
        }
        double alpha = Math.acos(radius / halfIntercenter);


        double beta0 = (psia - alpha - start[2] + Math.PI / 2 + 2 * Math.PI) % (2 * Math.PI);
        double beta2 = (0.5 * Math.PI - end[2] - alpha + psia + 2 * Math.PI) % (2 * Math.PI);

        double straightDist = 2 * Math.sqrt(Math.pow(halfIntercenter, 2) - Math.pow(radius, 2));
        double totalLen = radius * (beta2 + beta0) + straightDist;
        return new double[] {totalLen, beta0, -beta2, straightDist, 1};
    }


    public double[] rsl(double[] start, double[] end, double[] center0, double[] center2) {
        double[] medianPoint = {(center2[0] - center0[0]) / 2, (center2[1] - center0[1]) / 2};
        double psia = Math.atan2(medianPoint[1], medianPoint[0]);
        double halfIntercenter = dist(new double[]{0, 0}, medianPoint);
        if (halfIntercenter < radius) {
            return new double[] {Double.POSITIVE_INFINITY, 0, 0, 0, 1};
        }
        double alpha = Math.acos(radius / halfIntercenter);
        double beta0 = (-psia - alpha + start[2] + Math.PI / 2 + 2 * Math.PI) % (2 * Math.PI);
        double beta2 = (Math.PI + end[2] - Math.PI / 2 - alpha - psia + 2 * Math.PI) % (2 * Math.PI);
        double straightDist = 2 * Math.sqrt(Math.pow(halfIntercenter, 2) - Math.pow(radius, 2));
        double totalLen = radius * (beta2 + beta0) + straightDist;
        return new double[] {totalLen, -beta0, beta2, straightDist, 1};
    }

public double[] rlr(double[] start, double[] end, double[] center0, double[] center2) {
    double distIntercenter = dist(center0, center2);
    double[] intercenter = {(center2[0] - center0[0]) / 2, (center2[1] - center0[1]) / 2};
    double psia = Math.atan2(intercenter[1], intercenter[0]);
    if (2 * radius < distIntercenter && distIntercenter > 4 * radius) {
        return new double[] {Double.POSITIVE_INFINITY, 0, 0, 0, 0};
    }
    double gamma = 2 * Math.asin(distIntercenter / (4 * radius));


    double beta0 = -((-psia + (start[2] + Math.PI / 2) + (Math.PI - gamma) / 2 + 2 * Math.PI) % (2 * Math.PI));
    double beta1 = -((psia + Math.PI / 2 - end[2] + (Math.PI - gamma) / 2 + 2 * Math.PI) % (2 * Math.PI));

    double totalLen = (2 * Math.PI - gamma + Math.abs(beta0) + Math.abs(beta1)) * radius;
    return new double[] {totalLen, beta0, beta1, 2 * Math.PI - gamma, 0};
}
    public double[] lrl(double[] start, double[] end, double[] center0, double[] center2) {
        double distIntercenter = dist(center0, center2);
        double[] intercenter = {(center2[0] - center0[0]) / 2, (center2[1] - center0[1]) / 2};
        double psia = Math.atan2(intercenter[1], intercenter[0]);
        if (2 * radius < distIntercenter && distIntercenter > 4 * radius) {
            return new double[] {Double.POSITIVE_INFINITY, 0, 0, 0, 0};
        }
        double gamma = 2 * Math.asin(distIntercenter / (4 * radius));
        double beta0 = (psia - start[2] + Math.PI / 2 + (Math.PI - gamma) / 2 + 2 * Math.PI) % (2 * Math.PI);
        double beta1 = (-psia + Math.PI / 2 + end[2] + (Math.PI - gamma) / 2 + 2 * Math.PI) % (2 * Math.PI);
        double totalLen = (2 * Math.PI - gamma + Math.abs(beta0) + Math.abs(beta1)) * radius;
        return new double[] {totalLen, beta0, beta1, 2 * Math.PI - gamma, 0};
    }

    private double[][] generatePointsStraight(double[] start, double[] end, double[] path) {
        double total = radius * (Math.abs(path[1]) + Math.abs(path[0])) + path[2];
        double[] center0 = findCenter(start, path[0] > 0 ? 'L' : 'R');
        double[] center2 = findCenter(end, path[1] > 0 ? 'L' : 'R');

        double[] ini, fin;
        if (Math.abs(path[0]) > 0) {
            double angle = start[2] + (Math.abs(path[0]) - Math.PI / 2) * Math.signum(path[0]);
            ini = new double[] {center0[0] + radius * Math.cos(angle), center0[1] + radius * Math.sin(angle)};
        } else {
            ini = new double[] {start[0], start[1]};
        }

        if (Math.abs(path[1]) > 0) {
            double angle = end[2] + (-Math.abs(path[1]) - Math.PI / 2) * Math.signum(path[1]);
            fin = new double[] {center2[0] + radius * Math.cos(angle), center2[1] + radius * Math.sin(angle)};
        } else {
            fin = new double[] {end[0], end[1]};
        }

        double distStraight = dist(ini, fin);
        List<double[]> points = new ArrayList<>();
        for (double x = 0; x <= total; x += pointSeparation) {
            if (x < Math.abs(path[0]) * radius) {
                points.add(circleArc(start, path[0], center0, x));
            } else if (x > total - Math.abs(path[1]) * radius) {
                points.add(circleArc(end, path[1], center2, x - total));
            } else {
                double coeff = (x - Math.abs(path[0]) * radius) / distStraight;
                points.add(new double[] {coeff * fin[0] + (1 - coeff) * ini[0], coeff * fin[1] + (1 - coeff) * ini[1]});
            }
        }
        points.add(new double[] {end[0], end[1]});
        return points.toArray(new double[0][0]);
    }

    private double[][] generatePointsCurve(double[] start, double[] end, double[] path) {
        double total = radius * (Math.abs(path[1]) + Math.abs(path[0]) + Math.abs(path[2]));
        double[] center0 = findCenter(start, path[0] > 0 ? 'L' : 'R');
        double[] center2 = findCenter(end, path[1] > 0 ? 'L' : 'R');
        double intercenter = dist(center0, center2);


        double[] offsetDirection = ortho(new double[] {(center2[0] - center0[0]) / intercenter, (center2[1] - center0[1]) / intercenter});
        double offsetMagnitude = Math.sqrt(4 * radius * radius - Math.pow(intercenter / 2, 2));
        double[] center1 = {
                (center0[0] + center2[0]) / 2 + Math.signum(path[0]) * offsetDirection[0] * offsetMagnitude,
                (center0[1] + center2[1]) / 2 + Math.signum(path[0]) * offsetDirection[1] * offsetMagnitude
        };

        double psi0 = Math.atan2(center1[1] - center0[1], center1[0] - center0[0]) - Math.PI;

        List<double[]> points = new ArrayList<>();
        for (double x = 0; x <= total; x += pointSeparation) {
            if (x < Math.abs(path[0]) * radius) {
                points.add(circleArc(start, path[0], center0, x));
            } else if (x > total - Math.abs(path[1]) * radius) {
                points.add(circleArc(end, path[1], center2, x - total));
            } else {
                double angle = psi0 - Math.signum(path[0]) * (x / radius - Math.abs(path[0]));
                double[] vect = {Math.cos(angle), Math.sin(angle)};
                points.add(new double[] {center1[0] + radius * vect[0], center1[1] + radius * vect[1]});
            }
        }
        points.add(new double[] {end[0], end[1]});
        return points.toArray(new double[0][0]);
    }


    private double[] circleArc(double[] reference, double beta, double[] center, double x) {
        double angle = reference[2] + ((x / radius) - Math.PI / 2) * Math.signum(beta);
        double[] vect = {Math.cos(angle), Math.sin(angle)};
        return new double[] {center[0] + radius * vect[0], center[1] + radius * vect[1]};
    }
}
