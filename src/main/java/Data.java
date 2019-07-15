
/**
 * Created by JDJFisher on 9/07/2019.
 */
public class Data
{
    boolean[] pixels;
    final int width;
    final int height;

    public Data(boolean[] pixels, int width, int height)
    {
        this.pixels = pixels;
        this.width = width;
        this.height = height;

        if(pixels.length != width * height) throw new RuntimeException();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == this) return true;

        Data data = (Data) obj;

        if(width != data.width || height != data.height) return false;

        for (int i = 0; i < width * height; i++)
        {
            if(pixels[i] != data.pixels[i]) return false;
        }

        return true;
    }
}
