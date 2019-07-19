package jdjf.quadTree;

/**
 * Created by JDJFisher on 9/07/2019.
 */
public class OctTree
{
    private static final int CHUNK_SIZE = 2048;

    private int size;
    private int width;
    private int height;
    private int depth;
    private int points;
    private int nodes;
    private OTNode root;

    // empty constructor
    public OctTree(int width, int height, int depth)
    {
        this.width = width;
        this.height = height;
        this.depth = depth;
        points = 0;
        nodes = 1;
        size = 1;

        while (size < Math.max(Math.max(width, height), depth)) size <<= 1;

        root = new OTNode();
    }

    // pixel constructor
    public OctTree(boolean[] pixels, int width, int height, int depth)
    {
        this(width, height, depth);

        if (pixels.length != width * height * depth) throw new RuntimeException();

        for (int i = 0; i < depth; i+= CHUNK_SIZE)
        {
            for (int j = 0; j < height; j += CHUNK_SIZE)
            {
                for (int k = 0; k < width; k += CHUNK_SIZE)
                {
                    // iterate over chunk index range
                    for (int z = i; z < i + CHUNK_SIZE && z < depth; z++)
                    {
                        for (int y = j; y < j + CHUNK_SIZE && y < height; y++)
                        {
                            for (int x = k; x < k + CHUNK_SIZE && x < width; x++)
                            {
                                if (pixels[x + (y * width) + (z * width * height)]) addPoint(x, y, z);
                            }
                        }
                    }

                    // optimise chunk before moving on
                    optimise();
                }
            }
        }

        optimise();
    }

//    // point constructor
//    public jdjf.quadTree.OctTree(ArrayList<Point<Integer>> points, int width, int height, int depth)
//    {
//        this(width, height, depth);
//
//        for (Point<> p : points)
//        {
//            addPoint(p.getX(), p.getY(), p.getZ());
//        }
//
//        optimise();
//    }

    // copy constructor
    public OctTree(OctTree ot)
    {
        this.width = ot.width;
        this.height = ot.height;
        this.depth = ot.depth;
        this.size = ot.size;
        this.points = ot.points;
        this.nodes = ot.nodes;

        root = new OTNode(ot.root);
    }

    // determine whether there is a point stored at the specified coordinates
    public boolean isPoint(int x, int y, int z)
    {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth) throw new RuntimeException();

