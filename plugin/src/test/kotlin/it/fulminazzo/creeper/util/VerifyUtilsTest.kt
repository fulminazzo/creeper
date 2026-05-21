package it.fulminazzo.creeper.util

import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class VerifyUtilsTest {

    @Test
    fun `test that awaitVerified returns true if verification was successful`() {
        val previous = AtomicBoolean(false)
        val verifications = AtomicInteger(0)
        val result = VerifyUtils.awaitVerified(
            {
                val curr = previous.get()
                previous.set(!curr)
                if (curr) verifications.getAndIncrement()
                curr
            },
            2.seconds,
            times = 5,
            interval = 125.milliseconds
        )

        assertTrue(result, "awaitVerified should return true")
        assertEquals(
            5,
            verifications.get(),
            "awaitVerified should have verified exactly 5 times"
        )
    }

    @Test
    fun `test that awaitVerified returns false if verification times out`() {
        val result = VerifyUtils.awaitVerified({ false }, 1.milliseconds)
        assertFalse(result, "awaitVerified should return false")
    }

}