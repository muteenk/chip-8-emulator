package system;

public class Keypad {
    protected static boolean[] KEYPAD = new boolean[16];

    public Keypad() {
        for (int i = 0; i < KEYPAD.length; i++) KEYPAD[i] = false;
    }

}
