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
            System.out.println("Current Address" + currentAddress);
            System.out.println("Loaded Bytes: " + (currentAddress - programCounter));
            System.out.println("# --- First few bytes --- #");
            System.out.println(memory[programCounter]);
            System.out.println(memory[programCounter+1]);
            System.out.println(memory[programCounter+2]);
            System.out.println(memory[programCounter+3]);
        } catch (IOException ex) {
            System.err.println("Error streaming ROM: " + ex.getMessage());
        } 
    }

}