package it.fulminazzo.creeper.tester.tests

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ScalaScalatestTest extends AnyFlatSpec with Matchers {

    "2 + 2" should "be 4" in {
        (2 + 2) shouldBe 4
    }

}
