package edu.umn.cs.recsys.ii;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.util.collections.LongUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@DefaultProvider(SimpleItemItemModelBuilder.class)
public class SimpleItemItemModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long2DoubleMap itemMeans;
    private final Map<Long,Long2DoubleMap> neighborhoods;

    /**
     * Create a new item-item model.
     * @param nbrhoods A mapping of items to neighborhoods.  The neighborhoods
     *                 must be sorted by similarity in non-increasing order (most
     *                 similar neighbors first).  They should not have any negative
     *                 scores.
     */
    public SimpleItemItemModel(Long2DoubleMap means, Map<Long,Long2DoubleMap> nbrhoods) {
        itemMeans = LongUtils.frozenMap(means);
        neighborhoods = nbrhoods;
    }

    /**
     * Get the vector of item mean ratings.
     * @return The vector of item mean ratings.
     */
    public Long2DoubleMap getItemMeans() {
        return itemMeans;
    }

    /**
     * Get the neighbors of an item.
     * @return The neighbors of the item, sorted by decreasing score.
     */
    public Long2DoubleMap getNeighbors(long item) {
        Long2DoubleMap nbrs = neighborhoods.get(item);
        if (nbrs == null) {
            return new Long2DoubleOpenHashMap();
        } else {
            return nbrs;
        }
    }
}
