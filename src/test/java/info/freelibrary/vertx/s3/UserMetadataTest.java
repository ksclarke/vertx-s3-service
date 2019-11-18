
package info.freelibrary.vertx.s3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the UserMetadata class.
 */
public class UserMetadataTest {

    private String myName1;

    private String myName2;

    private String myValue1;

    private String myValue2;

    @Before
    public void setUp() {
        myName1 = UUID.randomUUID().toString();
        myName2 = UUID.randomUUID().toString();
        myValue1 = UUID.randomUUID().toString();
        myValue2 = UUID.randomUUID().toString();
    }

    /**
     * Tests creating new user metadata from a supplied name-value pair.
     */
    public final void newUserMetadataStringString() {
        final UserMetadata metadata = new UserMetadata(myName1, myValue1);

        assertEquals(myName1, metadata.getName(0));
        assertEquals(myValue1, metadata.getValue(0));
    }

    /**
     * Tests getting the name for the metadata at the supplied index position.
     */
    @Test
    public final void testGetNameInt() {
        assertEquals(myName1, new UserMetadata().add(myName1, myValue1).getName(0));
    }

    /**
     * Tests getting the value for the metadata at the supplied index position.
     */
    @Test
    public final void testGetValueInt() {
        assertEquals(myValue1, new UserMetadata().add(myName1, myValue1).getValue(0));
    }

    /**
     * Tests getting the value associated with the supplied name.
     */
    @Test
    public final void testGetValueString() {
        final UserMetadata metadata = new UserMetadata().add(myName1, myValue1);

        assertEquals(myValue1, metadata.getValue(myName1));
    }

    @Test
    public final void testGetValueStringNull() {
        assertNull(new UserMetadata().add(myName1, myValue1).getValue("nothing"));
    }

    /**
     * Tests adding a name-value pair to user metadata.
     */
    @Test
    public final void testAddSimple() {
        final UserMetadata metadata = new UserMetadata().add(myName1, myValue1);

        assertEquals(1, metadata.count());
        assertTrue(metadata.contains(myName1));
        assertEquals(myValue1, metadata.getValue(myName1));
    }

    /**
     * Tests adding a name-value pair to user metadata.
     */
    @Test
    public final void testAddNull() {
        final UserMetadata metadata = new UserMetadata().add(myName1, null);

        assertEquals(1, metadata.count());
        assertTrue(metadata.contains(myName1));
        assertEquals(null, metadata.getValue(myName1));
    }

    /**
     * Tests getting the index of the supplied name.
     */
    @Test
    public final void testIndexOfString() {
        final UserMetadata metadata = new UserMetadata().add(myName1, myValue1);

        assertEquals(0, metadata.indexOf(myName1));
        metadata.add(myName2, myValue2);
        assertEquals(1, metadata.indexOf(myName2));
    }

    /**
     * Tests adding multiple name-value pairs to user metadata.
     */
    @Test
    public final void testAddMultiple() {
        final UserMetadata metadata = new UserMetadata().add(myName1, myValue1).add(myName2, myValue2);

        assertEquals(2, metadata.count());
        assertTrue(metadata.contains(myName1));
        assertTrue(metadata.contains(myName2));
        assertEquals(myValue1, metadata.getValue(myName1));
        assertEquals(myValue2, metadata.getValue(myName2));
    }

    /**
     * Tests adding the same metadata property (with different metadata values) to user metadata.
     */
    @Test
    public final void testAddSame() {
        final UserMetadata metadata = new UserMetadata().add(myName1, myValue1).add(myName1, myValue2);

        assertEquals(1, metadata.count());
        assertTrue(metadata.contains(myName1));
        assertEquals(myValue1 + ',' + myValue2, metadata.getValue(myName1));
    }

    /**
     * Tests removing a metadata property from user metadata.
     */
    @Test
    public final void testRemove() {
        final UserMetadata metadata = new UserMetadata().add(myName1, myValue1);

        assertEquals(1, metadata.count());
        assertEquals(0, metadata.remove(myName1).count());
    }

    /**
     * Tests whether the supplied name is contained in the metadata.
     */
    @Test
    public final void testContains() {
        final UserMetadata metadata = new UserMetadata().add(myName1, myValue1);

        assertTrue(metadata.contains(myName1));
        assertFalse(metadata.contains(myValue2));
    }
}
