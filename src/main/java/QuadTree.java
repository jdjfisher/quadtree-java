import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/**
 * Created by JDJFisher on 9/07/2019.
 */
public class QuadTree
{
    private static final int CHUNK_SIZE = 2048;

    private int size;
    private int width;
    private int height;
    protected int points;
    protected int nodes;
    private QTNode root;

    // empty constructor
    public QuadTree(int width, int height)
    {
        this.width = width;
        this.height = height;
        points = 0;
        nodes = 1;

        size = 1;
        while (size < Math.max(width, height)) size <<= 1;

        root = new QTNode();
    }

    // pixel constructor
    public QuadTree(boolean[] pixels, int width, int height)
    {
        this(width, height);

        if (pixels.length != width * height) throw new RuntimeException();

        for (int i = 0; i < height; i+= CHUNK_SIZE)
        {
            for (int j = 0; j < width; j+= CHUNK_SIZE)
            {
                for (int y = i; y < i + CHUNK_SIZE && y < height; y++)
                {
                    for (int x = j; x < j + CHUNK_SIZE && x < width; x++)
                    {
                        if (pixels[x + y * width]) addPoint(x, y);
                    }
                }

                optimise();
            }
        }

        optimise();
    }

    // copy constructor
    public QuadTree(QuadTree qt)
    {
        this.width = qt.width;
        this.height = qt.height;
        this.size = qt.size;
        this.points = qt.points;
        this.nodes = qt.nodes;

        root = new QTNode(qt.root);
    }

    // determine whether there is a point stored at the specified coordinates
    public boolean isPoint(int x, int y)
    {
        if (x < 0 || y < 0 || x >= width || y >= height) throw new RuntimeException();

        return isPoint(root, x, y, size, 0, 0);
    }

