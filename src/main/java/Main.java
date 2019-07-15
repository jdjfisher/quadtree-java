import javax.imageio.ImageIO;
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

//        QuadTree qt = loadQuadTree("test.png");
//
//        Show.quadTree(qt);
//
//        qt.setPoint(23, 23, true);
//
//        Show.quadTree(qt);
//
//        qt.optimise();
//
//        Show.quadTree(qt);



//        Data data = loadData("bigBlobs.png");
//
//        System.out.println("loaded");
//
//        QuadTree qt = new QuadTree(data.width, data.height);
//
//        long a = System.currentTimeMillis();
//
//        int k = 1000;
//        for (int i = 0; i < data.width; i+= k)
//        {
//            for (int j = 0; j < data.height; j+= k)
//            {
//                for (int x = i; x < Math.min(i + k, data.width); x++)
//                {
//                    for (int y = j; y < Math.min(j + k, data.width); y++)
//                    {
//                        if (data.pixels[x + y * data.width]) qt.addPoint(x, y);
//                    }
//                }
//
//                qt.optimise();
//            }
//        }
//
//        qt.optimise();
//
//        long b = System.currentTimeMillis();
//
//        System.out.println("set: " + (b-a)/1000f);
//
//        exportQuadTree(qt, "jeff.png");
    }

    public static QuadTree loadQuadTree(String name)
    {
        Data data = loadData(name);
        return new QuadTree(data.pixels, data.width, data.height);
    }

    public static Data loadData(String name)
    {
        Data data = null;

        try {

            BufferedImage image = ImageIO.read(new File("./src/main/resources/" + name));
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
            qt.draw(bi.getGraphics(), 1);
            ImageIO.write(bi, "png", new File("./src/main/resources/" + name));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
