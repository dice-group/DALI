package de.uni_leipzig.mack

import de.uni_leipzig.simba.mapper.atomic.fastngram.FastNGram
import groovy.transform.TypeChecked

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
class CompilerTest {
    static FastNGram getFastNGram() {
        new FastNGram()
    }
}
