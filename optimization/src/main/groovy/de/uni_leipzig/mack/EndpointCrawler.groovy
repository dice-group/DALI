package de.uni_leipzig.mack

import aksw.org.doodle.engine.DescriptionCollector
import com.google.common.base.Optional
import com.google.common.base.Stopwatch
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import groovy.transform.TypeChecked
import groovy.util.logging.Log4j
import groovyx.gpars.GParsPool

import java.util.concurrent.*

import static java.util.Map.Entry

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
@Log4j('logger')
class EndpointCrawler {
    ConcurrentMap<String, Integer> responseTimes = Maps.newConcurrentMap()
    ConcurrentMap<String, Integer> tripleCount = Maps.newConcurrentMap()
    ExecutorService executor = Executors.newCachedThreadPool()

    def <V> Map<String, V> collectEndpointsData(List<String> endpointUris, Closure<V> work, int timeout) {
        def data = new ConcurrentHashMap<String,V>(endpointUris.size())
        GParsPool.withPool(16) {
            endpointUris.eachParallel { String uri ->
                def result = getDataWithTimeout(uri, work, timeout)
                if(result.present) {
                    data.putIfAbsent(uri,result.get())
                }
            }
        }
        data
    }

    protected <V> Optional<V> getDataWithTimeout(String endpointUri, Closure<V> work, int timeout) {
        Optional<V> ret = Optional.absent()
        def future = executor.submit(work.curry(endpointUri) as Callable<V>)
        try{
            ret = Optional.of(future.get(timeout, TimeUnit.SECONDS))
        } catch (TimeoutException toe) {
            logger.warn "Closure for '$endpointUri' timed out"
        } catch (ExecutionException ee) {
            logger.warn "Exception during is-alive test for '$endpointUri':\n${ee}"
        }
        ret
    }

    Map<String,Integer> genericResponseTimes(List<String> endpointUris) {
        def probeRespTime = { String uri ->
            def sw = new Stopwatch().start()
            DescriptionCollector.isAlive(uri)
            sw.elapsed(TimeUnit.MILLISECONDS).toInteger()
        }

        def respTimeMap = collectEndpointsData(endpointUris,probeRespTime ,300)
        ImmutableMap.copyOf(respTimeMap)
    }

    static String integerReport(Map<String, Integer> data, String dataHeading) {
        if(data.size() < 1) {
            throw new IllegalArgumentException("Need at least one observation to show")
        }
        def endpointMaxLen = data.keySet()*.size().max()
        def timeMaxLen = data.values()*.toString()*.size().max()
        def headFormat = "%-${endpointMaxLen}s\t%-${timeMaxLen}s".toString()
        def lineFormat = "%-${endpointMaxLen}s\t%${timeMaxLen}d".toString()
        def lines = [sprintf(headFormat,'endpoint', dataHeading)]
        (lines + data.entrySet().sort({ Entry<String, Integer> e -> e.value}).collect({
            Entry<String, Integer> e -> sprintf(lineFormat, e.key, e.value)
        })).join('\n')
    }
}
