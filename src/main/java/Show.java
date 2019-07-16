import javax.swing.*;
import java.awt.*;

/**
 * Created by JDJFisher on 9/07/2019.
 */
public class Show
{
    private static final int FRAME_SIZE = 800;

    private Show()
    {
    }

    public static void quadTree(QuadTree qt)
    {
        final int cellSize = Math.max(1, FRAME_SIZE / (Math.max(qt.getWidth(), qt.getHeight()) + 2));

        final DrawPane drawPane = new DrawPane(qt, cellSize);
        final JFrame frame = new JFrame();
        frame.setSize(Math.min(cellSize * qt.getWidth(), FRAME_SIZE), Math.min(cellSize * qt.getHeight(), FRAME_SIZE) + 30);
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
        private final int cellSize;

        public DrawPane(QuadTree quadTree, int cellSize)
        {
            this.qt = quadTree;
            this.cellSize = cellSize;
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            qt.draw(g, cellSize);
        }
    }
}