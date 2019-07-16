import java.awt.*;
import java.util.Arrays;

/**
 * Created by JDJFisher on 9/07/2019.
 */
public class QuadTree
{
    private final int width;
    private final int height;
    protected int points;
    protected int nodes;
    private QTNode root;

    // primary constructor
    public QuadTree(boolean[] data, int width, int height)
    {
        if (data.length != width * height) throw new RuntimeException();

        this.width = width;
        this.height = height;
        this.points = 0;
        this.nodes = 0;

        root = new QTNode(this, data, 0, 0, width, height);
    }

    // copy constructor
    public QuadTree(QuadTree qt)
    {
        this.width = qt.width;
        this.height = qt.height;
        this.points = qt.points;
        this.nodes = qt.nodes;

        root = new QTNode(qt.root);
    }

    // empty constructor
    public QuadTree(int width, int height)
    {
        this(new boolean[width * height], width, height);
    }

    // determine whether there is a point stored at the specified coordinates
    public boolean isPoint(int x, int y)
    {
        if (x < 0 || y < 0 || x >= width || y >= height) throw new RuntimeException();

        return isPoint(root, 0, 0, width, height, x, y);
    }

    private boolean isPoint(QTNode node, int minX, int minY, int width, int height, int x, int y)
    {
        // recursively select sub-quadrants until the leaf node encoding the coordinate pair is found
        if (node.isDivided())
        {
            // dimensions of sub-quadrants
            final int w2 = width / 2;
            final int w1 = width - w2;
            final int h2 = height / 2;
            final int h1 = height - h2;

            if (x < minX + w1 && y < minY + h1)
            {
                return isPoint(node.nw, minX, minY, w1, h1, x, y);
            }
            else if (x >= minX + w1 && y < minY + h1)
            {
                return isPoint(node.ne, minX + w1, minY, w2, h1, x, y);
            }
            else if (x < minX + w1 && y >= minY + h1)
            {
                return isPoint(node.sw, minX, minY + h1, w1, h2, x, y);
            }
            else
            {
                return isPoint(node.se, minX + w1, minY + h1, w2, h2, x, y);
            }
        }

        // return leaf value
        return node.colour;
    }

    public void addPoint(int x, int y)
    {
        setPoint(x, y, true);
    }

    public void removePoint(int x, int y)
    {
        setPoint(x, y, false);
    }

    // set the point state for a given coordinate pair
    public void setPoint(int x, int y, boolean b)
    {
        if (x < 0 || y < 0 || x >= width || y >= height) throw new RuntimeException();

        setPoint(root, x, y, b, 0, 0, width, height);
    }

    private void setPoint(QTNode node,int x, int y, boolean b, int minX, int minY, int width, int height)
    {
        // recursively select sub-quadrants until the leaf node encoding the coordinate pair is found
        if (node.isDivided())
        {
            // dimensions of sub-quadrants
            final int w2 = width / 2;
            final int w1 = width - w2;
            final int h2 = height / 2;
            final int h1 = height - h2;

            if (x < minX + w1 && y < minY + h1)
            {
                setPoint(node.nw, x, y, b, minX, minY, w1, h1);
            }
            else if (x >= minX + w1 && y < minY + h1)
            {
                setPoint(node.ne, x, y, b, minX + w1, minY, w2, h1);
            }
            else if (x < minX + w1 && y >= minY + h1)
            {
                setPoint(node.sw, x, y, b, minX, minY + h1, w1, h2);
            }
            else
            {
                setPoint(node.se, x, y, b, minX + w1, minY + h1, w2, h2);
            }
        }
        else if (node.colour != b)
        {
            if (width == 1 && height == 1)
            {
                node.colour = b;
                points += b ? 1 : -1;
            }
            else
            {
                // create a quadrant sized empty array
                boolean[] array = new boolean[width * height];

                // set the arrays data to match the quadrants original colour
                if (node.colour) Arrays.fill(array, Boolean.TRUE);

                // modify the specified coordinate
                array[(x - minX) + (y - minY) * width] = b;

                // subdivide the quadrant
                node.subDivide(this, array, minX, minY, width, height);
            }
        }
    }

    // optimise the QuadTree by merging sub-quadrants encoding a uniform value
    public void optimise()
    {
        optimise(root);
    }

    private void optimise(QTNode node)
    {
        if (node.isDivided())
        {
            // attempt to optimise sub-quadrants first
            optimise(node.nw);
            optimise(node.ne);
            optimise(node.sw);
            optimise(node.se);

            // if the sub-quadrants are equivalent leaves, disposes of them
            if (
               node.nw.isLeaf() && node.ne.isLeaf() && node.sw.isLeaf() && node.se.isLeaf() &&
               node.nw.colour == node.ne.colour && node.ne.colour == node.sw.colour && node.sw.colour == node.se.colour
              )
            {
                node.colour = node.nw.colour;

                // destroy the redundant sub-quadrants
                if (node.ne != node.nw) nodes--;
                if (node.sw != node.nw) nodes--;
                if (node.se != node.nw) nodes--;
                nodes--;
                node.nw = node.ne = node.sw = node.se = null;
            }
        }
    }

    // export the QuadTress points into an array of pixels
    public boolean[] extractPixels()
    {
        boolean[] pixels = new boolean[width * height];

        extractPixels(root, pixels, width, 0, 0, width, height);

        return pixels;
    }

    private void extractPixels(QTNode node, boolean[] pixels, int qtWidth, int minX, int minY, int width, int height)
    {
        // if this quadrant encodes no data, extract data from the sub-quadrants
        if (node.isDivided())
        {
            // dimensions of sub-quadrants
            final int w2 = width / 2;
            final int w1 = width - w2;
            final int h2 = height / 2;
            final int h1 = height - h2;

            extractPixels(node.nw, pixels, qtWidth, minX     , minY     , w1, h1);
            extractPixels(node.ne, pixels, qtWidth, minX + w1, minY     , w2, h1);
            extractPixels(node.sw, pixels, qtWidth, minX     , minY + h1, w1, h2);
            extractPixels(node.se, pixels, qtWidth, minX + w1, minY + h1, w2, h2);
        }
        else
        {
            // populate the pixel array with the value of the leaf across the index range spanned by the quadrant
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    pixels[minX + minY * qtWidth + x + y * qtWidth] = node.colour;
                }
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPoints()
    {
        return points;
    }

    public int getNodeCount()
    {
        return nodes;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuadTree qt = (QuadTree) o;

        return width == qt.width &&
               height == qt.height &&
               points == qt.points &&
               nodes == qt.nodes &&
               root.equals(qt.root);
    }

    public void draw(Graphics g, int sf)
    {
        draw(g, root, sf, 0, 0, width, height);
    }

    private void draw(Graphics g, QTNode node, int sf, int minX, int minY, int width, int height)
    {
        if (node.isDivided())
        {
            final int w2 = width / 2;
            final int w1 = width - w2;
            final int h2 = height / 2;
            final int h1 = height - h2;

            draw(g, node.nw, sf, minX     , minY     , w1, h1);
            draw(g, node.ne, sf, minX + w1, minY     , w2, h1);
            draw(g, node.sw, sf, minX     , minY + h1, w1, h2);
            draw(g, node.se, sf, minX + w1, minY + h1, w2, h2);
        }
        else
        {
            g.setColor(node.colour ? Color.BLACK : Color.WHITE);
            g.fillRect(sf * minX, sf * minY, sf * width, sf * height);
            g.setColor(Color.RED);
            g.drawRect(sf * minX, sf * minY, sf * width, sf * height);
        }
    }
}
