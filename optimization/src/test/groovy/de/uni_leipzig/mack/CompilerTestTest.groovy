package de.uni_leipzig.mack
/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
class CompilerTestTest extends  GroovyTestCase {

    void testLinking() {
        def name = CompilerTestJava.getSimMethodname()
        assertEquals("FastNGram", name)
    }
}
