package it.fulminazzo.creeper.module

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class KotlinKotestCalculatorTest : StringSpec({

    "test that 2 + 2 is 4" {
        Calculator.sum(2, 2) shouldBe 4
    }

})