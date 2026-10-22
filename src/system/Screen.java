package system;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Screen {
    private static final int DISPLAY_WIDTH = 640;
    private static final int DISPLAY_HEIGHT = 320;
    private final int[][] display = new int[DISPLAY_WIDTH][DISPLAY_HEIGHT];

    public void renderScreen() {
        // Create the top-level container (JFrame)
        JFrame frame = new JFrame("Chip-8 Emulator");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
        frame.setLocationRelativeTo(null); // Centers the window on screen

        // Create a basic button component
        JButton button = new JButton("Click Me");
        frame.add(button);

        // Make the window visible
        frame.setVisible(true);
    }

}
