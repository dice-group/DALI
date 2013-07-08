package extensions;

import groovy.lang.Closure;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class JavaExtensions {

    public static <T> Iterable<T> makeIterable(Iterator<T> self) {
        final Iterator<T> iterator = self;

        return new Iterable<T>() {
            //@Override
            public Iterator<T> iterator() {
                return iterator;
            }
        };
    }

    public static <K, V> V get(Map<K, V> map, K key, Closure<V> defaultProvider) {
        if (!map.containsKey(key)) {
            map.put(key, defaultProvider.getMaximumNumberOfParameters() > 0 ? defaultProvider.call(key) :
                    defaultProvider.call());
        }
        return map.get(key);
    }
}
