package aksw.org.doodle.similarity;

import aksw.org.doodle.dataset.Description;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public abstract class SelfSimNorm implements Similarity {

    Similarity baseSimilarity;

    public SelfSimNorm() {
        this.baseSimilarity = initBaseSimilarity();
    }

    protected abstract Similarity initBaseSimilarity();

    public Similarity getBaseSimilarity() {
        return baseSimilarity;
    }

    @Override
    public double getSimilarity(Description d1, Description d2) {
        return (2 * baseSimilarity.getSimilarity(d1,d2)) /
               (baseSimilarity.getSimilarity(d1,d1) + baseSimilarity.getSimilarity(d2,d2));
    }
}
