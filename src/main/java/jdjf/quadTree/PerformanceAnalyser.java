package jdjf.quadTree;

//import org.apache.spark.util.SizeEstimator;
import java.util.Iterator;
import java.util.TreeSet;

public class PerformanceAnalyser {
    public static void main(String[] args) {
        String name = "blobs.png";

        new PerformanceAnalyser().testPerformance(name);

    }

    void testPerformance(String name) {
        System.out.println("Loading image");
        PixelData data = Main.loadPixelData(name);

        System.out.println("Starting QuadTree compression");
        long startTimeNode = System.currentTimeMillis();
        QuadTree quadTree = new QuadTree(data.pixels, data.width, data.height);
        long endTimeNode = System.currentTimeMillis();

        System.out.println("Starting Point loading");
        long startTimePoint = System.currentTimeMillis();
        try {
            Volume volume = getVolume(data.pixels, data.width, data.height);
        } catch (IntegerOverflowException e) {
            System.err.println("Too many points!");
        }
        long endTimePoint = System.currentTimeMillis();

//        long estimatedSizeQTNode = SizeEstimator.estimate(new QTNode());
//        long estimatedSizePointInteger = SizeEstimator.estimate(new Point<Integer>(1,1,1));
        long estimatedSizeQTNode = 32; // A node has 1 boolean and 4 QTNode references
        long estimatedSizePointInteger = 40; // A point has 3 integer coordinates

        System.out.println();
        System.out.println("QTNode size = "+estimatedSizeQTNode+" B");
        System.out.println("Number of nodes = "+quadTree.getNodeCount());
        System.out.println("Estimated node size = "+((double) quadTree.getNodeCount()*estimatedSizeQTNode)/1048576d+" MB");
        System.out.println("Total time nodes = "+((double) (endTimeNode-startTimeNode))/(1E3d)+" s");
        System.out.println();
        System.out.println("Point size = "+estimatedSizePointInteger+" B");
        System.out.println("Number of points = "+quadTree.getPointCount());
        System.out.println("Estimated points size = "+((double) quadTree.getPointCount()*estimatedSizePointInteger)/1048576d+" MB");
        System.out.println("Total time points = "+((double) (endTimePoint-startTimePoint))/(1E3d)+" s");

        new Show(quadTree);

    }

    Volume getVolume(boolean[] pixels, int width, int height) throws IntegerOverflowException {
        Volume volume = new Volume(1,1,"",true);

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                if (pixels[x + y * width]) volume.addCoord(x,y,0);

            }
        }

        return volume;

    }

    class Point<T extends Number> implements Comparable<Point<T>> {
        protected T x;
        protected T y;
        protected T z;

        public Point(T x, T y, T z) {
            this.x = x;
            this.y = y;
            this.z = z;

        }

        public T getX() {
            return x;
        }

        public T getY() {
            return y;
        }

        public T getZ() {
            return z;
        }

        @Override
        public int hashCode() {
            int hash = 1;

            hash = 31*hash + x.hashCode();
            hash = 31*hash + y.hashCode();
            hash = 31*hash + z.hashCode();

            return hash;

        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof Point)) return false;

            Point point = (Point) obj;
            return x.equals(point.x )&& y.equals(point.y) && z.equals(point.z);

        }

        public int compareTo(Point<T> point) {
            double x1 = x.doubleValue();
            double x2 = point.getX().doubleValue();
            double y1 = y.doubleValue();
            double y2 = point.getY().doubleValue();
            double z1 = z.doubleValue();
            double z2 = point.getZ().doubleValue();

            if (x1 > x2) {
                return 1;
            } else if (x1 < x2) {
                return -1;
            } else {
                if (y1 > y2) {
                    return 1;
                } else if (y1 < y2) {
                    return -1;
                } else {
                    if (z1 > z2) {
                        return 1;
                    } else if (z1 < z2){
                        return -1;
                    }
                }
            }

            return 0;
        }
    }

    class Volume {
        protected final double dppXY; //Calibration in xy (fixed once declared in constructor)
        protected final double dppZ; //Calibration in z (fixed once declared in constructor)
        protected final String calibratedUnits;
        protected final boolean twoD;

        protected TreeSet<Point<Integer>> points = new TreeSet<Point<Integer>>();

        public Volume(double dppXY, double dppZ, String calibratedUnits, boolean twoD) {
            this.dppXY = dppXY;
            this.dppZ = dppZ;
            this.calibratedUnits = calibratedUnits;
            this.twoD = twoD;
        }

        public Volume addCoord(int xIn, int yIn, int zIn) throws IntegerOverflowException {
            points.add(new Point<Integer>(xIn,yIn,zIn));
            if (points.size() == Integer.MAX_VALUE) throw new IntegerOverflowException("Object too large (Integer overflow).");
            return this;
        }

        public TreeSet<Point<Integer>> getPoints() {
            return points;

        }

        public double getDistPerPxXY() {
            return dppXY;

        }

        public double getDistPerPxZ() {
            return dppZ;

        }

        public String getCalibratedUnits() {
            return calibratedUnits;
        }

        @Override
        public int hashCode() {
            int hash = 1;

            hash = 31*hash + ((Number) dppXY).hashCode();
            hash = 31*hash + ((Number) dppZ).hashCode();
            hash = 31*hash + calibratedUnits.toUpperCase().hashCode();

            for (Point<Integer> point:points) {
                hash = 31*hash + point.hashCode();
            }

            return hash;

        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof Volume)) return false;

            Volume volume2 = (Volume) obj;
            TreeSet<Point<Integer>> points1 = getPoints();
            TreeSet<Point<Integer>> points2 = volume2.getPoints();

            if (points1.size() != points2.size()) return false;

            if (dppXY != volume2.getDistPerPxXY()) return false;
            if (dppZ != volume2.getDistPerPxZ()) return false;
            if (!calibratedUnits.toUpperCase().equals(volume2.getCalibratedUnits().toUpperCase())) return false;

            Iterator<Point<Integer>> iterator1 = points1.iterator();
            Iterator<Point<Integer>> iterator2 = points2.iterator();

            while (iterator1.hasNext()) {
                Point<Integer> point1 = iterator1.next();
                Point<Integer> point2 = iterator2.next();

                if (!point1.equals(point2)) return false;

            }

            return true;

        }


    }

    class IntegerOverflowException extends Exception {
        IntegerOverflowException(String errorMessage) {
            super(errorMessage);
        }
    }
}
