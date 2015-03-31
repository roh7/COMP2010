package comp2010.target;

public class DynamicVariableFolding {
    public int methodOneDynamicVariableFolding() {
        int a = 42;
        int b = (a + 764) * 3;
        a = 666;
        return b + 1234 - a;
    }

    public boolean methodTwoDynamicVariableFolding() {
        int x = 12345;
        int y = 54321;
        System.out.println(x < y);
        y = 0;
        return x > y;
    }

    public int methodThreeDynamicVariableFolding() {
        int i = 0;
        int j = i + 3;
        i = j + 4;
        j = i + 5;
        return i * j;
    }
}