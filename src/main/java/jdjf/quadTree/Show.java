package jdjf.quadTree;

import javax.swing.*;
import java.awt.*;

/**
 * Created by JDJFisher on 9/07/2019.
 */
public class Show
{
    private static final int MAX_FRAME_SIZE = 900;
    private JFrame frame;
    private QuadTree qt;
    private int cellSize;

    public Show(QuadTree qt)
    {
        this.qt = qt;

        frame = new JFrame();
        updateFrameSize();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setContentPane(new DrawPane(qt, cellSize));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setBackground(new Color(58, 58, 58));
        frame.setVisible(true);
    }

    public void update()
    {
        updateFrameSize();
        frame.repaint();
    }

    private void updateFrameSize()
    {
        cellSize = Math.max(1, MAX_FRAME_SIZE / qt.getRootSize());
        int frameSize = Math.min(cellSize * qt.getRootSize(), MAX_FRAME_SIZE);
        frame.setSize(frameSize, frameSize + 30);
    }

    private static class DrawPane extends JPanel
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
            if(qt.getRootSize() > MAX_FRAME_SIZE)
            {
                g.setFont(new Font("Arial", Font.PLAIN, 40));
                g.setColor(Color.WHITE);
                g.drawString("Quad Tree too big to display!", MAX_FRAME_SIZE / 2 - 240, MAX_FRAME_SIZE / 2 - 50);
            }
            else
            {
                qt.draw(g, cellSize, true);
            }
        }
    }
}