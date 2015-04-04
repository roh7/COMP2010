package comp2010.target;

public class ConstantVariableFolding
{
    public int methodOne(){
        int a = 42;
        int b = (a + 764) * 3;
        return b + 1234 - a;
    }

    public double methodTwo(){
        double i = 0;
        int j = 1;
        return i + j;
    }

    public boolean methodThree(){
        int x = 12345;
        int y = 54321;
        return x > y;
    }
}