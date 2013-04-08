/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aksw.org.doodle.similarity;

import aksw.org.doodle.dataset.Description;
import aksw.org.doodle.dataset.VectorDescription;

/**
 *
 * @author ngonga
 */
public class CosineSimilarity implements Similarity{

    public double getSimilarity(Description d1, Description d2) {
        if (d1 instanceof VectorDescription && d2 instanceof VectorDescription)
        {
            VectorDescription vd1 = (VectorDescription)d1;
            VectorDescription vd2 = (VectorDescription)d2;
            
            double result = 0d; 
            for(String key: vd1.features.keySet())
            {
                if(vd2.features.containsKey(key))
                {
                    result = result + vd1.features.get(key)*vd2.features.get(key);
                }
            }
            return result/Math.sqrt(vd1.squaredLength()*vd2.squaredLength());
        }
        else return 0d;
    }
    
}
