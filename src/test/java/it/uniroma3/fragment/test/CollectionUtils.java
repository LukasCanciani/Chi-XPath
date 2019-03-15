package it.uniroma3.fragment.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilities methods to make assertions with collections less verbose.
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class CollectionUtils {

    /**
     * An utility method to extract a singleton element from a collection.
     * It checks that a collection is not null, is a singleton-collection and its only element
     * is not null
     * @param <T> the formal type of the hosted elements
     * @param collection the collection to check
     * @return the only non-null element of the collection
     */
    static public <T> T unique(String msg, Collection<T> collection) {
        assertNotNull(msg+"\nCollection is null.", collection);
        assertEquals("exactly one element expected in: "+collection, 1, collection.size());
        final T result = collection.iterator().next();
        assertNotNull(msg+"\nSingleton element is null",result);
        return result;
    }

    static public <T> T unique(Collection<T> collection) {
        return unique("",collection);
    }

    /**
     * An utility method to convert an array of elements with the same type
     * to a {@link java.util.Set}
     * 
     * @param <T> formal type of hosted elements
     * @param elements the elements to insert in the set
     * @return a set with the given elements
     */
    @SafeVarargs
    static public <T> Set<T> setOf(T... elements) {
        assertNotNull(elements);
        return new HashSet<T>(Arrays.asList(elements));
    }

}
