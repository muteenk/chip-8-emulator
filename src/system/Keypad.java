package system;

public class Keypad {
    private final int MAX_KEYS = 16;
    private boolean[] KEYS = new boolean[MAX_KEYS];

    public Keypad() {
        for (int i = 0; i < KEYS.length; i++) KEYS[i] = false;
    }

    public boolean isKeyOn(int idx) throws IllegalStateException {
        if (idx < 0 || idx > 15) 
            throw new IllegalStateException("Key index out of bounds !");
        return KEYS[idx];
    }

    public int getAnyKeyPress() {
        for (int i = 0; i < MAX_KEYS; i++) {
            if (KEYS[i]) return i;
        }
        return -1;
    }
}
