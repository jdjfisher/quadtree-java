import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by JDJFisher on 9/07/2019.
 */
public class QuadTree
{
    protected static int NODES = 0;

    private QTNode root;
    private final int width;
    private final int height;
    protected int points;
    protected int nodes;

    public QuadTree(Data data)
    {
        this(data.points, data.width, data.height);
    }

    public QuadTree(boolean[] data, int width, int height)
    {
        this.width = width;
        this.height = height;
        this.points = 0;

        NODES = 0;
        root = new QTNode(data, 0, 0, width, height, null);
        nodes = NODES;

        // count the initial number of points
        for(boolean x : data)
        {
            if (x) points++;
        }
    }

    public boolean getPoint(int x, int y)
    {
        if(x >= width || y >= height) throw new RuntimeException("");

        return getPoint(root, x, y);
    }

    private boolean getPoint(QTNode node, int x, int y)
    {
        switch (node.colour)
        {
            case Black:
                return true;
            case White:
                return false;
            case Grey:
                if(x < node.minX + node.width / 2 && y < node.minY + node.height / 2)
                {
                    return getPoint(node.nw, x, y);
                }
                else if(x >= node.minX + node.width / 2 && y < node.minY + node.height / 2)
                {
                    return getPoint(node.ne, x, y);
                }
                else if(x >= node.minX + node.width / 2 && y >= node.minY + node.height / 2)
                {
                    return getPoint(node.se, x, y);
                }
                else
                {
                    return getPoint(node.sw, x, y);
                }
        }

        // unreachable statement
        throw new RuntimeException("");
    }

    public void setPoint(int x, int y, boolean b)
    {
        if(x >= width || y >= height) throw new RuntimeException("");

        QTNode node = root;

        // find the leaf quadrant that encodes the data for the specified coordinate
        while(node.colour == QTNode.Colour.Grey)
        {
            if(x < node.minX + node.width / 2 && y < node.minY + node.height / 2)
            {
                node = node.nw;
            }
            else if(x >= node.minX + node.width / 2 && y < node.minY + node.height / 2)
            {
                node = node.ne;
            }
            else if(x >= node.minX + node.width / 2 && y >= node.minY + node.height / 2)
            {
                node = node.se;
            }
            else
            {
                node = node.sw;
            }
        }

        // if quadrant already encodes the appropriate value return
        if(node.colour == (b ? QTNode.Colour.Black : QTNode.Colour.White)) return;

        // modify the number of points
        points += b ? 1 : -1;

        if(node.width == 1 && node.height == 1)
        {
            // recolour the quadrant
            node.colour = b ? QTNode.Colour.Black : QTNode.Colour.White;

            // ask parent to check whether all children are now uniform
            node = node.parent;

            while (node != null && node.nw.colour == node.ne.colour && node.ne.colour == node.se.colour && node.se.colour == node.sw.colour)
            {
                node.colour = node.nw.colour;
                nodes -= 4;
                node = node.parent;
            }
        }
        else
        {
            // create a quadrant sized empty array
            boolean[] array = new boolean[node.width * node.height];

            // set the arrays data to match the quadrants original colour
            if (node.colour == QTNode.Colour.Black) Arrays.fill(array, Boolean.TRUE);

            // modify the specified coordinate
            array[(x - node.minX) + (y - node.minY) * node.width] = b;

            // subdivide the quadrant
            node.subDivide(array);
        }
    }

    public Data extractData()
    {
        boolean[] data = new boolean[width * height];

        root.extractData(data, width, height);

        return new Data(data, width, height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public QTNode getRoot() {
        return root;
    }

    // this function is very experimental currently
    public ArrayList<Point> getPointsNear(int x, int y, int r)
    {
        ArrayList<Point> points = new ArrayList<Point>();

        QTNode node = root;

        return points;
    }

    // this function is very experimental currently
    public ArrayList<Point> edgeCoordinates()
    {
        ArrayList<Point> points = new ArrayList<Point>();

        QTNode node = root;

        return points;
    }
}
