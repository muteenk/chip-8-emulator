package system;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import utils.Utils;

public class Chip8CPU {

    public static final byte[] FONT_SET = {
        // 0
        (byte) 0xF0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xF0,
        // 1
        (byte) 0x20, (byte) 0x60, (byte) 0x20, (byte) 0x20, (byte) 0x70,
        // 2
        (byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x80, (byte) 0xF0,
        // 3
        (byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x10, (byte) 0xF0,
        // 4
        (byte) 0x90, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0x10,
        // 5
        (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x10, (byte) 0xF0,
        // 6
        (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x90, (byte) 0xF0,
        // 7
        (byte) 0xF0, (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x40,
        // 8
        (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0xF0,
        // 9
        (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0xF0,
        // A
        (byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0x90,
        // B
        (byte) 0xE0, (byte) 0x90, (byte) 0xE0, (byte) 0x90, (byte) 0xE0,
        // C
        (byte) 0xF0, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0xF0,
        // D
        (byte) 0xE0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xE0,
        // E
        (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0xF0,
        // F
        (byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0x80
    };

    private static final int MEMORY_BYTES = 4096;
    private static final int STACK_SPACE = 16;
    private static final int ROM_HEAD = 0x200;
    private static final int FPS = 60;
    private static final int BYTE_MASK = 0xFF;
    private static final int FONT_START_ADDR = 0x00;

    private final byte[] memory = new byte[MEMORY_BYTES];
    private final int[] registers = new int[16];
    private final int[] stack = new int[STACK_SPACE];

    private int indexRegister = 0;
    private int programCounter = ROM_HEAD;
    private int stackPointer = -1;
    private int delayTimer = 0;
    private int soundTimer = 0;
    private volatile boolean running;

    private final Keypad keypad;
    private final Screen screen;

    public Chip8CPU(Screen screen, Keypad keypad) {
        this.keypad = keypad;
        this.screen = screen;

        int fontAddr = FONT_START_ADDR;
        for (int i = 0; i < FONT_SET.length; i++) {
            memory[fontAddr] = FONT_SET[i];
            fontAddr++;
        }

    }
    
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
        if (programCounter < memory.length-1) {
            byte b1 = memory[programCounter];
            byte b2 = memory[programCounter+1];
            programCounter+=2;
            return ((b1 & BYTE_MASK) << 8) | (b2 & BYTE_MASK);
        }
        return 0x00;
    }

    private void parseInstruction(int opcode) {
        int type = (opcode & 0xF000) >> 12;
        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;
        int kk = (opcode & 0x00FF);
        int nnn = (opcode & 0x0FFF);
        int n = (opcode & 0x000F);

        switch (type) {
            case 0x0:
                if (kk == 0xE0){
                    screen.clear();
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
                registers[x] = (kk & BYTE_MASK);
                break;
            case 0x7:
                registers[x] = (registers[x] + kk) & BYTE_MASK;
                break;
            case 0x8:
                parseType8Instruction(x, y, n);
                break;
            case 0x9:
                if (registers[x] != registers[y]) programCounter += 2;
                break;
            case 0xA:
                indexRegister = nnn;
                break;
            case 0xB:
                programCounter = (nnn + registers[0]) & BYTE_MASK;
                break;
            case 0xC:
                int random = Utils.randomInt(0, 255);
                registers[x] = random & kk;
                break;
            case 0xD:
                draw(x, y, n);
                break;
            case 0xE:
                if (kk == 0x9E) {
                    if (keypad.isKeyOn(registers[x] & 0x0F)) programCounter += 2;
                }
                else if (kk == 0xA1) {
                    if (!keypad.isKeyOn(registers[x] & 0x0F)) programCounter += 2;
                }
                break;
            case 0xF:
                parseTypeFInstruction(x, kk);
                break;
            default:
                break;
        }
    }

    private void parseType8Instruction(int x, int y, int n) {
        int result;
        switch (n) {
            case 0x0:
                // COPY
                registers[x] = registers[y];
                break;
            case 0x1:
                // OR
                registers[x] = registers[x] | registers[y];
                break;
            case 0x2:
                // AND
                registers[x] = registers[x] & registers[y];
                break;
            case 0x3:
                // XOR
                registers[x] = registers[x] ^ registers[y];
                break;
            case 0x4:
                // ADD
                result = registers[x] + registers[y];
                registers[15] = (result > 255) ? 1 : 0;
                registers[x] = (result & BYTE_MASK);
                break;
            case 0x5:
                // SUBTRACT with BORROW checks
                registers[15] = (registers[x] >= registers[y]) ? 1 : 0;
                result = registers[x] - registers[y];
                registers[x] = (result & BYTE_MASK);
                break;
            case 0x6:
                // RIGHT SHIFT
                registers[15] = registers[x] & 1;
                registers[x] = ((registers[x] >> 1) & BYTE_MASK);
                break;
            case 0x7:
                // NEGATIVE SUBTRACTION with BORROW checks
                registers[15] = (registers[y] >= registers[x]) ? 1 : 0;
                result = registers[y] - registers[x];
                registers[x] = (result & BYTE_MASK);
                break;
            case 0xE:
                // LEFT SHIFT
                registers[15] = ((registers[x] & 0x80) != 0) ? 1 : 0;
                registers[x] = ((registers[x] << 1) & BYTE_MASK);
                break;
            default:
                break;
        }
    }

    private void parseTypeFInstruction(int x, int kk) {
        int result;
        int initAddr;
        switch (kk) {
            case 0x07:
                registers[x] = delayTimer;
                break;
            case 0x0A:
                int keyAddr = keypad.getAnyKeyPress();
                if (keyAddr > -1) registers[x] = keyAddr;
                else programCounter -= 2;
                break;
            case 0x15:
                delayTimer = registers[x];
                break;
            case 0x18:
                soundTimer = registers[x];
                break;
            case 0x1E:
                result = indexRegister + registers[x];
                indexRegister = result & BYTE_MASK;
                break;
            case 0x29:
                indexRegister = FONT_START_ADDR + (registers[x] & 0x0F) * 5;
                break;
            case 0x33:
                int p100 = (registers[x] / 100);
                int p10 = ((registers[x] - (p100*100)) / 10);
                int p1 = (registers[x] - p100 * 100 - p10 * 10);
                memory[indexRegister] = (byte) p100;
                memory[indexRegister + 1] = (byte) p10;
                memory[indexRegister + 2] = (byte) p1;
                break;
            case 0x55:
                initAddr = indexRegister;
                for (int i = 0; i <= x; i++) {
                    memory[initAddr] = (byte) registers[i];
                    initAddr++;
                }
                break;
            case 0x65:
                initAddr = indexRegister;
                for (int i = 0; i <= x; i++) {
                    registers[i] = memory[initAddr] & BYTE_MASK;
                    initAddr++;
                }
                break;
            default:
                break;
        }
    }

    private void draw(int x, int y, int n) {
        int startX = registers[x];
        int startY = registers[y];
        registers[15] = 0;

        for (int row = 0; row < n; row++) {
            int spriteByte = memory[indexRegister + row];
            for (int col = 0; col < 8; col++) {
                int currentBit = spriteByte & (0x80 >> col);
                if (currentBit != 0) {
                    int screenX = (startX + col) % 64;
                    int screenY = (startY + row) % 32;

                    if (screen.getPixel(screenX, screenY) == 1) registers[15] = 1;
                    screen.xorPixel(screenX, screenY, 1);
                }
            }
        }

        screen.refresh();
    }

    private void cycle() {
        int opcode = getInstruction();
        parseInstruction(opcode);
    }

    private void tickTimers() {
        if (delayTimer > 0) delayTimer--;
        if (soundTimer > 0) soundTimer--;
    }

    public void execute() throws InterruptedException {
        if (memory[ROM_HEAD] == 0)
            throw new IllegalStateException("No ROM was loaded !");

        long deadline = System.nanoTime();
        long frame = 1_000_000_000L / FPS;
        running = true;

        while (running) {
            for (int i = 0; i < 10; i++) cycle();
            tickTimers();

            deadline += frame;
            long remaining = deadline - System.nanoTime();
            if (remaining > 0) Thread.sleep(remaining / 1_000_000, (int) (remaining % 1_000_000));
        }
    }

    public void stop() {
        running = false;
    }

}