package system;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

class Display extends JPanel {

    private final int[][] display;

    public Display(int width, int height, int[][] display) {
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.WHITE);
        this.display = display;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < display.length; i++){
            for (int j = 0; j < display[i].length; j++){
                if (display[i][j] == 1) {
                    g2d.setColor(Color.GREEN);
                } else {
                    g2d.setColor(Color.BLACK);
                }
                g2d.fillRect(j*10, i*10, 10, 10);
            }
        }
        
    }
}

public class Screen {
    private static final int DISPLAY_WIDTH = 64;
    private static final int DISPLAY_HEIGHT = 32;
    private final int[][] display = new int[DISPLAY_HEIGHT][DISPLAY_WIDTH];

    private final JFrame frame = new JFrame("Chip-8 Emulator");
    Display displayCanvas = new Display(DISPLAY_WIDTH * 10, DISPLAY_HEIGHT * 10, display);

    public void initScreen() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(DISPLAY_WIDTH * 10, DISPLAY_HEIGHT * 11);
        frame.setLocationRelativeTo(null); // Centers the window on screen
        frame.add(displayCanvas);

        // Make the window visible
        frame.setVisible(true);
    }

    public void refresh() {
        displayCanvas.repaint();
    }

    public void clear() {
        for (int i = 0; i < display.length; i++) {
            for (int j = 0; j < display[i].length; j++) {
                display[i][j] = 0;
            }
        }
    }

    public int getPixel(int x, int y) {
        return display[y][x];
    }

    public void setPixel(int x, int y) {
        display[y][x] = 1;
    }

    public void unsetPixel(int x, int y) {
        display[y][x] = 0;
    }

    public void xorPixel(int x, int y, int bit) {
        display[y][x] ^= bit;
    }

    public JPanel getCanvas() {
        return displayCanvas;
    }

    public int setSprite(int[] sprite, int x, int y, int n) {

        return 0;
    }

}
