/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.mapper.atomic.fastngram;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ngonga
 */
public class QGramStringSimilarity {

    Tokenizer tokenizer;
    int q = 3;

    public QGramStringSimilarity(int q) {
        this();
        this.q = q;
    }

    public QGramStringSimilarity() {
        tokenizer = new NGramTokenizer();
    }

    public double getSimilarity(String x, String y) {
        Set<String> yTokens = tokenizer.tokenize(y, q);
        Set<String> xTokens = tokenizer.tokenize(x, q);
        return getSimilarity(xTokens, yTokens);
    }

    public double getSimilarity(Set<String> X, Set<String> Y) {
        double x = (double) X.size();
        double y = (double) Y.size();
        //create a copy of X
        Set<String> K = new HashSet<String>(X);
        K.retainAll(Y);
        double z = (double) K.size();
        return z / (x + y - z);
    }

    public static void main(String args[]) {
        System.out.println(new QGramStringSimilarity().getSimilarity("abcd", "abcde"));
    }

    
    public String getType() {
        return "string";
    }
    
    public String getName() {
        return "qgrams";
    }

    public double getRuntimeApproximation(double mappingSize) {
        return mappingSize/10000d;
    }
}