        return isPoint(root, x, y, z, size, 0, 0, 0);
    }

    private boolean isPoint(OTNode node, int x, int y, int z, int size, int minX, int minY, int minZ)
    {
        // recursively select sub-quadrants until the leaf node encoding the coordinate pair is found
        if (node.isDivided())
        {
            final int halfSize = size / 2;
            final int midX = minX + halfSize;
            final int midY = minY + halfSize;
            final int midZ = minZ + halfSize;

            if (x < midX && y < midY && z < midZ)
            {
                return isPoint(node.lnw, x, y, z, halfSize, minX, minY, minZ);
            }
            else if (x >= midX && y < midY && z < midZ)
            {
                return isPoint(node.lne, x, y, z, halfSize, midX, minY, minZ);
            }
            else if (x < midX && y >= midY && z < midZ)
            {
                return isPoint(node.lsw, x, y, z, halfSize, minX, midY, minZ);
            }
            else if (x >= midX && y >= midY && z < midZ)
            {
                return isPoint(node.lse, x, y, z, halfSize, midX, midY, minZ);
            }
            else  if (x < midX && y < midY && z >= midZ)
            {
                return isPoint(node.unw, x, y, z, halfSize, minX, minY, midZ);
            }
            else if (x >= midX && y < midY && z >= midZ)
            {
                return isPoint(node.une, x, y, z, halfSize, midX, minY, midZ);
            }
            else if (x < midX && y >= midY && z >= midZ)
            {
                return isPoint(node.usw, x, y, z, halfSize, minX, midY, midZ);
            }
            else
            {
                return isPoint(node.use, x, y, z, halfSize, midX, midY, midZ);
            }
        }

        // return leaf value
        return node.coloured;
    }

    // add a point to the structure
    public void addPoint(int x, int y, int z)
    {
        setPoint(x, y, z, true);
    }

    // remove a point from the structure
    public void removePoint(int x, int y, int z)
    {
        setPoint(x, y, z, false);
    }

    // set the point state for a given coordinate pair
    public void setPoint(int x, int y, int z, boolean b)
    {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth) throw new RuntimeException();

        setPoint(root, x, y, z, b, size, 0, 0, 0);
    }

    private void setPoint(OTNode node, int x, int y, int z, boolean b, int size, int minX, int minY, int minZ)
    {
        if (node.isLeaf())
        {
            if (node.coloured == b) return;

            if (size == 1)
            {
                node.coloured = b;
                points += b ? 1 : -1;
                return;
            }

            node.subDivide();
            nodes += 8;
        }

        final int halfSize = size / 2;
        final int midX = minX + halfSize;
        final int midY = minY + halfSize;
        final int midZ = minZ + halfSize;

        if (x < midX && y < midY && z < midZ)
        {
            setPoint(node.lnw, x, y, z, b, halfSize, minX, minY, minZ);
        }
        else if (x >= midX && y < midY && z < midZ)
        {
            setPoint(node.lne, x, y, z, b, halfSize, midX, minY, minZ);
        }
        else if (x < midX && y >= midY && z < midZ)
        {
            setPoint(node.lsw, x, y, z, b, halfSize, minX, midY, minZ);
        }
        else if (x >= midX && y >= midY && z < midZ)
        {
            setPoint(node.lse, x, y, z, b, halfSize, midX, midY, minZ);
        }
        else  if (x < midX && y < midY && z >= midZ)
        {
            setPoint(node.unw, x, y, z, b, halfSize, minX, minY, midZ);
        }
        else if (x >= midX && y < midY && z >= midZ)
        {
            setPoint(node.une, x, y, z, b, halfSize, midX, minY, midZ);
        }
        else if (x < midX && y >= midY && z >= midZ)
        {
            setPoint(node.usw, x, y, z, b, halfSize, minX, midY, midZ);
        }
        else
        {
           setPoint(node.use, x, y, z, b, halfSize, midX, midY, midZ);
        }
    }

    // optimise the jdjf.quadTree.OctTree by merging sub-sectors encoding a uniform value
    public void optimise()
    {
        optimise(root);
    }

    private void optimise(OTNode node)
    {
        if (node.isDivided())
        {
            // attempt to optimise sub-quadrants first
            optimise(node.lnw);
            optimise(node.lne);
            optimise(node.lsw);
            optimise(node.lse);
            optimise(node.unw);
            optimise(node.une);
            optimise(node.usw);
            optimise(node.use);

            // if all the sub-quadrants are equivalent, dispose of them
            if (
                node.lnw.equals(node.lne) && node.lne.equals(node.lsw) && node.lsw.equals(node.lse) && node.lse.equals(node.unw) &&
                node.unw.equals(node.une) && node.une.equals(node.usw) && node.usw.equals(node.use)
               )
            {
                node.coloured = node.unw.coloured;

                // destroy the redundant sub-sectors
                node.lnw = node.lne = node.lsw = node.lse = node.unw = node.une = node.usw = node.use = null;
                nodes -= 8;
            }
        }
    }

    private void recountNodes()
    {
        nodes = 1;
        recountNodes(root);
    }

    private void recountNodes(OTNode node)
    {
        if (node.isDivided())
        {
            nodes += 8;
            recountNodes(node.lnw);
            recountNodes(node.lne);
            recountNodes(node.lsw);
            recountNodes(node.lse);
            recountNodes(node.unw);
            recountNodes(node.une);
            recountNodes(node.usw);
            recountNodes(node.use);
        }
    }

    private void recountPoints()
    {
        points = 0;
        recountPoints(root, size);
    }

    private void recountPoints(OTNode node, int size)
    {
        if (node.isDivided())
        {
            final int halfSize = size / 2;

            recountPoints(node.lnw, halfSize);
            recountPoints(node.lne, halfSize);
            recountPoints(node.lsw, halfSize);
            recountPoints(node.lse, halfSize);
            recountPoints(node.unw, halfSize);
            recountPoints(node.une, halfSize);
            recountPoints(node.usw, halfSize);
            recountPoints(node.use, halfSize);
        }
        else if (node.coloured)
        {
            points += size * size * size;
        }
    }

    public void clear()
    {
        points = 0;
        nodes = 1;
        root = new OTNode();
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int getDepth()
    {
        return depth;
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

        OctTree ot = (OctTree) o;

        return width == ot.width &&
               height == ot.height &&
               depth == ot.depth &&
               points == ot.points &&
               nodes == ot.nodes &&
               root.equals(ot.root);
    }

    @Override
    public int hashCode()
    {
        int result = size;
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + depth;
        result = 31 * result + points;
        result = 31 * result + nodes;
        result = 31 * result + (root != null ? root.hashCode() : 0);
        return result;
    }
}