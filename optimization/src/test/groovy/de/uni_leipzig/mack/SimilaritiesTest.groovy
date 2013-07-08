package de.uni_leipzig.mack

import aksw.org.doodle.dataset.Description
import aksw.org.doodle.dataset.VectorDescription
import aksw.org.doodle.engine.Engine
import aksw.org.doodle.similarity.AlternativeQGramSimilarity
import aksw.org.doodle.similarity.CosineSimilarity
import aksw.org.doodle.similarity.QGramSimilarity
import aksw.org.doodle.similarity.Similarity
import com.google.common.collect.ImmutableMap
import groovy.transform.TypeChecked
import groovy.util.logging.Log4j

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
@Log4j('logger')
class SimilaritiesTest extends GroovyTestCase{

    static <T> List<List<T>> combWithoutSymmetry(List<T> list) {
        def combinations = new LinkedList<List<T>>()

        for(first in list) {
            def innerIter = list.iterator()
            def second = innerIter.next()

            while(second != first) {
                second = innerIter.next()
            }

            assert first == second
            List<T> pair =  (List<T>) [first, second]
            combinations.addLast(pair)

            while(innerIter.hasNext()) {
                pair =  (List<T>) [first, innerIter.next()]
                combinations.addLast(pair)
            }
        }
        return combinations
    }

    void testSimilarityComparison() {
        def eng = Engine.instance
        def simMeasures = ImmutableMap.copyOf([qgram: new QGramSimilarity(),
                altqgram: new AlternativeQGramSimilarity(),
                cosine: new CosineSimilarity()])

        def dsSample = eng.datasets.entrySet().grep({ Map.Entry<String, Description> e ->
            def desc = e.value
            if(desc instanceof VectorDescription) {
                return desc.features.size() > 0 ? true : false
            } else {
                return false
            }
        }) .toList()[0..<8]

        def combs = combWithoutSymmetry(dsSample)

        def headings =  ['knowledge base pair'] + (Collection<String>) simMeasures.keySet()
        printf('%-68s %8s %8s %8s%n', headings)
        println combs.collect({
             List<Map.Entry<String, Description>> pairList ->
                 def dsNames = pairList.key.collect({ sprintf('%-32.32s', it) }).join(' <=> ')
                 def line = simMeasures.values().inject(new StringBuffer(dsNames)) { StringBuffer sb, Similarity sim ->
                    sb << sprintf('%8.2f ', sim.getSimilarity(pairList[0].value, pairList[1].value))
                 }
             line
        }).join('\n')
    }
}
