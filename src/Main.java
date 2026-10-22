import javax.swing.SwingUtilities;

import system.Chip8CPU;
import system.Keypad;
import system.Screen;

public class Main {

    public static void main(String[] args) {
        Screen screen = new Screen();
        Keypad keypad = new Keypad();
        Chip8CPU cpu = new Chip8CPU(screen, keypad);

        if (args.length < 1) {
            throw new IllegalArgumentException("ROM file not provided !");

        }
        cpu.loadROM(args[0]);
        SwingUtilities.invokeLater(() -> {
                screen.renderScreen();
        });
        try {
            cpu.execute();
        } catch (InterruptedException ex) {
            System.out.println("Execution halted: " + ex.getMessage());
        }
        
    }

}
