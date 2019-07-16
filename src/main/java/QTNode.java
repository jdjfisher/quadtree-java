/**
 * Created by JDJFisher on 9/07/2019.
 */
public class QTNode
{
    protected boolean colour;          // colour of the quadrant
    protected QTNode nw, ne, sw, se;   // pointers to potential sub-quadrants

    // primary constructor
    public QTNode(QuadTree qt, boolean[] data, int minX, int minY, int width, int height)
    {
        qt.nodes++;

        // an arbitrary data point from the quadrant
        boolean x = data[0];

        for(boolean d : data)
        {
            // if the data to be stored in this quadrant is not uniform, colour this node internal and sub divide
            if (d != x)
            {
                subDivide(qt, data, minX, minY, width, height);
                return;
            }
        }

        // the data in the quadrant is uniform, assign an appropriate colour to encode the value
        colour = x;

        // add the quadrants points to the total count
        if (colour)
        {
            qt.points += width * height;
        }
    }

    // copy constructor
    public QTNode(QTNode qtNode)
    {
        if (qtNode.isDivided())
        {
            nw = new QTNode(qtNode.nw);
            ne = new QTNode(qtNode.ne);
            sw = new QTNode(qtNode.sw);
            se = new QTNode(qtNode.se);
        }
        else
        {
            colour = qtNode.colour;
        }
    }

    // split the quadrant in to 4 sub-quadrants
    public void subDivide(QuadTree qt, boolean[] data, int minX, int minY, int width, int height)
    {
        // dimensions of sub-quadrants
        final int w2 = width / 2;
        final int w1 = width - w2;
        final int h2 = height / 2;
        final int h1 = height - h2;

        // init sub-array for the nw quadrant data
        boolean[] nwData = new boolean[w1 * h1];

        // copy across the nw quadrant data
        for (int x = 0; x < w1; x++)
        {
            for (int y = 0; y < h1; y++)
            {
                nwData[x + y * w1] = data[x + y * width];
            }
        }

        // create nw node
        nw = new QTNode(qt, nwData, minX, minY, w1, h1);
        se = sw = ne = nw;

        if (w2 != 0)
        {
            boolean[] neData = new boolean[w2 * h1];

            for (int x = 0; x < w2; x++)
            {
                for (int y = 0; y < h1; y++)
                {
                    neData[x + y * w2] = data[x + y * width + w1];
                }
            }

            ne = new QTNode(qt, neData, minX + w1, minY, w2, h1);
        }

        if (h2 != 0)
        {
            boolean[] swData = new boolean[w1 * h2];

            for (int x = 0; x < w1; x++)
            {
                for (int y = 0; y < h2; y++)
                {
                    swData[x + y * w1] = data[x + (y + h1) * width];
                }
            }

            sw = new QTNode(qt, swData, minX, minY + h1, w1, h2);
        }

        if (w2 != 0 && h2 != 0)
        {
            boolean[] seData = new boolean[w2 * h2];

            for (int x = 0; x < w2; x++)
            {
                for (int y = 0; y < h2; y++)
                {
                    seData[x + y * w2] = data[x + (y + h1) * width + w1];
                }
            }

            se = new QTNode(qt, seData, minX + w1, minY + h1, w2, h2);
        }
    }

    // determine whether the node is divided into quadrants
    public boolean isDivided()
    {
        return nw != null;
    }

    // determine whether the node is a leaf
    public boolean isLeaf()
    {
        return nw == null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QTNode node = (QTNode) o;

        if (nw == null && node.nw == null) return colour == node.colour;

        return nw != null &&
               node.nw != null &&
               nw.equals(node.nw) &&
               ne.equals(node.ne) &&
               sw.equals(node.sw) &&
               se.equals(node.se);
    }
}
