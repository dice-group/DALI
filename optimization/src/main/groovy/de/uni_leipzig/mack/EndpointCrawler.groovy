package de.uni_leipzig.mack

import aksw.org.doodle.engine.DescriptionCollector
import com.google.common.base.Optional
import com.google.common.base.Stopwatch
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import groovy.time.Duration
import groovy.transform.TypeChecked
import groovy.util.logging.Log4j
import groovyx.gpars.GParsPool

import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

import static java.util.Map.*

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
        GParsPool.withPool {
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


    Map<String,Integer> genericResponsTimes(List<String> endpointUris) {
        def probeRespTime = { String uri ->
            def sw = new Stopwatch().start()
            DescriptionCollector.isAlive(uri)
            sw.elapsed(TimeUnit.MILLISECONDS).toInteger()
        }

        def respTimeMap = collectEndpointsData(endpointUris,probeRespTime ,300)
        ImmutableMap.copyOf(respTimeMap)
    }



    int collectRespsonseTimes(List<String> endpointUris) {
        (int) GParsPool.withPool {
            Closure<Boolean> cl =  { String uri -> measureResponseTime(uri) }
            endpointUris.collectParallel(cl).grep().size()
        }
    }

    protected boolean measureResponseTime(String endpointUri) {
        def sw = new Stopwatch().start()
        def future = executor.submit({ DescriptionCollector.isAlive(endpointUri) } as Callable<Boolean>)
        boolean alive = false
        try{
            alive = future.get(60, TimeUnit.SECONDS)
        } catch (TimeoutException toe) {
            logger.warn "Is-alive test for '$endpointUri' timed out"
        } catch (ExecutionException ee) {
            logger.warn "Exception during is-alive test for '$endpointUri':\n${ee}"
        }
        if(alive) {
            responseTimes.putIfAbsent(endpointUri, sw.elapsed(TimeUnit.MILLISECONDS).toInteger())
        }
        return alive
    }

    protected static String responseTimeReport(Map<String, Integer> respTimes) {
        def endpointMaxLen = respTimes.keySet()*.size().max()
        def timeMaxLen = respTimes.values()*.toString()*.size().max() /*.max { int t -> t.toString().size() }*/
        def headFormat = "%-${endpointMaxLen}s\t%-${timeMaxLen}s".toString()
        def lineFormat = "%-${endpointMaxLen}s\t%${timeMaxLen}d".toString()
        def lines = [sprintf(headFormat,'endpoint', 'time')]
        (lines + respTimes.entrySet().sort({ Entry<String, Integer> e -> e.value}).collect({
            Entry<String, Integer> e -> sprintf(lineFormat, e.key, e.value)
        })).join('\n')
    }
}
