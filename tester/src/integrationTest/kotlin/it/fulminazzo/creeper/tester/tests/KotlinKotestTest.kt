package it.fulminazzo.creeper.tester.tests

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class KotlinKotestTest : StringSpec({

    "test" {
        true shouldBe true
    }

})