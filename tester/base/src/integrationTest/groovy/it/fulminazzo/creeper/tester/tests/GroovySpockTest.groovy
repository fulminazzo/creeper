package it.fulminazzo.creeper.tester.tests

import spock.lang.Specification

class GroovySpockTest extends Specification {

    def 'test that 2 + 2 is 4'() {
        expect: '2 + 2 should be 4'
        2 + 2 == 4
    }

}