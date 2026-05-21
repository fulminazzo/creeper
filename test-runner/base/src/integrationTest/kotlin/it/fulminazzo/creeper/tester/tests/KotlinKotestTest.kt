package it.fulminazzo.creeper.tester.tests

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class KotlinKotestTest : StringSpec({

    "test that 2 + 2 is 4" {
        2 + 2 shouldBe 4
    }

})