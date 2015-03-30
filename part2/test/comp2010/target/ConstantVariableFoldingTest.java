package comp2010.target;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by ntrolls on 04/03/15.
 */
public class ConstantVariableFoldingTest {

    ConstantVariableFolding cvf = new ConstantVariableFolding();

    @Test
    public void testMethodOne(){
        assertEquals(3610, cvf.methodOne());
    }

    @Test
    public void testMethodTwo(){
        assertEquals(1.0, cvf.methodTwo(), 0);
    }

    @Test
    public void testMethodThree(){
        assertEquals(false, cvf.methodThree());
    }
}
