package it.fulminazzo.creeper.tester.tests

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KotlinJUnitTest {

    @Test
    fun `test that 2 + 2 is 4`() {
        assertEquals(4, 2 + 2, "2 + 2 should be 4");
    }

}