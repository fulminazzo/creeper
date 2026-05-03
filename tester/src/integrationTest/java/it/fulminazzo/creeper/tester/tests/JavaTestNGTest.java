package it.fulminazzo.creeper.tester.tests;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class JavaTestNGTest {

    @Test(description = "Basic assertion")
    void test() {
        assertTrue(true, "Should have been true");
    }

}
