import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Created by JDJFisher on 9/07/2019.
 */
public class Main
{
    private static final Random rng = new Random();

    public static void main(String[] args)
    {
        Show.quadTree(loadQuadTree("test.png"));
    }

    public static QuadTree loadQuadTree(String name)
    {
        return new QuadTree(loadData(name));
    }

    public static Data loadData(String name)
    {
        Data data = null;

        try {

            BufferedImage image = ImageIO.read(Main.class.getResource(name));
            boolean[] pixels = new boolean[image.getWidth() * image.getHeight()];

            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pixels[x + y * image.getWidth()] = image.getRGB(x, y) != 0xFFFFFFFF;
                }
            }

            data = new Data(pixels, image.getWidth(), image.getHeight());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return data;
    }

    public static void exportQuadTree(QuadTree qt, String name)
    {
        try
        {
            BufferedImage bi = new BufferedImage(qt.getWidth(), qt.getHeight(), BufferedImage.TYPE_INT_ARGB);
            drawNode(bi.getGraphics(), qt.getRoot());
            ImageIO.write(bi, "png", new File("./src/main/resources/" + name));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void drawNode(Graphics g, QTNode node)
    {
        if(node.colour == QTNode.Colour.Grey)
        {
            drawNode(g, node.ne);
            drawNode(g, node.nw);
            drawNode(g, node.se);
            drawNode(g, node.sw);
        }
        else
        {
            g.setColor(node.colour == QTNode.Colour.Black ? Color.BLACK : Color.WHITE);
            g.fillRect(node.minX, node.minY, node.width, node.height);
            g.setColor(Color.RED);
            g.drawRect(node.minX, node.minY, node.width, node.height);
        }
    }
}
