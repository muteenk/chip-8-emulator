package system;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

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

    public void setMapKeyStrokes(JPanel panel) {
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = panel.getActionMap();

        for (Control control : Control.values()) {
            inputMap.put(
                KeyStroke.getKeyStroke(control.getDefaultKey()),
                control.name()
            );
            inputMap.put(
                KeyStroke.getKeyStroke("released " + control.getDefaultKey()),
                control.name()+"Released"
            );
            actionMap.put(
                control.name(),
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (KEYS[control.getIndex()]) return;
                        KEYS[control.getIndex()] = true;
                    }
                }
            );
            actionMap.put(
                control.name()+"Released", 
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        KEYS[control.getIndex()] = false;
                    }
                }
            );
        }
    }
}
