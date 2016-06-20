package org.springframework.roo.support.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * Unit test of {@link CollectionUtils}
 * 
 * @author Andrew Swan
 * @since 1.2.0
 */
public class CollectionUtilsTest {

    private static class Child extends Parent {
    }

    private static class Parent {
        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    // A simple filter for testing the filtering methods
    private static final Filter<String> NON_BLANK_FILTER = new Filter<String>() {
        public boolean include(final String instance) {
            return StringUtils.isNotBlank(instance);
        }
    };

    @Test
    public void testFilterNonNullIterableWithNonNullFilter() {
        // Set up
        final Iterable<String> inputs = Arrays.asList("a", "", null, "b");

        // Invoke
        final List<? extends String> results = CollectionUtils.filter(inputs,
                NON_BLANK_FILTER);

        // Check
        assertEquals(Arrays.asList("a", "b"), results);
    }

    @Test
    public void testFilterNonNullIterableWithNullFilter() {
        // Set up
        final Iterable<String> inputs = Arrays.asList("a", "");

        // Invoke
        final List<? extends String> results = CollectionUtils.filter(inputs,
                null);

        // Check
        assertEquals(inputs, results);
    }

    @Test
    public void testFilterNullCollection() {
        assertEquals(0, CollectionUtils.filter(null, NON_BLANK_FILTER).size());
    }

    @Test
    public void testFirstElementOfEmptyCollection() {
        assertNull(CollectionUtils.firstElementOf(Collections.emptySet()));
    }

    @Test
    public void testFirstElementOfMultiElementCollection() {
        final String[] members = { "x", "y", "z" };
        assertEquals(members[0],
                CollectionUtils.firstElementOf(Arrays.asList(members)));
    }

    @Test
    public void testFirstElementOfNullCollection() {
        assertNull(CollectionUtils.firstElementOf(null));
    }

    @Test
    public void testFirstElementOfSingleElementCollection() {
        final String member = "x";
        assertEquals(member,
                CollectionUtils.firstElementOf(Collections.singleton(member)));
    }

    @Test
    public void testPopulateNonNullCollectionWithNonNullCollection() {
        // Set up
        final Collection<Parent> originalCollection = new ArrayList<Parent>();
        originalCollection.add(new Parent());
        final Child child = new Child();

        // Invoke
        final Collection<Parent> result = CollectionUtils.populate(
                originalCollection, Arrays.asList(child));

        // Check
        assertEquals(Collections.singletonList(child), result);
    }

    @Test
    public void testPopulateNonNullCollectionWithNullCollection() {
        // Set up
        final Collection<Parent> collection = new ArrayList<Parent>();
        collection.add(new Parent());

        // Invoke
        final Collection<Parent> result = CollectionUtils.populate(collection,
                null);

        // Check
        assertEquals(0, result.size());
    }

    @Test
    public void testPopulateNullCollectionWithNullCollection() {
        assertNull(CollectionUtils.populate(null, null));
    }
}
