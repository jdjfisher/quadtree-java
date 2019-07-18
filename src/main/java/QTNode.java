/**
 * Created by JDJFisher on 9/07/2019.
 */
public class QTNode
{
    protected boolean colour;          // colour of the quadrant
    protected QTNode nw, ne, sw, se;   // references to potential sub-quadrants

    public QTNode()
    {
        this.colour = false;
    }

    public QTNode(boolean colour)
    {
        this.colour = colour;
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
    protected void subDivide()
    {
        nw = new QTNode(colour);
        ne = new QTNode(colour);
        sw = new QTNode(colour);
        se = new QTNode(colour);
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