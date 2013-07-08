package extensions

import com.google.common.collect.Multiset
import com.google.common.collect.Multiset.Entry
import com.google.common.collect.Multisets
import com.google.common.collect.Ordering
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */

@Category(Multiset)
@TypeChecked
class MultisetExtensions {
    @Lazy static final Ordering<Entry> GREATEST = buildDecreasingContOrder()

    @TypeChecked(TypeCheckingMode.SKIP)
    static Ordering<Entry> buildDecreasingContOrder() {
        return new Ordering<Entry>() {

            @Override
            int compare(Entry left, Entry right) {
                left.count > right.count
            }
        }
    }

    static double getTypeRatio(Multiset self) {
        return self.empty ? Double.NaN : (double) (self.elementSet().size() / self.size())
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    static <T> List<Entry<T>> mostFrequentEntries(Multiset<T> self, int n) {
        List<Entry<T>> list = Multisets.copyHighestCountFirst(self).entrySet().toList()
        return list.size() <= n ? list : list[0..<n]
    }

    static <T> List<T> mostFrequentElements(Multiset<T> self, int n) {
        return mostFrequentEntries(self, n).collect { Entry<T> e -> (T) e.element }
    }

}
