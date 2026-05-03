package it.fulminazzo.creeper.tester.tests

import munit.FunSuite

class ScalaMUnitTest extends FunSuite {
    
    test("test that 2 + 2 is 4") {
        assertEquals(2 + 2, 4, "2 + 2 should be 4")
    }

}
