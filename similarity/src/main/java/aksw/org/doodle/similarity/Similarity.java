/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aksw.org.doodle.similarity;

import aksw.org.doodle.dataset.Description;

/**
 *
 * @author ngonga
 */
public interface Similarity {
    double getSimilarity(Description d1, Description d2);
}
