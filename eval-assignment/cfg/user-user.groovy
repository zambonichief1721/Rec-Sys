package edu.umn.cs.recsys;

import org.lenskit.knn.user.UserUserItemScorer;
import org.lenskit.knn.NeighborhoodSize;
import org.grouplens.lenskit.vectors.similarity.PearsonCorrelation;
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity;
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity;
import org.grouplens.lenskit.transform.normalize.VectorNormalizer
import org.grouplens.lenskit.transform.normalize.MeanCenteringVectorNormalizer

for (nnbrs in [5, 10, 15, 20, 25, 30, 40, 50, 75, 100]) {
    algorithm("UserUser") {
        include 'tag-setup.groovy'
        include 'fallback.groovy'
// Attributes let you specify additional properties of the algorithm.
// They go in the output file, so you can do things like plot accuracy
// by neighborhood size
        attributes["NNbrs"] = nnbrs
// use the user-user rating predictor
        bind ItemScorer to UserUserItemScorer
        set NeighborhoodSize to nnbrs
        bind VectorSimilarity to PearsonCorrelation
    }
    algorithm("UserUserNorm") {
        include 'tag-setup.groovy'
        include 'fallback.groovy'
// Attributes let you specify additional properties of the algorithm.
// They go in the output file, so you can do things like plot accuracy
// by neighborhood size
        attributes["NNbrs"] = nnbrs
// use the user-user rating predictor
        bind ItemScorer to UserUserItemScorer
        set NeighborhoodSize to nnbrs
        bind VectorNormalizer to MeanCenteringVectorNormalizer
        bind VectorSimilarity to PearsonCorrelation
    }
    algorithm("UserUserCosine") {
        include 'tag-setup.groovy'
        include 'fallback.groovy'
// Attributes let you specify additional properties of the algorithm.
// They go in the output file, so you can do things like plot accuracy
// by neighborhood size
        attributes["NNbrs"] = nnbrs
// use the user-user rating predictor
        bind ItemScorer to UserUserItemScorer
        set NeighborhoodSize to nnbrs
        bind VectorNormalizer to MeanCenteringVectorNormalizer
        bind VectorSimilarity to CosineVectorSimilarity
    }
}
