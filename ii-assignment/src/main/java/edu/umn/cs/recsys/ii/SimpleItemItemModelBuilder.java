package edu.umn.cs.recsys.ii;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity;
import org.lenskit.data.dao.ItemDAO;
import org.lenskit.data.dao.ItemEventDAO;
import org.lenskit.data.history.ItemEventCollection;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.Ratings;
import org.lenskit.inject.Transient;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SimpleItemItemModelBuilder implements Provider<SimpleItemItemModel> {
    private static final Logger logger = LoggerFactory.getLogger(SimpleItemItemModelBuilder.class);


    private final ItemDAO itemDao;
    private final ItemEventDAO itemEventDAO;

    @Inject
    public SimpleItemItemModelBuilder(@Transient ItemDAO idao,
                                      @Transient ItemEventDAO iedao) {
        itemDao = idao;
        itemEventDAO = iedao;
    }

    @Override
    public SimpleItemItemModel get() {
        // Create a map to store item rating vectors
        Map<Long,Long2DoubleMap> itemVectors = Maps.newHashMap(); //map item to its rating vectors
        // And a vector to store item mean ratings
        Long2DoubleMap itemMeans = new Long2DoubleOpenHashMap(); //maps item id to item mean rating

        ObjectStream<ItemEventCollection<Rating>> stream = itemEventDAO.streamEventsByItem(Rating.class);
        try {
            for (ItemEventCollection<Rating> item : stream) {
                Long2DoubleOpenHashMap ratings = new Long2DoubleOpenHashMap(Ratings.itemRatingVector(item)); //user to rating of item
                // TODO - normalize the item vector before putting it in the itemVectors map
                MutableSparseVector ratingsv = MutableSparseVector.create(ratings);
                double mean =  ratingsv.mean();
                itemMeans.put(item.getItemId(), mean);


                // TODO Store the item's mean rating in itemMeans
                for(Map.Entry<Long, Double> e: ratings.entrySet()){
                    e.setValue(e.getValue() - mean);
                }

                itemVectors.put(item.getItemId(), LongUtils.frozenMap(ratings));

            }
        } finally {
            stream.close();
        }

        // Get all items - you might find this useful
        LongSortedSet items = LongUtils.packedSet(itemVectors.keySet());
        // Map items to vectors of item similarities
        Map<Long,Long2DoubleMap> itemSimilarities = Maps.newHashMap();

        // TODO Compute the similarities between each pair of items
        // Ignore nonpositive similarities

        for(long i: itemVectors.keySet()) {
            itemSimilarities.put(i, new Long2DoubleOpenHashMap());
        }

        CosineVectorSimilarity cosineVectorSimilarity = new CosineVectorSimilarity();
        for(long i : items){
            MutableSparseVector vectori = MutableSparseVector.create(itemVectors.get(i));
            for(long j: items){
                MutableSparseVector vectorj = MutableSparseVector.create(itemVectors.get(j));
                double sim = cosineVectorSimilarity.similarity(vectori, vectorj);
                if(sim > 0){
                    itemSimilarities.get(i).put(j, sim);
                }
            }
        }

        return new SimpleItemItemModel(LongUtils.frozenMap(itemMeans), itemSimilarities);
    }

    /**
     * Load the data into memory, indexed by item.
     * @return A map from item IDs to item rating vectors. Each vector contains users' ratings for
     * the item, keyed by user ID.
     */
    public Map<Long,Long2DoubleMap> getItemVectors() {
        Map<Long,Long2DoubleMap> itemData = Maps.newHashMap();

        ObjectStream<ItemEventCollection<Rating>> stream = itemEventDAO.streamEventsByItem(Rating.class);
        try {
            for (ItemEventCollection<Rating> item: stream) {
                Long2DoubleMap vector = Ratings.itemRatingVector(item);

                // Compute and store the item's mean.
                double mean = Vectors.mean(vector);

                // Mean center the ratings.
                for (Map.Entry<Long, Double> entry : vector.entrySet()) {
                    entry.setValue(entry.getValue() - mean);
                }

                itemData.put(item.getItemId(), LongUtils.frozenMap(vector));
            }
        } finally {
            stream.close();
        }

        return itemData;
    }
}
