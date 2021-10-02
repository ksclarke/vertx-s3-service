
package info.freelibrary.vertx.s3;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.util.StringUtils;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests of the {@link S3BucketList} class.
 */
@RunWith(VertxUnitRunner.class)
public class BucketListTest {

    private static final String PREFIXED_KEY = "prefix_29737da4-0344-4e69-b512-f746667330e6";

    private static final String STANDARD_KEY = "e48af1f0-b745-4d11-ad8c-3b024ecde959";

    private S3BucketList myBucketList;

    /**
     * Creates a test S3BucketList.
     *
     * @param aContext A test context
     * @throws IOException If there is trouble constructing the S3BucketList
     */
    @Before
    public final void setUp(final TestContext aContext) throws IOException {
        myBucketList =
            new S3BucketList(StringUtils.read(new File("src/test/resources/list.xml"), StandardCharsets.UTF_8));
    }

    /**
     * Tests creating a new S3BucketList.
     *
     * @param aContext A test context
     */
    @Test
    public final void testNewBucketList(final TestContext aContext) {
        aContext.assertEquals(2, myBucketList.size());
    }

    /**
     * Tests getting an object through it's index position.
     *
     * @param aContext A test context
     */
    @Test
    public final void testGet(final TestContext aContext) {
        aContext.assertEquals(PREFIXED_KEY, myBucketList.get(0).getKey());
        aContext.assertEquals(STANDARD_KEY, myBucketList.get(1).getKey());
    }

    /**
     * Tests whether the bucket list is empty.
     *
     * @param aContext A test context
     */
    @Test
    public final void testIsEmptyFalse(final TestContext aContext) {
        aContext.assertFalse(myBucketList.isEmpty());
    }

    /**
     * Tests the iterator method.
     *
     * @param aContext A test context
     */
    @Test
    public final void testIterator(final TestContext aContext) {
        final Iterator<S3ObjectList> iterator = myBucketList.iterator();

        int index = 0;

        while (iterator.hasNext()) {
            switch (index++) {
                case 0:
                    aContext.assertEquals(PREFIXED_KEY, iterator.next().getKey());
                    break;
                case 1:
                    aContext.assertEquals(STANDARD_KEY, iterator.next().getKey());
                    break;
                default:
                    aContext.fail();
            }
        }
    }

    /**
     * Tests the forEach method.
     *
     * @param aContext A test context
     */
    @Test
    public final void testForEach(final TestContext aContext) {
        myBucketList.forEach(listObject -> {
            final String key = listObject.getKey();
            aContext.assertTrue(key.equals(PREFIXED_KEY) || key.equals(STANDARD_KEY));
        });
    }

    /**
     * Tests the iterator method.
     *
     * @param aContext A test context
     */
    @Test
    public final void testSpliterator(final TestContext aContext) {
        aContext.assertEquals(2L, myBucketList.spliterator().getExactSizeIfKnown());
    }

    /**
     * Tests the toArray method on S3BucketList.
     *
     * @param aContext A test context
     */
    @Test
    public final void testToArray(final TestContext aContext) {
        aContext.assertEquals(myBucketList.toArray(new S3ObjectList[myBucketList.size()]).length, myBucketList.size());
    }

    /**
     * Tests whether the bucket list is empty.
     *
     * @param aContext A test context
     */
    @Test
    public final void testIsEmptyTrue(final TestContext aContext) throws IOException {
        aContext.assertTrue(
            new S3BucketList(StringUtils.read(new File("src/test/resources/list-empty.xml"), StandardCharsets.UTF_8))
                .isEmpty());
    }

    /**
     * Test that our S3BucketList is iterable.
     *
     * @param aContext A testing context
     * @throws IOException If there is trouble reading the XML list test fixture
     */
    @Test
    public final void testIterableList(final TestContext aContext) {
        int count = 0;

        for (final S3ObjectList s3ObjectList : myBucketList) {
            aContext.assertNotNull(s3ObjectList.getKey());
            count += 1;
        }

        aContext.assertEquals(2, count);
    }

    /**
     * A test of the containsKey method.
     *
     * @param aContext A test context
     */
    @Test
    public final void testContainsKey(final TestContext aContext) {
        aContext.assertTrue(myBucketList.containsKey(STANDARD_KEY));
    }

    /**
     * A test of the containsKey method using a key that should not be found.
     *
     * @param aContext A test context
     */
    @Test
    public final void testContainsKeyFalse(final TestContext aContext) {
        aContext.assertFalse(myBucketList.containsKey(UUID.randomUUID().toString()));
    }

    /**
     * A test of the indexOfKey method.
     *
     * @param aContext A test context
     */
    @Test
    public final void testIndexOfKey(final TestContext aContext) {
        aContext.assertEquals(1, myBucketList.indexOfKey(STANDARD_KEY));
    }

    /**
     * A test of the indexOfKey method using a key that should not be found.
     *
     * @param aContext A test context
     */
    @Test
    public final void testIndexOfKeyFalse(final TestContext aContext) {
        aContext.assertEquals(-1, myBucketList.indexOfKey(UUID.randomUUID().toString()));
    }
}
