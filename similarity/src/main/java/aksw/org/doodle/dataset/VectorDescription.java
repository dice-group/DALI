/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aksw.org.doodle.dataset;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ngonga
 */
public class VectorDescription implements Description, Serializable {

    public Map<String, Double> features;
    String label;
    
    public VectorDescription(String name) {
        label = name;
        features = new HashMap<String, Double>();
    }

    public String getName()
    {
        return label;
    }
    
    public double squaredLength() {
        double l = 0;
        for (String key : features.keySet()) {
            l = l + features.get(key) * features.get(key);
        }
        return l;
    }
    
    public String toString()
    {
        String result = "";
        return features.toString();
    }
}
