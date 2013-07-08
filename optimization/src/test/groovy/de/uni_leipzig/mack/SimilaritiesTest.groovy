package de.uni_leipzig.mack

import aksw.org.doodle.dataset.Description
import aksw.org.doodle.dataset.VectorDescription
import aksw.org.doodle.engine.Engine
import aksw.org.doodle.lodstats.EndpointReader
import aksw.org.doodle.similarity.CosineSimilarity
import aksw.org.doodle.similarity.QGramSimilarity
import aksw.org.doodle.similarity.Similarity
import com.google.common.collect.*
import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.query.Syntax
import groovy.transform.TypeChecked
import groovy.util.logging.Log4j

import static aksw.org.doodle.similarity.AbstractNormalizedQGramSimilarity.NormalizedAssymmetricQGramSimilarity
import static aksw.org.doodle.similarity.AbstractNormalizedQGramSimilarity.NormalizedSymmetricQGramSimilarity

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

    void testEndpointDescriptions() {
        def endpoints = EndpointReader.getEndpointUris(Engine.SELECTED_ENDPOINTS_FILE)
        logger.info "endpoints read"
        def queryStr = 'SELECT ?s (COUNT(?s) as ?c) { ?s ?p ?o } GROUP BY ?s ORDER BY ?c'
        def query = QueryFactory.create(queryStr, Syntax.syntaxARQ)
        def qexec = QueryExecutionFactory.sparqlService(endpoints.first(), query)
        logger.info "starting query execution"
        def results = qexec.execSelect()
        logger.info("uri query executed")
        Multiset<URI> subjUris = HashMultiset.create()
        Multiset<String> subjNamespaces = HashMultiset.create()
        def rowsProcessed = 0
        while(results.hasNext()) {
            if(rowsProcessed % 10 == 0) logger.debug "$rowsProcessed rows processed"
            def resultRow = results.next()
            def resName = resultRow.get('s')
            if(resName.isURIResource()) {
                def nameSpace = resName.asResource().nameSpace
                def uriStr = resName.asResource().getURI()
                URI resUri = null
                try {
                    resUri = URI.create(uriStr)
                } catch(IllegalArgumentException iae) {
                    logger.warn "Encountered illegal URI for names resource: '$uriStr'"
                }
                def count = resultRow.getLiteral('c').getInt()
                ((AbstractMapBasedMultiset) subjUris).add(resUri, count)
                ((AbstractMapBasedMultiset) subjNamespaces).add(nameSpace, count)
                rowsProcessed++
            }
        }
        subjUris = Multisets.copyHighestCountFirst(subjUris)
        subjNamespaces = Multisets.copyHighestCountFirst(subjNamespaces)
        for(e in ['subject uris': subjUris, 'subject namespaces': subjNamespaces].entrySet()) {
            logger.info "Found ${e.value.size()} $e.key"
            logger.debug(e.value.entrySet().collect({ Multiset.Entry me ->
                sprintf("%96.-96s\t%5d", me.element, me.count)
            }).join('\n'))
        }
    }

    void testSimilarityComparison() {
        def eng = Engine.instance
        def simMeasures = ImmutableMap.copyOf([qgram: new QGramSimilarity(),
                norm_qgram_asym: new NormalizedAssymmetricQGramSimilarity(),
                norm_qgram_symm: new NormalizedSymmetricQGramSimilarity(),
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
        printf('%-68s %8s %8s %8s %8s%n', headings)
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
