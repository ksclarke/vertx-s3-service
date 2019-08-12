
package info.freelibrary.vertx.s3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for the UserMetadata class.
 */
public class UserMetadataTest {

    /**
     * Tests creating new user metadata from a supplied name-value pair.
     */
    public final void newUserMetadataStringString() {
        final String name = "name";
        final String value = "value";
        final UserMetadata metadata = new UserMetadata(name, value);

        assertEquals(name, metadata.getName(0));
        assertEquals(value, metadata.getValue(0));
    }

    /**
     * Tests getting the name for the metadata at the supplied index position.
     */
    @Test
    public final void testGetNameInt() {
        final String name = "aaaa";
        final String value = "cccc";
        final UserMetadata metadata = new UserMetadata().add(name, value);

        assertEquals(name, metadata.getName(0));
    }

    /**
     * Tests getting the value for the metadata at the supplied index position.
     */
    @Test
    public final void testGetValueInt() {
        final String name = "bbbb";
        final String value = "dddd";
        final UserMetadata metadata = new UserMetadata().add(name, value);

        assertEquals(value, metadata.getValue(0));
    }

    /**
     * Tests getting the value associated with the supplied name.
     */
    @Test
    public final void testGetValueString() {
        final String name = "qqqq";
        final String value = "pppp";
        final UserMetadata metadata = new UserMetadata().add(name, value);

        assertEquals(value, metadata.getValue(name));
    }

    /**
     * Tests adding a name-value pair to user metadata.
     */
    @Test
    public final void testAddSimple() {
        final String name = "peach";
        final String value = "grape";
        final UserMetadata metadata = new UserMetadata().add(name, value);

        assertEquals(1, metadata.count());
        assertTrue(metadata.contains(name));
        assertEquals(value, metadata.getValue(name));
    }

    /**
     * Tests getting the index of the supplied name.
     */
    @Test
    public final void testIndexOfString() {
        final String name1 = "good";
        final String name2 = "wrong";
        final UserMetadata metadata = new UserMetadata().add(name1, "evil");

        assertEquals(0, metadata.indexOf(name1));
        metadata.add(name2, "right");
        assertEquals(1, metadata.indexOf(name2));
    }

    /**
     * Tests adding multiple name-value pairs to user metadata.
     */
    @Test
    public final void testAddMultiple() {
        final String name1 = "sky";
        final String name2 = "rock";
        final String value1 = "ocean";
        final String value2 = "tree";
        final UserMetadata metadata = new UserMetadata().add(name1, value1).add(name2, value2);

        assertEquals(2, metadata.count());
        assertTrue(metadata.contains(name1));
        assertTrue(metadata.contains(name2));
        assertEquals(value1, metadata.getValue(name1));
        assertEquals(value2, metadata.getValue(name2));
    }

    /**
     * Tests adding the same metadata property (with different metadata values) to user metadata.
     */
    @Test
    public final void testAddSame() {
        final String name1 = "fridge";
        final String value1 = "ac";
        final String value2 = "heater";
        final UserMetadata metadata = new UserMetadata().add(name1, value1).add(name1, value2);

        assertEquals(1, metadata.count());
        assertTrue(metadata.contains(name1));
        assertEquals(value1 + ',' + value2, metadata.getValue(name1));
    }

    /**
     * Tests removing a metadata property from user metadata.
     */
    @Test
    public final void testRemove() {
        final String name = "stop";
        final UserMetadata metadata = new UserMetadata().add(name, "start");

        assertEquals(1, metadata.count());
        assertEquals(0, metadata.remove(name).count());
    }

    /**
     * Tests whether the supplied name is contained in the metadata.
     */
    @Test
    public final void testContains() {
        final String name = "black";
        final UserMetadata metadata = new UserMetadata().add(name, "white");

        assertTrue(metadata.contains(name));
        assertFalse(metadata.contains("nothing"));
    }
}
