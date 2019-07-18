import java.awt.*;
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
    private int points;
    private int nodes;
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
                // iterate over chunk index range
                for (int y = i; y < i + CHUNK_SIZE && y < height; y++)
                {
                    for (int x = j; x < j + CHUNK_SIZE && x < width; x++)
                    {
                        if (pixels[x + y * width]) addPoint(x, y);
                    }
                }

                // optimise chunk before moving on
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

    // add a point to the structure
    public void addPoint(int x, int y)
    {
        setPoint(x, y, true);
    }

    // remove a point from the structure
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
        if (node.isLeaf())
        {
            if (node.colour == b) return;

            if (size == 1)
            {
                node.colour = b;
                points += b ? 1 : -1;
                return;
            }

            node.subDivide();
            nodes += 4;
        }

        final int halfSize = size / 2;
        final int midX = minX + halfSize;
        final int midY = minY + halfSize;

        if (x < midX && y < midY)
        {
            setPoint(node.nw, x, y, b, halfSize, minX, minY);
        }
        else if (x >= midX && y < midY)
        {
            setPoint(node.ne, x, y, b, halfSize, midX, minY);
        }
        else if (x < midX && y >= midY)
        {
            setPoint(node.sw, x, y, b, halfSize, minX, midY);
        }
        else
        {
            setPoint(node.se, x, y, b, halfSize, midX, midY);
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

            // if all the sub-quadrants are leaves of the same colour, dispose of them
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

        getPixels(root, pixels, width, height, size, 0, 0);

        return pixels;
    }

    private void getPixels(QTNode node, boolean[] pixels, int qtWidth, int qtHeight, int size, int minX, int minY)
    {
        // if this quadrant encodes no data, search the sub-quadrants for data
        if (node.isDivided())
        {
            final int halfSize = size / 2;

            getPixels(node.nw, pixels, qtWidth, qtHeight, halfSize, minX           , minY           );
            getPixels(node.ne, pixels, qtWidth, qtHeight, halfSize, minX + halfSize, minY           );
            getPixels(node.sw, pixels, qtWidth, qtHeight, halfSize, minX           , minY + halfSize);
            getPixels(node.se, pixels, qtWidth, qtHeight, halfSize, minX + halfSize, minY + halfSize);
        }
        // if the leaf is coloured, colour the pixel array across the index range spanned by the quadrant
        else if (node.colour)
        {
            for (int y = minY; y < minY + size && y < qtHeight; y++)
            {
                for (int x = minX; x < minX + size && x < qtWidth; x++)
                {
                    pixels[x + y * qtWidth] = true;
                }
            }
        }
    }

    // merge points from another Quad Tree into the structure
    public void merge(QuadTree qt)
    {
        QuadTree merged = merge(qt, this);

        width = merged.width;
        height = merged.height;
        size = merged.size;
        points = merged.points;
        nodes = merged.nodes;
        root = merged.root;
    }

    public static QuadTree merge(QuadTree l, QuadTree s)
    {
        if (s.size > l.size) return merge(s, l);

        if (l == s) return l;

        QuadTree result = new QuadTree(l);

        if (result.size == s.size)
        {
            result.root = mergeNodes(result.root, s.root);
        }
        else
        {
            QTNode prevNode = result.root;
            QTNode currNode = prevNode;
            int size = result.size;

            while (size > s.size)
            {
                if (currNode.isLeaf())
                {
                    if (currNode.colour) break;

                    currNode.subDivide();
                }

                prevNode = currNode;
                currNode = currNode.nw;
                size /= 2;
            }

            prevNode.nw = mergeNodes(currNode, s.root);
        }

        result.width = Math.max(l.width, s.width);
        result.height = Math.max(l.height, s.height);
        result.recountPoints();
        result.recountNodes();

        return result;
    }

    private static QTNode mergeNodes(QTNode a, QTNode b)
    {
        if (a.isLeaf()) return new QTNode(a.colour ? a : b);
        if (b.isLeaf()) return new QTNode(b.colour ? b : a);

        QTNode merged = new QTNode();
        merged.nw = mergeNodes(a.nw, b.nw);
        merged.ne = mergeNodes(a.ne, b.ne);
        merged.sw = mergeNodes(a.sw, b.sw);
        merged.se = mergeNodes(a.se, b.se);

        return merged;
    }

    private void recountNodes()
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

    private void recountPoints()
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

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
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