package jdjf.quadTree;

import sjcross.sjcommon.Point;
import java.util.TreeSet;

/**
 * Created by JDJFisher on 16/07/2019.
 */
public class PointData
{
    public TreeSet<Point<Integer>> points;
    public final int width;
    public final int height;

    public PointData(TreeSet<Point<Integer>> points, int width, int height)
    {
        this.points = points;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        PointData data = (PointData) obj;

        return width  == data.width &&
               height == data.height &&
               points.equals(data.points);
    }

    @Override
    public int hashCode()
    {
        int result = points != null ? points.hashCode() : 0;
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }
}
