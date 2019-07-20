package jdjf.quadTree;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by JDJFisher on 9/07/2019.
 */
public class Main
{
    public static void main(String[] args)
    {
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
            boolean[] pixels = new boolean[bi.getWidth() * bi.getHeight()];

            for (int y = 0; y < bi.getHeight(); y++)
            {
                for (int x = 0; x < bi.getWidth(); x++)
                {
                    pixels[x + y * bi.getWidth()] = bi.getRGB(x, y) == -0x1000000;
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
            ArrayList<Point> points = new ArrayList<>();

            for (int y = 0; y < bi.getHeight(); y++)
            {
                for (int x = 0; x < bi.getWidth(); x++)
                {
                    if (bi.getRGB(x, y) == -0x1000000) points.add(new Point(x, y));
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
        exportQuadTree(qt, name, false, true);
    }

    public static void exportQuadTree(QuadTree qt, String name, boolean showPadding, boolean showNodes)
    {
        try
        {
            BufferedImage bi = new BufferedImage(
                    showPadding ? qt.getSize() : qt.getWidth(),
                    showPadding ? qt.getSize() : qt.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );

            qt.draw(bi.getGraphics(), 1, showNodes);
            ImageIO.write(bi, "png", new File("./src/main/resources/output/" + name));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void exportPixelData(PixelData data, String name)
    {
        try
        {
            BufferedImage bi = new BufferedImage(data.width, data.height, BufferedImage.TYPE_INT_ARGB);

            Graphics g = bi.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, data.width, data.height);
            g.setColor(Color.BLACK);
            for (int y = 0; y < data.height; y++)
            {
                for (int x = 0; x < data.width; x++)
                {
                    if (data.pixels[x + y * data.width]) g.fillRect(x, y, 1, 1);
                }
            }

            ImageIO.write(bi, "png", new File("./src/main/resources/output/" + name));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void exportPointData(PointData data, String name)
    {
        try
        {
            BufferedImage bi = new BufferedImage(data.width, data.height, BufferedImage.TYPE_INT_ARGB);

            Graphics g = bi.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, data.width, data.height);
            g.setColor(Color.BLACK);
            for (Point p : data.points)
            {
                g.fillRect(p.x, p.y, 1, 1);
            }

            ImageIO.write(bi, "png", new File("./src/main/resources/output/" + name));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}