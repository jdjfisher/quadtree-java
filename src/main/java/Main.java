import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by JDJFisher on 9/07/2019.
 */
public class Main
{
    private static final Random RNG = new Random();

    public static int COUNT = 0;

    public static void main(String[] args)
    {
//        t1();
//        t2();
//        t3();
    }

    public static void t1()
    {
        PixelData data = loadPixelData("bigBlobs.png");
        System.out.println("loaded");
        QuadTree qt = new QuadTree(data.width, data.height);

        final int k = 1000;
        long a = System.currentTimeMillis();
        int opts = 0;

        for (int i = 0; i < data.height; i+= k)
        {
            for (int j = 0; j < data.width; j+= k)
            {
                for (int y = i; y < i + k && y < data.height; y++)
                {
                    for (int x = j; x < j + k && x < data.width; x++)
                    {
                        if (data.pixels[x + y * data.width]) qt.addPoint(x, y);
                    }
                }

                qt.optimise();
                opts++;
            }
        }

        qt.optimise();

        long b = System.currentTimeMillis();

        System.out.println("time (secs): " + (b - a) / 1000f);
        System.out.println("opts: " + opts);
    }

    public static void t2()
    {
        final int size = 5000;
        final int r = 2000;
        QuadTree qt = new QuadTree(size, size);

        long a = System.currentTimeMillis();

        for (int y = -r; y < r; y++)
        {
            for (int x = -r; x < r; x++)
            {
                qt.addPoint(size / 2 + x, size / 2 + y);
            }
        }

        long b = System.currentTimeMillis();

        System.out.println("time (secs): " + (b - a) / 1000f);

        qt.optimise();

        exportQuadTree(qt, "delete.png");
    }

    public static void t3()
    {
        long a = System.currentTimeMillis();

        QuadTree qt = loadQuadTree("bigBlobs.png");

        long b = System.currentTimeMillis();

        System.out.println("time (secs): " + (b - a) / 1000f);
        System.out.println("nodes: " + qt.getNodeCount());
        System.out.println("points: " + qt.getPoints());

        exportQuadTree(qt, "delete.png");
    }

    public static QuadTree loadQuadTree(String name)
    {
        try
        {
            BufferedImage bi = ImageIO.read(new File("./src/main/resources/images/" + name));
            QuadTree qt = new QuadTree(bi.getWidth(), bi.getHeight());

            final int k = 1000;

            for (int i = 0; i < bi.getHeight(); i+= k)
            {
                for (int j = 0; j < bi.getWidth(); j+= k)
                {
                    for (int y = i; y < i + k && y < bi.getHeight(); y++)
                    {
                        for (int x = j; x < i + j && x < bi.getWidth(); x++)
                        {
                            qt.setPoint(x, y, bi.getRGB(x, y) != 0xFFFFFFFF);
                        }
                    }

                    qt.optimise();
                }
            }

            qt.optimise();

            return qt;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static PixelData loadPixelData(String name)
    {
        try
        {
            BufferedImage bi = ImageIO.read(new File("./src/main/resources/images/" + name));
            boolean[] pixels = new boolean[bi.getWidth() * bi.getHeight()];

            for (int y = 0; y < bi.getHeight(); y++)
            {
                for (int x = 0; x < bi.getWidth(); x++)
                {
                    pixels[x + y * bi.getWidth()] = bi.getRGB(x, y) != 0xFFFFFFFF;
                }
            }

            return new PixelData(pixels, bi.getWidth(), bi.getHeight());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static PointData loadPointData(String name)
    {
        try
        {
            BufferedImage bi = ImageIO.read(new File("./src/main/resources/quadTrees/" + name));
            ArrayList<Point> points = new ArrayList<Point>();

            for (int y = 0; y < bi.getHeight(); y++)
            {
                for (int x = 0; x < bi.getWidth(); x++)
                {
                    if (bi.getRGB(x, y) != 0xFFFFFFFF) points.add(new Point(x, y));
                }
            }

            return new PointData(points, bi.getWidth(), bi.getHeight());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void exportQuadTree(QuadTree qt, String name)
    {
        try
        {
            BufferedImage bi = new BufferedImage(qt.getWidth(), qt.getHeight(), BufferedImage.TYPE_INT_ARGB);
            qt.draw(bi.getGraphics(), 1);
            ImageIO.write(bi, "png", new File("./src/main/resources/" + name));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}