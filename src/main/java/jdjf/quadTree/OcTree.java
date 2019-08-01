package jdjf.quadTree;

import wbif.sjx.common.Object.Point;

import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;

/**
 * Created by JDJFisher on 19/07/2019.
 */
public class OcTree implements Iterable<Point<Integer>>
{
    private static final int CHUNK_SIZE = 2048;

    private int width;
    private int height;
    private int depth;
    private int size;
    private int points;
    private int nodes;
    private OTNode root;

    // empty constructor
    public OcTree(int width, int height, int depth)
    {
        this.width = width;
        this.height = height;
        this.depth = depth;
        points = 0;
        nodes = 1;
        size = 1;

        // set the size to the first power of 2 that is greater than all dimensions
        while (size < Math.max(Math.max(width, height), depth)) size <<= 1;

        root = new OTNode();
    }

    // pixel constructor
    public OcTree(boolean[] pixels, int width, int height, int depth)
    {
        this(width, height, depth);

        if (pixels.length != width * height * depth) throw new IllegalArgumentException("Invalid array size");

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
                                if (pixels[x + (y * width) + (z * width * height)]) add(x, y, z);
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

    // point constructor
    public OcTree(TreeSet<Point<Integer>> points, int width, int height, int depth)
    {
        this(width, height, depth);

        for (Point<Integer> p : points)
        {
            add(p.getX(), p.getY(), p.getZ());
        }

        optimise();
    }

    // copy constructor
    public OcTree(OcTree ot)
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
    public boolean contains(int x, int y, int z)
    {
        if (x < 0 || x >= width)  throw new IndexOutOfBoundsException("Coordinate out of bounds! (x: " + x + ")");
        if (y < 0 || y >= height) throw new IndexOutOfBoundsException("Coordinate out of bounds! (y: " + y + ")");
        if (z < 0 || z >= depth)  throw new IndexOutOfBoundsException("Coordinate out of bounds! (z: " + z + ")");

        return contains(root, x, y, z, size, 0, 0, 0);
    }

    private boolean contains(OTNode node, int x, int y, int z, int size, int minX, int minY, int minZ)
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
                return contains(node.lnw, x, y, z, halfSize, minX, minY, minZ);
            }
            else if (x >= midX && y < midY && z < midZ)
            {
                return contains(node.lne, x, y, z, halfSize, midX, minY, minZ);
            }
            else if (x < midX && y >= midY && z < midZ)
            {
                return contains(node.lsw, x, y, z, halfSize, minX, midY, minZ);
            }
            else if (x >= midX && y >= midY && z < midZ)
            {
                return contains(node.lse, x, y, z, halfSize, midX, midY, minZ);
            }
            else  if (x < midX && y < midY && z >= midZ)
            {
                return contains(node.unw, x, y, z, halfSize, minX, minY, midZ);
            }
            else if (x >= midX && y < midY && z >= midZ)
            {
                return contains(node.une, x, y, z, halfSize, midX, minY, midZ);
            }
            else if (x < midX && y >= midY && z >= midZ)
            {
                return contains(node.usw, x, y, z, halfSize, minX, midY, midZ);
            }
            else
            {
                return contains(node.use, x, y, z, halfSize, midX, midY, midZ);
            }
        }

        // return leaf value
        return node.coloured;
    }

    // add a point to the structure
    public void add(int x, int y, int z)
    {
        set(x, y, z, true);
    }

    // remove a point from the structure
    public void remove(int x, int y, int z)
    {
        set(x, y, z, false);
    }

    // set the point state for a given coordinate pair
    public void set(int x, int y, int z, boolean b)
    {
        if (x < 0 || x >= width)  throw new IndexOutOfBoundsException("Coordinate out of bounds! (x: " + x + ")");
        if (y < 0 || y >= height) throw new IndexOutOfBoundsException("Coordinate out of bounds! (y: " + y + ")");
        if (z < 0 || z >= depth)  throw new IndexOutOfBoundsException("Coordinate out of bounds! (z: " + z + ")");

        set(root, x, y, z, b, size, 0, 0, 0);
    }

    private void set(OTNode node, int x, int y, int z, boolean b, int size, int minX, int minY, int minZ)
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
            set(node.lnw, x, y, z, b, halfSize, minX, minY, minZ);
        }
        else if (x >= midX && y < midY && z < midZ)
        {
            set(node.lne, x, y, z, b, halfSize, midX, minY, minZ);
        }
        else if (x < midX && y >= midY && z < midZ)
        {
            set(node.lsw, x, y, z, b, halfSize, minX, midY, minZ);
        }
        else if (x >= midX && y >= midY && z < midZ)
        {
            set(node.lse, x, y, z, b, halfSize, midX, midY, minZ);
        }
        else  if (x < midX && y < midY && z >= midZ)
        {
            set(node.unw, x, y, z, b, halfSize, minX, minY, midZ);
        }
        else if (x >= midX && y < midY && z >= midZ)
        {
            set(node.une, x, y, z, b, halfSize, midX, minY, midZ);
        }
        else if (x < midX && y >= midY && z >= midZ)
        {
            set(node.usw, x, y, z, b, halfSize, minX, midY, midZ);
        }
        else
        {
            set(node.use, x, y, z, b, halfSize, midX, midY, midZ);
        }
    }

    // optimise the OcTree by merging sub-sectors encoding a uniform value
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

    // export the QuadTress points into an list of points
    public TreeSet<Point<Integer>> getPoints()
    {
        TreeSet<Point<Integer>> points = new TreeSet<>();

        getPoints(root, points, size, 0, 0, 0);

        return points;
    }

    private void getPoints(OTNode node, TreeSet<Point<Integer>> points, int size, int minX, int minY, int minZ)
    {
        // if this quadrant encodes no data, search the sub-quadrants for data
        if (node.isDivided())
        {
            final int halfSize = size / 2;
            final int midX = minX + halfSize;
            final int midY = minY + halfSize;
            final int midZ = minZ + halfSize;

            getPoints(node.lnw, points, halfSize, minX, minY, minZ);
            getPoints(node.lne, points, halfSize, midX, minY, minZ);
            getPoints(node.lsw, points, halfSize, minX, midY, minZ);
            getPoints(node.lse, points, halfSize, midX, midY, minZ);
            getPoints(node.unw, points, halfSize, minX, minY, midZ);
            getPoints(node.une, points, halfSize, midX, minY, midZ);
            getPoints(node.usw, points, halfSize, minX, midY, midZ);
            getPoints(node.use, points, halfSize, midX, midY, midZ);
        }
        // if the leaf is coloured, add all the points in the quadrant to the list
        else if (node.coloured)
        {
            for (int z = minZ; z < minZ + size && z < depth; z++)
            {
                for (int y = minY; y < minY + size && y < height; y++)
                {
                    for (int x = minX; x < minX + size && x < width; x++)
                    {
                        points.add(new Point<>(x, y, z));
                    }
                }
            }
        }
    }

    // export the QuadTress points into an array of pixels
    public boolean[] getPixels()
    {
        boolean[] pixels = new boolean[width * height * depth];

        getPixels(root, pixels, size, 0, 0, 0);

        return pixels;
    }

    private void getPixels(OTNode node, boolean[] pixels, int size, int minX, int minY, int minZ)
    {
        // if this quadrant encodes no data, search the sub-quadrants for data
        if (node.isDivided())
        {
            final int halfSize = size / 2;
            final int midX = minX + halfSize;
            final int midY = minY + halfSize;
            final int midZ = minZ + halfSize;

            getPixels(node.lnw, pixels, halfSize, minX, minY, minZ);
            getPixels(node.lne, pixels, halfSize, midX, minY, minZ);
            getPixels(node.lsw, pixels, halfSize, minX, midY, minZ);
            getPixels(node.lse, pixels, halfSize, midX, midY, minZ);
            getPixels(node.unw, pixels, halfSize, minX, minY, midZ);
            getPixels(node.une, pixels, halfSize, midX, minY, midZ);
            getPixels(node.usw, pixels, halfSize, minX, midY, midZ);
            getPixels(node.use, pixels, halfSize, midX, midY, midZ);
        }
        // if the leaf is coloured, colour the pixel array across the index range spanned by the quadrant
        else if (node.coloured)
        {
            for (int z = minZ; z < minZ + size && z < depth; z++)
            {
                for (int y = minY; y < minY + size && y < height; y++)
                {
                    for (int x = minX; x < minX + size && x < width; x++)
                    {
                        pixels[x + (y * width) + (z * width * height)] = true;
                    }
                }
            }
        }
    }

    public TreeSet<Point<Integer>> getEdgePoints()
    {
        TreeSet<Point<Integer>> points = new TreeSet<>();

        getEdgePoints(root, points, size, 0, 0, 0);

        return points;
    }

    private void getEdgePoints(OTNode node, TreeSet<Point<Integer>> points, int size, int minX, int minY, int minZ)
    {
        if (node.isDivided())
        {
            final int halfSize = size / 2;
            final int midX = minX + halfSize;
            final int midY = minY + halfSize;
            final int midZ = minZ + halfSize;

            getEdgePoints(node.lnw, points, halfSize, minX, minY, minZ);
            getEdgePoints(node.lne, points, halfSize, midX, minY, minZ);
            getEdgePoints(node.lsw, points, halfSize, minX, midY, minZ);
            getEdgePoints(node.lse, points, halfSize, midX, midY, minZ);
            getEdgePoints(node.unw, points, halfSize, minX, minY, midZ);
            getEdgePoints(node.une, points, halfSize, midX, minY, midZ);
            getEdgePoints(node.usw, points, halfSize, minX, midY, midZ);
            getEdgePoints(node.use, points, halfSize, midX, midY, midZ);
        }
        else if (node.coloured)
        {
            final int maxX = minX + size - 1;
            final int maxY = minY + size - 1;
            final int maxZ = minZ + size - 1;

            for (int x = minX; x <= maxX; x++)
            {
                if (
                        minY - 1 <= 0 || !contains(x, minY - 1, minZ) ||
                                minZ - 1 <= 0 || !contains(x, minY, minZ - 1)
                        )
                {
                    points.add(new Point<>(x, minY, minZ));
                }

                if (
                        maxY + 1 >= height || !contains(x, maxY + 1, minZ) ||
                                minZ - 1 <= 0      || !contains(x, maxY, minZ - 1)
                        )
                {
                    points.add(new Point<>(x, maxY, minZ));
                }

                if (
                        minY - 1 <= 0     || !contains(x, minY - 1, maxZ) ||
                                maxZ + 1 >= depth || !contains(x, minY, maxZ + 1)
                        )
                {
                    points.add(new Point<>(x, minY, maxZ));
                }

                if (
                        maxY + 1 >= height || !contains(x, maxY + 1, maxZ) ||
                                maxZ + 1 >= depth  || !contains(x, maxY, maxZ + 1)
                        )
                {
                    points.add(new Point<>(x, maxY, maxZ));
                }
            }

            for (int y = minY; y <= maxY; y++)
            {
                if (
                        minX - 1 <= 0 || !contains(minX - 1, y, minZ) ||
                                minZ - 1 <= 0 || !contains(minX, y, minZ - 1)
                        )
                {
                    points.add(new Point<>(minX, y, minZ));
                }

                if (
                        maxX + 1 >= width || !contains(maxX + 1, y, minZ) ||
                                minZ - 1 <= 0     || !contains(maxX, y, minZ - 1)
                        )
                {
                    points.add(new Point<>(maxX, y, minZ));
                }

                if (
                        minX - 1 <= 0     || !contains(minX - 1, y, maxZ) ||
                                maxZ + 1 >= depth || !contains(minX, y, maxZ + 1)
                        )
                {
                    points.add(new Point<>(minX, y, maxZ));
                }

                if (
                        maxX + 1 >= width || !contains(maxX + 1, y, maxZ) ||
                                maxZ + 1 >= depth || !contains(maxX, y, maxZ + 1)
                        )
                {
                    points.add(new Point<>(maxX, y, maxZ));
                }
            }

            for (int z = minZ; z <= maxZ; z++)
            {
                if (
                        minX - 1 <= 0 || !contains(minX - 1, minY, z) ||
                                minY - 1 <= 0 || !contains(minX, minY - 1, z)
                        )
                {
                    points.add(new Point<>(minX, minY, z));
                }

                if (
                        maxX + 1 >= width || !contains(maxX + 1, minY, z) ||
                                minY - 1 <= 0     || !contains(maxX, minY - 1, z)
                        )
                {
                    points.add(new Point<>(maxX, minY, z));
                }

                if (
                        minX - 1 <= 0      || !contains(minX - 1, maxY, z) ||
                                maxY + 1 >= height || !contains(minX, maxY + 1, z)
                        )
                {
                    points.add(new Point<>(minX, maxY, z));
                }

                if (
                        maxX + 1 >= width  || !contains(maxX + 1, maxY, z) ||
                                maxY + 1 >= height || !contains(maxX, maxY + 1, z)
                        )
                {
                    points.add(new Point<>(maxX, maxY, z));
                }
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

    public boolean isEmpty()
    {
        return points == 0;
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

    public int getPointCount()
    {
        return points;
    }

    public int getNodeCount()
    {
        return nodes;
    }

    public OTNode getRoot()
    {
        return root;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OcTree ot = (OcTree) o;

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

    @Override
    public Iterator<Point<Integer>> iterator()
    {
        return new OcTreeIterator();
    }

    private class OcTreeIterator implements Iterator<Point<Integer>>
    {
        private final Stack<OTNode>  nodeStack;
        private final Stack<Integer> sizeStack;
        private final Stack<Integer> minXStack;
        private final Stack<Integer> minYStack;
        private final Stack<Integer> minZStack;

        private int x, y, z;
        private int minX, minY, minZ;
        private int maxX, maxY, maxZ;

        public OcTreeIterator()
        {
            nodeStack = new Stack<>();
            sizeStack = new Stack<>();
            minXStack = new Stack<>();
            minYStack = new Stack<>();
            minZStack = new Stack<>();

            nodeStack.push(root);
            sizeStack.push(size);
            minXStack.push(0);
            minYStack.push(0);
            minZStack.push(0);

            maxX = maxY = maxZ = Integer.MIN_VALUE;

            findNextColouredLeaf();
        }

        @Override
        public boolean hasNext()
        {
            return !nodeStack.empty() || z <= maxZ;
        }

        @Override
        public Point<Integer> next()
        {
            Point<Integer> currentPoint = new Point<>(x, y, z);

            findNextPoint();

            return currentPoint;
        }

        private void findNextPoint()
        {
            x++;

            if (x > maxX)
            {
                x = minX;
                y++;

                if (y > maxY)
                {
                    x = minX;
                    y = minY;
                    z++;

                    if (z > maxZ)
                    {
                        findNextColouredLeaf();
                    }
                }
            }
        }

        private void findNextColouredLeaf()
        {
            while (!nodeStack.empty())
            {
                final OTNode node = nodeStack.pop();
                final int size = sizeStack.pop();
                minX = minXStack.pop();
                minY = minYStack.pop();
                minZ = minZStack.pop();

                if (node.isDivided())
                {
                    final int halfSize = size / 2;
                    final int midX = minX + halfSize;
                    final int midY = minY + halfSize;
                    final int midZ = minZ + halfSize;

                    nodeStack.push(node.lnw);
                    sizeStack.push(halfSize);
                    minXStack.push(minX);
                    minYStack.push(minY);
                    minZStack.push(minZ);

                    nodeStack.push(node.lne);
                    sizeStack.push(halfSize);
                    minXStack.push(midX);
                    minYStack.push(minY);
                    minZStack.push(minZ);

                    nodeStack.push(node.lsw);
                    sizeStack.push(halfSize);
                    minXStack.push(minX);
                    minYStack.push(midY);
                    minZStack.push(minZ);

                    nodeStack.push(node.lse);
                    sizeStack.push(halfSize);
                    minXStack.push(midX);
                    minYStack.push(midY);
                    minZStack.push(minZ);

                    nodeStack.push(node.unw);
                    sizeStack.push(halfSize);
                    minXStack.push(minX);
                    minYStack.push(minY);
                    minZStack.push(midZ);

                    nodeStack.push(node.une);
                    sizeStack.push(halfSize);
                    minXStack.push(midX);
                    minYStack.push(minY);
                    minZStack.push(midZ);

                    nodeStack.push(node.usw);
                    sizeStack.push(halfSize);
                    minXStack.push(minX);
                    minYStack.push(midY);
                    minZStack.push(midZ);

                    nodeStack.push(node.use);
                    sizeStack.push(halfSize);
                    minXStack.push(midX);
                    minYStack.push(midY);
                    minZStack.push(midZ);
                }
                else if (node.coloured)
                {
                    maxX = minX + size - 1;
                    maxY = minY + size - 1;
                    maxZ = minZ + size - 1;

                    x = minX;
                    y = minY;
                    z = minZ;

                    return;
                }
            }
        }
    }
}