package jdjf.quadTree;

import wbif.sjx.common.Object.Point;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

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
        QuadTree qt = new QuadTree();

        for (Point<Integer> point : loadPoints(name))
        {
            qt.add(point);
        }

        qt.optimise();

        return qt;
    }

    public static TreeSet<Point<Integer>> loadPoints(String name)
    {
        try
        {
            BufferedImage bi = ImageIO.read(Main.class.getResource("/images/" + name));
            TreeSet<Point<Integer>> points = new TreeSet<>();

            for (int y = 0; y < bi.getHeight(); y++)
            {
                for (int x = 0; x < bi.getWidth(); x++)
                {
                    if (bi.getRGB(x, y) == -0x1000000) points.add(new Point<>(x, y, 0));
                }
            }

            return points;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void exportQuadTree(QuadTree qt, String name)
    {
        exportQuadTree(qt, name, true);
    }

    public static void exportQuadTree(QuadTree qt, String name, boolean showNodes)
    {
        try
        {
            BufferedImage bi = new BufferedImage(qt.getRootSize() , qt.getRootSize(), BufferedImage.TYPE_INT_ARGB);

            qt.draw(bi.getGraphics(), 1, showNodes);
            ImageIO.write(bi, "png", new File("./src/main/resources/output/" + name));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void exportPoints(TreeSet<Point<Integer>> points, int size, String name)
    {
        try
        {
            BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

            Graphics g = bi.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, size, size);
            g.setColor(Color.BLACK);
            for (Point p : points)
            {
                g.fillRect(p.getX().intValue(), p.getY().intValue(), 1, 1);
            }

            ImageIO.write(bi, "png", new File("./src/main/resources/output/" + name));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}