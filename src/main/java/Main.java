import com.sun.javafx.css.Size;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
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
         QuadTree qt = loadQuadTree("blob.png");
    }

    public static QuadTree loadQuadTree(String name)
    {
        PixelData data = loadPixelData(name);
        return new QuadTree(data.pixels, data.width, data.height);

    }

    public static PixelData loadPixelData(String name)
    {
        try
        {
            BufferedImage bi = ImageIO.read(Main.class.getResource("/images/" + name));
//            BufferedImage bi = ImageIO.read(new File("./src/main/resources/images/" + name));
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
            BufferedImage bi = ImageIO.read(Main.class.getResource("/images/" + name));
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
        exportQuadTree(qt, name, false);
    }

    public static void exportQuadTree(QuadTree qt, String name, boolean p)
    {
        try
        {
            BufferedImage bi = new BufferedImage(p ? qt.getSize() : qt.getWidth(), p ? qt.getSize() : qt.getHeight(), BufferedImage.TYPE_INT_ARGB);
            qt.draw(bi.getGraphics(), 1);
            ImageIO.write(bi, "png", new File("./src/main/resources/quadTrees/" + name));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}