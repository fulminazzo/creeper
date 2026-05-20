package it.fulminazzo.creeper.module

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KotlinJUnitCalculatorTest {

    @Test
    fun `test that 2 + 2 is 4`() {
        assertEquals(4, Calculator.sum(2, 2), "2 + 2 should be 4");
    }

}