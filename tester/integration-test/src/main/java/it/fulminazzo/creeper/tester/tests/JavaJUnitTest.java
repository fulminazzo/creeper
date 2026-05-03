package it.fulminazzo.creeper.tester.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaJUnitTest {

    @DisplayName("test that 2 + 2 is 4")
    @Test
    void testSum() {
        assertEquals(4, 2 + 2, "2 + 2 should be 4");
    }

}
