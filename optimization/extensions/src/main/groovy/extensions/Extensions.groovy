package extensions

import groovy.transform.TypeChecked
import org.w3c.dom.Element

@TypeChecked
class Extensions {

    static <K, V> V getWithDefault(Map<K, V> self, K key, Closure<V> defaultProvider) {
        def value = self.get(key)

        if (value == null && !(key in self)) {
            if (defaultProvider.maximumNumberOfParameters > 1)
                throw new IllegalArgumentException()
            if (defaultProvider.maximumNumberOfParameters == 1 && K.class.isCase(defaultProvider.parameterTypes[0])) {
                value = defaultProvider.call(key)
            } else {
                value = defaultProvider.call()
            }
            self.put(key, value)
        }
        return value
    }

    static <K,V> HashMap<K,V> filterByKeys(Map<K, V> self, Collection<K> keys) {
        def ret = new HashMap<K,V>(keys.size())
        self.entrySet().each { Map.Entry<K,V> e ->
            if(e.key in keys) ret.put(e.key, e.value)
        }
        return ret
    }

    static <K,V,T> HashMap<K,T> convertValues(Map<K, V> self, Closure<T> converter) {
        def ret = new HashMap<K,T>(self.size())
        self.entrySet().each { Map.Entry<K,V> e ->
            ret.put(e.key, (T) converter.call(e.value))
        }
        return ret
    }

    static Map<String,String> getAttributeMap(Element self) {
        def attr = self.attributes
        def ret = new HashMap<String,String>()
        for(i in 0..<attr.length) {
            def a = attr.item(i)
            ret.put(a.nodeName, a.nodeValue)
        }
        (Map<String,String>) ret
    }

    /*static ImmutableList<String> tokenize(String self, String delim) {
        def tokenizer = new StringTokenizer(self, delim)
        def tokens = new LinkedList<String>()
        while(tokenizer.hasMoreTokens()) tokens.add(tokenizer.nextToken())
        return ImmutableList.copyOf(tokens)
    }*/

    // compilation fails: unable to resolve class T
    /*static <T> Iterable<T> makeIterable(Iterator<T> self) {
        final Iterator<T> selfVar = self

        return new Iterable<T>() {

            @Override
            Iterator<T> iterator() {
                return selfVar
            }
        }
    }*/
}
