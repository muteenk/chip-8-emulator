package system;

public enum Control {
    NUM1("1", 1),
    NUM2("2", 2),
    NUM3("3", 3),
    C("4", 12),
    NUM4("Q", 4),
    NUM5("W", 5),
    NUM6("E", 6),
    D("R", 13),
    NUM7("A", 7),
    NUM8("S", 8),
    NUM9("D", 9),
    E("F", 14),
    A("Z", 10),
    NUM0("X", 0),
    B("C", 11),
    F("V", 15);

    private final String defaultKey;
    private final int index;

    Control(String defaultKey, int index) {
        this.defaultKey = defaultKey;
        this.index = index;
    }

    public String getDefaultKey() {
        return this.defaultKey;
    }

    public int getIndex() {
        return this.index;
    }
}