    private boolean isPoint(QTNode node, int x, int y, int size, int minX, int minY)
    {
        // recursively select sub-quadrants until the leaf node encoding the coordinate pair is found
        if (node.isDivided())
        {
            final int halfSize = size / 2;

            if (x < minX + halfSize && y < minY + halfSize)
            {
                return isPoint(node.nw, x, y, halfSize, minX, minY);
            }
            else if (x >= minX + halfSize && y < minY + halfSize)
            {
                return isPoint(node.ne, x, y, halfSize, minX + halfSize, minY);
            }
            else if (x < minX + halfSize && y >= minY + halfSize)
            {
                return isPoint(node.sw, x, y, halfSize, minX, minY + halfSize);
            }
            else
            {
                return isPoint(node.se, x, y, halfSize, minX + halfSize, minY + halfSize);
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

        setPoint(root, x, y, b, size, 0, 0);
    }

    private void setPoint(QTNode node, int x, int y, boolean b, int size, int minX, int minY)
    {
        if (size == 1)
        {
            node.colour = b;
            points += b ? 1 : -1;
        }
        else
        {
            if (node.isLeaf())
            {
                if (node.colour == b) return;

                node.subDivide();
                nodes += 4;
            }

            final int halfSize = size / 2;
            final int midX = minX + halfSize;
            final int midY = minY + halfSize;

            if (x < midX && y < midY)
            {
                setPoint(node.nw, x, y, b, halfSize, minX, minY);
            } else if (x >= midX && y < midY)
            {
                setPoint(node.ne, x, y, b, halfSize, midX, minY);
            } else if (x < midX && y >= midY)
            {
                setPoint(node.sw, x, y, b, halfSize, minX, midY);
            } else
            {
                setPoint(node.se, x, y, b, halfSize, midX, midY);
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
                node.nw = node.ne = node.sw = node.se = null;
                nodes -= 4;
            }
        }
    }

    // export the QuadTress points into an array of pixels
    public boolean[] getPixels()
    {
        boolean[] pixels = new boolean[width * height];

        getPixels(root, pixels, width, size, 0, 0);

        return pixels;
    }

    private void getPixels(QTNode node, boolean[] pixels, int qtWidth, int size, int minX, int minY)
    {
        // if this quadrant encodes no data, extract data from the sub-quadrants
        if (node.isDivided())
        {
            final int halfSize = size / 2;

            getPixels(node.nw, pixels, qtWidth, halfSize, minX           , minY           );
            getPixels(node.ne, pixels, qtWidth, halfSize, minX + halfSize, minY           );
            getPixels(node.sw, pixels, qtWidth, halfSize, minX           , minY + halfSize);
            getPixels(node.se, pixels, qtWidth, halfSize, minX + halfSize, minY + halfSize);
        }
        else
        {
            // populate the pixel array with the value of the leaf across the index range spanned by the quadrant
            for (int y = 0; y < size; y++)
            {
                for (int x = 0; x < size; x++)
                {
                    pixels[minX + minY * qtWidth + x + y * qtWidth] = node.colour;
                }
            }
        }
    }

//    public void merge(QuadTree qt)
//    {
//        if (qt == this) return;
//
//        QuadTree qtL = size > qt.size ? this : qt;
//        QuadTree qtS = size > qt.size ? qt : this;
//
//        QTNode nodeL = qtL.root;
//        QTNode nodeS = qtS.root;
//
//        int sizeL = qtL.size;
//        int sizeS = qtS.size;
//
//        root = mergeNodes(nodeL, sizeL, nodeS, sizeS);
//
//        countNodes();
//
//        size = Math.max(size, qt.size);
//        width = Math.max(width, qt.width);
//        height = Math.max(height, qt.height);
//    }

//    private QTNode mergeNodes(QTNode nodeA, int sizeA, QTNode nodeB, int sizeB)
//    {
//        if ((nodeA.isLeaf() && nodeA.colour) || (nodeB.isLeaf() && !nodeB.colour)) return nodeA;
//
//        if (sizeA > sizeB && nodeA.isDivided()) return mergeNodes(nodeA.nw, sizeA/2, nodeB, sizeB);
//
//        if (sizeA == sizeB && nodeB.isLeaf() && nodeB.colour) return nodeB;
//
//        if (sizeA == sizeB && nodeA.isDivided() && nodeB.isDivided())
//        {
//            final int halfSize = sizeA / 2;
//
//            nodeA.nw = mergeNodes(nodeA.nw, halfSize, nodeB.nw, halfSize);
//            nodeA.ne = mergeNodes(nodeA.ne, halfSize, nodeB.ne, halfSize);
//            nodeA.sw = mergeNodes(nodeA.sw, halfSize, nodeB.sw, halfSize);
//            nodeA.se = mergeNodes(nodeA.se, halfSize, nodeB.se, halfSize);
//        }
//        else
//        {
//            boolean[] data = new boolean[sizeA * sizeA];
//            extractPixels(nodeB, data, sizeA, sizeB, 0, 0);
//
//            nodeA.subDivide(this, data, sizeA, 0, 0);
//        }
//
//        return nodeA;
//    }

    protected void recountNodes()
    {
        nodes = 1;
        recountNodes(root);
    }

    private void recountNodes(QTNode node)
    {
        if (node.isDivided())
        {
            nodes += 4;
            recountNodes(node.nw);
            recountNodes(node.ne);
            recountNodes(node.sw);
            recountNodes(node.se);
        }
    }

    protected void recountPoints()
    {
        points = 0;
        recountPoints(root, size);
    }

    private void recountPoints(QTNode node, int size)
    {
        if (node.isDivided())
        {
            final int halfSize = size / 2;

            recountPoints(node.nw, halfSize);
            recountPoints(node.ne, halfSize);
            recountPoints(node.sw, halfSize);
            recountPoints(node.se, halfSize);
        }
        else if (node.colour)
        {
            points += size * size;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSize()
    {
        return size;
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

    @Override
    public int hashCode()
    {
        int result = size;
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + points;
        result = 31 * result + nodes;
        result = 31 * result + (root != null ? root.hashCode() : 0);
        return result;
    }

    public void draw(Graphics g, int sf)
    {
        draw(g, root, sf, size, 0, 0);
    }

    private void draw(Graphics g, QTNode node, int sf, int size, int minX, int minY)
    {
        if (node.isDivided())
        {
            final int halfSize = size / 2;

            draw(g, node.nw, sf, halfSize, minX           , minY           );
            draw(g, node.ne, sf, halfSize, minX + halfSize, minY           );
            draw(g, node.sw, sf, halfSize, minX           , minY + halfSize);
            draw(g, node.se, sf, halfSize, minX + halfSize, minY + halfSize);
        }
        else
        {
            g.setColor(node.colour ? Color.BLACK : Color.WHITE);
            g.fillRect(sf * minX, sf * minY, sf * size, sf * size);
            g.setColor(Color.RED);
            g.drawRect(sf * minX, sf * minY, sf * size, sf * size);
        }
    }
}