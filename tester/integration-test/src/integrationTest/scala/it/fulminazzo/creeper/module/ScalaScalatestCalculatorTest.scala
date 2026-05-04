package it.fulminazzo.creeper.module

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ScalaScalatestCalculatorTest extends AnyFlatSpec with Matchers {

    "2 + 2" should "be 4" in {
        Calculator.sum(2, 2) shouldBe 4
    }

}
