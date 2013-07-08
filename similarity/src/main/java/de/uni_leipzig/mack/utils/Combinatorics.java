package de.uni_leipzig.mack.utils;

import java.util.*;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class Combinatorics {

    public static <T> Set<CombinationPair<T>> unorderedCombinations(Collection<T> A, Collection<T> B) {
        final Set<CombinationPair<T>> unorderedCombinations = new HashSet<CombinationPair <T>>();
        for(T a : A) {
            for(T b: B) {
                unorderedCombinations.add(new CombinationPair<T>(a,b));
            }
        }
        return unorderedCombinations;
    }

    public static <T> List<CombinationPair<T>> orderedPairs(Set<T> A, Set<T> B) {
        final List<CombinationPair<T>> pairs = new LinkedList<CombinationPair<T>>();
        for(T a : A) {
            for(T b: B) {
                pairs.add(new CombinationPair<T>(a,b));
            }
        }
        return pairs;
    }

    public static class CombinationPair<T> {
        private T first;
        private T second;

        public CombinationPair(T first, T second) {
            this.first = first;
            this.second = second;
        }

        public T getFirst() {
            return first;
        }

        public T getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CombinationPair that = (CombinationPair) o;

            if (first != null ? !first.equals(that.first) : that.first != null) return false;
            if (second != null ? !second.equals(that.second) : that.second != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = first != null ? first.hashCode() : 0;
            result = 31 * result + (second != null ? second.hashCode() : 0);
            return result;
        }
    }
}
