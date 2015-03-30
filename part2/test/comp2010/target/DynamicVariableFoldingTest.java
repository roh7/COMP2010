package comp2010.target;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class DynamicVariableFoldingTest
{
    DynamicVariableFolding dvf = new DynamicVariableFolding();
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams()
    {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void cleanUpStreams()
    {
        System.setOut(null);
    }

    @Test
    public void testMethodOne()
    {
        assertEquals(3630, dvf.methodOne());
    }

    @Test
    public void testMethodTwoOut()
    {
        dvf.methodTwo();
        assertEquals("true\n", outContent.toString());
    }

    @Test
    public void testMethodTwoReturn()
    {
        assertEquals(true, dvf.methodTwo());
    }

    @Test
    public void testMethodThree()
    {
        assertEquals(84, dvf.methodThree());
    }


}