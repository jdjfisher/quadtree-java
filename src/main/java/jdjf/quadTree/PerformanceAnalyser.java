package jdjf.quadTree;

//import org.apache.spark.util.SizeEstimator;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume;

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
}
