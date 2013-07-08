/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aksw.org.doodle.similarity;

import aksw.org.doodle.dataset.Description;
import aksw.org.doodle.dataset.VectorDescription;
import de.uni_leipzig.simba.mapper.atomic.fastngram.QGramStringSimilarity;

/**
 *
 * @author ngonga
 */
public class QGramSimilarity extends SelfSimNorm{

    @Override
    protected Similarity initBaseSimilarity() {
        return new NonNormalized();
    }

    protected static class NonNormalized implements Similarity{
        public double getSimilarity(Description d1, Description d2) {
            QGramStringSimilarity sim = new QGramStringSimilarity();

            if (d1 instanceof VectorDescription && d2 instanceof VectorDescription)
            {
                VectorDescription vd1 = (VectorDescription)d1;
                VectorDescription vd2 = (VectorDescription)d2;

                double result = 0d;
                for(String key: vd1.features.keySet())
                {
                    for(String key2: vd2.features.keySet())
                    {
                        result += sim.getSimilarity(key, key2)*vd1.features.get(key)*vd2.features.get(key2);
                    }
                }
                return result/Math.sqrt(vd1.squaredLength()*vd2.squaredLength());
            }
            else return 0d;
        }
    }
}
