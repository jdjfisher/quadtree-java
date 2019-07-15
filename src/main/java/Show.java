import javax.swing.*;
import java.awt.*;

/**
 * Created by JDJFisher on 9/07/2019.
 */
public class Show
{
    private static final int SIZE = 800;

    private Show()
    {
    }

    public static void quadTree(QuadTree qt)
    {
        final int cellSize = Math.max(1, SIZE / (Math.max(qt.getWidth(), qt.getHeight()) + 2));

        final DrawPane drawPane = new DrawPane(qt);
        final JFrame frame = new JFrame();
        frame.setSize(cellSize * qt.getWidth(), cellSize * qt.getHeight() + 30);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setContentPane(drawPane);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setBackground(new Color(58, 58, 58));
        frame.setVisible(true);
    }

    public static class DrawPane extends JPanel
    {
        private final QuadTree qt;

        public DrawPane(QuadTree quadTree)
        {
            this.qt = new QuadTree(quadTree);
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            qt.draw(g, Math.max(1, SIZE / (Math.max(qt.getWidth(), qt.getHeight()) + 2)));
        }
    }
}