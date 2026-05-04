package it.fulminazzo.creeper.module


import spock.lang.Specification

class GroovySpockCalculatorTest extends Specification {

    def 'test that 2 + 2 is 4'() {
        expect: '2 + 2 should be 4'
        Calculator.sum(2, 2) == 4
    }

}