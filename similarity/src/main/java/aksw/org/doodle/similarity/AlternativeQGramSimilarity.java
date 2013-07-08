package aksw.org.doodle.similarity;

import aksw.org.doodle.dataset.Description;
import aksw.org.doodle.dataset.VectorDescription;
import de.uni_leipzig.mack.utils.Combinatorics;
import de.uni_leipzig.simba.mapper.atomic.fastngram.QGramStringSimilarity;

import java.util.Collection;

import static de.uni_leipzig.mack.utils.Combinatorics.CombinationPair;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class AlternativeQGramSimilarity extends SelfSimNorm{

    @Override
    protected Similarity initBaseSimilarity() {
        return new NonNormalized();
    }

    protected static class NonNormalized implements Similarity {
        @Override
        public double getSimilarity(Description d1, Description d2) {
            QGramStringSimilarity sim = new QGramStringSimilarity();

            if (d1 instanceof VectorDescription && d2 instanceof VectorDescription)
            {
                VectorDescription vd1 = (VectorDescription)d1;
                VectorDescription vd2 = (VectorDescription)d2;

                double nominator = 0d;
                double denominator = 0d;

                Collection<CombinationPair<String>> combinations =
                        Combinatorics.orderedPairs(vd1.features.keySet(), vd2.features.keySet());

                for(CombinationPair<String> pair : combinations) {
                    double weightProduct = vd1.features.get(pair.getFirst()) * vd2.features.get(pair.getSecond());
                    nominator += sim.getSimilarity(pair.getFirst(), pair.getSecond())* weightProduct;
                    denominator += weightProduct;
                }

                return nominator/denominator;
            }
            else return 0d;
        }
    }
}
