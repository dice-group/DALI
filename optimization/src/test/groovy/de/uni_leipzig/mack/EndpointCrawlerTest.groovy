package de.uni_leipzig.mack

import aksw.org.doodle.engine.Engine
import aksw.org.doodle.lodstats.EndpointReader
import com.google.common.base.Stopwatch
import groovy.transform.TypeChecked
import groovy.util.logging.Log4j

import java.util.concurrent.TimeUnit

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
@Log4j('logger')
class EndpointCrawlerTest extends GroovyTestCase{
    void testCollectRespsonseTimes() {
        def crawler = new EndpointCrawler()
        def endpointUris = EndpointReader.getEndpointUris(Engine.COMPLETE_ENDPOINTS_FILE)

        logger.info 'measuring resp times'
        def sw = new Stopwatch().start()
        def times = crawler.genericResponseTimes(endpointUris)
        println crawler.integerReport(times, 'time')
        logger.info "and it took ${sw.elapsed(TimeUnit.MILLISECONDS)} ms"
        logger.info "could measure ${times.size()} of ${endpointUris.size()} endpoints"
    }
}
