package jdjf.quadTree;

import java.util.Arrays;

/**
 * Created by JDJFisher on 9/07/2019.
 */
public class PixelData
{
    public boolean[] pixels;
    public final int width;
    public final int height;

    public PixelData(boolean[] pixels, int width, int height)
    {
        this.pixels = pixels;
        this.width = width;
        this.height = height;

        if (pixels.length != width * height) throw new IllegalArgumentException("Invalid array size");
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        PixelData data = (PixelData) obj;

        if (width != data.width || height != data.height) return false;

        for (int i = 0; i < width * height; i++)
        {
            if (pixels[i] != data.pixels[i]) return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = Arrays.hashCode(pixels);
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }
}
