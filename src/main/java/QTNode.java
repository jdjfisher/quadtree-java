
/**
 * Created by JDJFisher on 9/07/2019.
 */
public class QTNode
{
    protected boolean colour;                // colour of the quadrant
    protected final int width;               // width of the quadrant
    protected final int height;              // height of the quadrant
    protected final int minX;                // minimum x value of the quadrants region
    protected final int minY;                // minimum x value of the quadrants region
    protected final QTNode parent;           // pointer to parent node
    protected QTNode nw, ne, se, sw;         // pointers to potential sub-quadrants

    public QTNode(boolean[] data, int minX, int minY, int width, int height, QTNode parent)
    {
        this.parent = parent;
        this.minX = minX;
        this.minY = minY;
        this.width = width;
        this.height = height;

        // an arbitrary data point from the quadrant
        boolean x = data[0];

        for(boolean d : data)
        {
            // if the data to be stored in this quadrant is not uniform, colour this node internal and sub divide
            if (d != x)
            {
                subDivide(data);
                return;
            }
        }

        // the data in the quadrant is uniform, assign an appropriate colour to encode the value
        colour = x;

        QuadTree.NODES++;
    }

    // split the quadrant in to 4 sub-quadrants
    public void subDivide(boolean[] data)
    {
        // calculate dimensions of sub-quadrants
        int w1 = width / 2;
        int w2 = width - w1;
        int h1 = height / 2;
        int h2 = height - h1;

        // assign sub-arrays for the data
        boolean[] nwData = new boolean[w1 * h1];
        boolean[] neData = new boolean[w2 * h1];
        boolean[] seData = new boolean[w2 * h2];
        boolean[] swData = new boolean[w1 * h2];

        for (int x = 0; x < w1; x++)
        {
            for (int y = 0; y < h1; y++)
            {
                nwData[x + y * w1] = data[x + y *width];
            }
        }

        for (int x = 0; x < w2; x++)
        {
            for (int y = 0; y < h1; y++)
            {
                neData[x + y * w2] = data[x + y *width + w1];
            }
        }

        for (int x = 0; x < w2; x++)
        {
            for (int y = 0; y < h2; y++)
            {
                seData[x + y * w2] = data[x + (y + h1) * width + w1];
            }
        }

        for (int x = 0; x < w1; x++)
        {
            for (int y = 0; y < h2; y++)
            {
                swData[x + y * w1] = data[x + (y + h1) * width];
            }
        }

        // create sub-quadrants
        se = new QTNode(seData, minX + w1, minY + h1, w2, h2, this);
        nw = ne = sw = se;

        if(w1 != 0)
        {
            sw = new QTNode(swData, minX, minY + h1, w1, h2, this);
        }

        if(h1 != 0)
        {
            ne = new QTNode(neData, minX + w1, minY, w2, h1, this);
        }

        if(w1 != 0 && h1 != 0)
        {
            nw = new QTNode(nwData, minX, minY, w1, h1, this);
        }
    }

    public void extractData(boolean[] data, int dataWidth, int dataHeight)
    {
        // if this quadrant encodes no data extract data from the sub-quadrants
        if(isDivided())
        {
            nw.extractData(data, dataWidth, dataHeight);
            ne.extractData(data, dataWidth, dataHeight);
            se.extractData(data, dataWidth, dataHeight);
            sw.extractData(data, dataWidth, dataHeight);
        }
        else
        {
            if(minX + width > dataWidth || minY + height > dataHeight) return;

            // populate the appropriate index range of the data array the quadrants data value
            for (int x = 0; x < width; x++)
            {
                for (int y = 0; y < height; y++)
                {
                    data[minX + minY * dataWidth + x + y * dataWidth] = colour;
                }
            }
        }
    }

    public boolean isDivided()
    {
        return nw != null;
    }
}
