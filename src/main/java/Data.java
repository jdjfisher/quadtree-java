
/**
 * Created by JDJFisher on 9/07/2019.
 */
public class Data
{
    boolean[] points;
    final int width;
    final int height;

    public Data(boolean[] points, int width, int height)
    {
        this.points = points;
        this.width = width;
        this.height = height;

        if(points.length != width * height) throw new RuntimeException("");
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == this) return true;

        Data data = (Data) obj;

        if(width != data.width || height != data.height) return false;

        for (int i = 0; i < width * height; i++)
        {
            if(points[i] != data.points[i]) return false;
        }

        return true;
    }
}
