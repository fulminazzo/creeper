package it.fulminazzo.creeper.util

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A collection of utilities to verify data integrity.
 */
object VerifyUtils {

    /**
     * Awaits that the given condition is verified.
     *
     * @param condition the condition
     * @param timeout the timeout after which the verification will be considered failed
     * @param times the number of times the condition must be checked
     * @param interval the interval between each check
     * @receiver the condition
     * @return `true` if the condition was verified, `false` otherwise
     */
    fun awaitVerified(
        condition: () -> Boolean,
        timeout: Duration,
        times: Int = 1,
        interval: Duration = 1.seconds
    ): Boolean {
        val verified = AtomicBoolean(false)
        val latch = CountDownLatch(times)

        val scheduler = Executors.newSingleThreadScheduledExecutor()
        try {
            scheduler.scheduleAtFixedRate({
                val result = condition()
                verified.set(result)
                if (result) latch.countDown()
            }, 0, interval.inWholeMilliseconds, TimeUnit.MILLISECONDS)

            latch.await(timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        } finally {
            scheduler.shutdownNow()
        }

        return verified.get()
    }

}