import system.Chip8CPU;

public class Main {

    public static void main(String[] args) {
        Chip8CPU cpu = new Chip8CPU();
        if (args.length < 1) {
            throw new IllegalArgumentException("ROM file not provided !");

        }
        cpu.loadROM(args[0]);
        try {
            cpu.execute();
        } catch (InterruptedException ex) {
            System.out.println("Execution halted: " + ex.getMessage());
        }
        
    }

}
