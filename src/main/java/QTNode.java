/**
 * Created by JDJFisher on 9/07/2019.
 */
public class QTNode
{
    protected boolean colour;          // colour of the quadrant
    protected QTNode nw, ne, sw, se;   // pointers to potential sub-quadrants

    // primary constructor
    public QTNode(QuadTree qt, boolean[] data, int size, int minX, int minY)
    {
        qt.nodes++;

        // an arbitrary data point from the quadrant
        boolean x = data[0];

        for(boolean d : data)
        {
            // if the data to be stored in this quadrant is not uniform, colour this node internal and sub-divide
            if (d != x)
            {
                subDivide(qt, data, size, minX, minY);
                return;
            }
        }

        // the data in the quadrant is uniform, assign an appropriate colour to encode the value
        colour = x;

        // add the quadrants points to the total count
        if (colour)
        {
            qt.points += size * size;
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
    protected void subDivide(QuadTree qt, boolean[] data, int size, int minX, int minY)
    {
        final int halfSize = size / 2;
        final int k = halfSize * halfSize;

        // init sub-array for the quadrant data
        boolean[] nwData = new boolean[k];
        boolean[] neData = new boolean[k];
        boolean[] swData = new boolean[k];
        boolean[] seData = new boolean[k];

        // copy across the nw quadrant data
        for (int x = 0; x < halfSize; x++)
        {
            for (int y = 0; y < halfSize; y++)
            {
                final int i = x + y * halfSize;

                nwData[i] = data[x + y * size];
                neData[i] = data[x + halfSize + y * size];
                swData[i] = data[x + (y + halfSize) * size];
                seData[i] = data[x + halfSize + (y + halfSize) * size];
            }
        }

        // create nodes for sub-quadrants
        nw = new QTNode(qt, nwData, halfSize, minX           , minY           );
        ne = new QTNode(qt, neData, halfSize, minX + halfSize, minY           );
        sw = new QTNode(qt, swData, halfSize, minX           , minY + halfSize);
        se = new QTNode(qt, seData, halfSize, minX + halfSize, minY + halfSize);
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

        if (nw.isLeaf() && node.isLeaf()) return colour == node.colour;

        return nw.isDivided() &&
                node.nw.isDivided()  &&
                nw.equals(node.nw) &&
                ne.equals(node.ne) &&
                sw.equals(node.sw) &&
                se.equals(node.se);
    }

    @Override
    public int hashCode()
    {
        int result = (colour ? 1 : 0);
        result = 31 * result + (nw != null ? nw.hashCode() : 0);
        result = 31 * result + (ne != null ? ne.hashCode() : 0);
        result = 31 * result + (sw != null ? sw.hashCode() : 0);
        result = 31 * result + (se != null ? se.hashCode() : 0);
        return result;
    }
}