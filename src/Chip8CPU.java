import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Chip8CPU {

    private static final int MEMORY_BYTES = 4096;
    private static final int STACK_SPACE = 16;
    private static final int DISPLAY_WIDTH = 64;
    private static final int DISPLAY_HEIGHT = 32;
    private static final int ROM_HEAD = 0x200;

    private final byte[] memory = new byte[MEMORY_BYTES];
    private final int[] registers = new int[16];
    private final int[] stack = new int[STACK_SPACE];
    private final int[] keyboard = new int[16];
    private final int[][] display = new int[DISPLAY_WIDTH][DISPLAY_HEIGHT];

    private int indexRegister = 0;
    private int programCounter = ROM_HEAD;
    private int stackPointer = -1;
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
            programCounter+=2;
            return ((b1 & mask) << 8) | (b2 & mask);
        }
        return 0x00;
    }

    private void parseInstruction(int opcode) {
        int type = (opcode & 0xF000) >> 12;
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        int kk = (opcode & 0x00FF);
        int nnn = (opcode & 0x0FFF);

        switch (type) {
            case 0x0:
                if (kk == 0xE0){
                    for (int i = 0; i < display.length; i++)
                        for (int j = 0; j < display[0].length; j++)
                            display[i][j] = 0;
                }
                else if (kk == 0xEE){
                    if (stackPointer < 0)
                        throw new IllegalStateException("Tried to return while the call stack is empty !");
                    programCounter = stack[stackPointer];
                    stackPointer--;
                }
                break;
            case 0x1:
                programCounter = nnn;
                break;
            case 0x2:
                if (stackPointer + 1 >= STACK_SPACE)
                    throw new IllegalStateException("The Stack memory has overflown !");
                stackPointer++;
                stack[stackPointer] = programCounter;
                programCounter = nnn;
                break;
            case 0x3:
                if (registers[x] == kk) programCounter += 2;
                break;
            case 0x4:
                if (registers[x] != kk) programCounter += 2;
                break;
            case 0x5:
                if (registers[x] == registers[y]) programCounter += 2;
                break;
            case 0x6:
                registers[x] = (kk & 0xFF);
                break;
            case 0x7:
                registers[x] = (registers[x] + kk) & 0xFF;
                break;
            case 0x9:
                if (registers[x] != registers[y]) programCounter += 2;
                break;
            default:
                break;
        }
    }

    public void execute() {
        if (memory[ROM_HEAD] == 0)
            throw new IllegalStateException("No ROM was loaded !");

        int cnt = 0;
        while (programCounter < memory.length-1) {
            if (cnt > 10) break;
            int instruction = getInstruction();
            System.out.println(Integer.toHexString(instruction));
            System.out.println("Program Counter: " + Integer.toHexString(programCounter));
            parseInstruction(instruction);
            cnt++;
        }
    }

}