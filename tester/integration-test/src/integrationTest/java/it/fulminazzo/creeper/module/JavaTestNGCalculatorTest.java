package it.fulminazzo.creeper.module;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class JavaTestNGCalculatorTest {

    @Test(description = "test that 2 + 2 is 4")
    void test() {
        assertEquals(4, Calculator.sum(2, 2), "2 + 2 should be 4");
    }

}
