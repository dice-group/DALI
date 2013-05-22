/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.mapper.atomic.fastngram;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


/**
 *
 * @author ngonga
 */
public class FastNGram {

    static Logger logger = Logger.getLogger("DOODLE");
    static int q = 3;

    public String getName()
    {
        return "FastNGram";
    }
    
    
    public static Map<String, Map<String, Double>> compute(Set<String> source, Set<String> target, int q, double threshold) {
        Index index = new Index(q);
        double kappa = (1 + threshold) / threshold;
        QGramSimilarity sim = new QGramSimilarity(q);
        Tokenizer tokenizer = new NGramTokenizer();
        Map<String, Set<String>> targetTokens = new HashMap<String, Set<String>>();
        Map<String, Map<String, Double>> result = new HashMap<String, Map<String, Double>>();
        //index target
        for (String t : target) {
            targetTokens.put(t, index.addString(t));
        }

        Set<String> candidates1;
        Set<String> candidates2;
        //run similarity computation

        for (String s : source) {
            Set<Integer> allSizes = index.getAllSizes();
            Set<String> sourceTokens = tokenizer.tokenize(s, q);
            double sourceSize = (double) sourceTokens.size();
            for (int size = (int) Math.ceil(sourceSize * threshold); size <= (int) Math.floor(sourceSize / threshold); size++) {
                if (allSizes.contains(size)) {
                    //maps tokens to strings
                    Map<String, Set<String>> stringsOfSize = index.getStrings(size);
                    Map<String, Integer> countMap = new HashMap<String, Integer>();
                    for (String token : sourceTokens) {
                        if (stringsOfSize.containsKey(token)) {
                            //take each string and add it to the count map
                            Set<String> candidates = stringsOfSize.get(token);
                            for (String candidate : candidates) {
                                if (!countMap.containsKey(candidate)) {
                                    countMap.put(candidate, 0);
                                }
                                countMap.put(candidate, countMap.get(candidate) + 1);
                            }
                        }
                    }
                    // now apply filtering |X \cap Y| \geq \kappa(|X| + |Y|)
                    for (String candidate : countMap.keySet()) {
                        double count = (double) countMap.get(candidate);
                        if (kappa * count >= (sourceSize + size)) {
                            double similarity = sim.getSimilarity(targetTokens.get(candidate), sourceTokens);
                            if (similarity >= threshold) {
                                if(!result.containsKey(s))
                                    result.put(s, new HashMap<String, Double>());
                                result.get(s).put(candidate, similarity);
                            }
                        }
                    }
                }
            }

        }
        return result;
    }
}
