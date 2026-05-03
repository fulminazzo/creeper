package it.fulminazzo.creeper.tester.tests;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class JavaTestNGTest {

    @Test(description = "test that 2 + 2 is 4")
    void test() {
        assertEquals(4, 2 + 2, "2 + 2 should be 4");
    }

}
