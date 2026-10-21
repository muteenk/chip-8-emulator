import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Chip8CPU {

    private static final int MEMORY = 4096;
    private static final int WIDTH = 64;
    private static final int HEIGHT = 32;
    private static final int ROM_HEAD = 0x200;

    private final byte[] memory = new byte[MEMORY];
    private final int[] registers = new int[16];
    private final int[] stack = new int[16];
    private final int[] keyboard = new int[16];
    private final int[][] display = new int[WIDTH][HEIGHT];

    private int indexRegister = 0;
    private int programCounter = ROM_HEAD;
    private int stackPointer = 0;
    private int fps = 60;
    private int delayTimer = 0;
    private int soundTimer = 0;

    
    public void loadROM(String path) {
        int currentAddress = ROM_HEAD;
        try (var inputStream = new BufferedInputStream(Files.newInputStream(Path.of(path)))) {
            int nextByte;

            while (true) {
                if (currentAddress >= memory.length) {
                    if (inputStream.read() != -1) 
                        throw new SecurityException("Buffer overflow ! Couldn't parse ROM...");
                    break;
                }

                nextByte = inputStream.read();
                if (nextByte == -1) break;

                memory[currentAddress] = (byte) nextByte;
                currentAddress++;
            }

            System.out.println("# --- ROM loaded --- #");
        } catch (IOException ex) {
            System.err.println("Error streaming ROM: " + ex.getMessage());
        } 
    }

    private int getInstruction() {
        int mask = 0xFF;
        if (programCounter < memory.length-1) {
            byte b1 = memory[programCounter];
            byte b2 = memory[programCounter+1];
            return ((b1 & mask) << 8) | (b2 & mask);
        }
        return 0x00;
    }

    public void execute() {
        if (memory[ROM_HEAD] == 0) 
            throw new IllegalStateException("No ROM was loaded !");

        while (programCounter < memory.length-1) {
            System.out.print(Integer.toHexString(getInstruction()) + " ");
            programCounter+=2;
        }
    }

}