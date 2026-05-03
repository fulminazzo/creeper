package it.fulminazzo.creeper.tester.tests

import spock.lang.Specification

class GroovySpockTest extends Specification {

    def test() {
        expect: 'Should have been true'
        true
    }

}