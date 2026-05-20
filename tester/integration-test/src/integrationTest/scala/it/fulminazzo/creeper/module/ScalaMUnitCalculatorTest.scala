package it.fulminazzo.creeper.module

import munit.FunSuite

class ScalaMUnitCalculatorTest extends FunSuite {
    
    test("test that 2 + 2 is 4") {
        assertEquals(Calculator.sum(2, 2), 4, "2 + 2 should be 4")
    }

}
