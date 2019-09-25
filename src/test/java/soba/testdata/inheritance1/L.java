package soba.testdata.inheritance1;

public enum L {

    ORANGE,
    APPLE,
    BANANA;
    
    public static boolean m(L test) {
        return test.equals(ORANGE);
    }
}
