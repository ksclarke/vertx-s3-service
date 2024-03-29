
package info.freelibrary.vertx.s3;

import java.text.ParseException;
import java.time.Instant;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.vertx.s3.util.DateUtils;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests of the S3 list object.
 */
@RunWith(VertxUnitRunner.class)
public class ListObjectTest {

    /**
     * Tests setting and getting the object key.
     *
     * @param aContext A test context
     */
    @Test
    public final void testSetKey(final TestContext aContext) {
        final String key = UUID.randomUUID().toString();
        aContext.assertEquals(key, new S3Object().setKey(key).getKey());
    }

    /**
     * Tests setting and getting the ETag.
     *
     * @param aContext A test context
     */
    @Test
    public final void testSetETag(final TestContext aContext) {
        final String eTag = UUID.randomUUID().toString();
        aContext.assertEquals(eTag, new S3Object().setETag(eTag).getETag());
    }

    /**
     * Tests setting and getting the last updated timestamp.
     *
     * @param aContext A test context
     * @throws ParseException If there is trouble parsing the date-time string
     */
    @Test
    public final void testSetLastUpdated(final TestContext aContext) throws ParseException {
        final Instant instant = DateUtils.parse("2020-04-26T04:34:02.920Z");
        aContext.assertEquals(instant, new S3Object().setLastUpdated(instant).getLastUpdated());
    }

    /**
     * Tests setting and getting the object size.
     *
     * @param aContext A test context
     * @throws NumberFormatException If the supplied size isn't an integer
     */
    @Test
    public final void testSetSize(final TestContext aContext) throws NumberFormatException {
        final long size = 80;
        aContext.assertEquals(size, new S3Object().setSize(size).getSize());
    }

    /**
     * Tests setting and getting the storage class.
     *
     * @param aContext A test context
     */
    @Test
    public final void testSetStorageClass(final TestContext aContext) {
        final String storageClass = "STANDARD";
        aContext.assertEquals(storageClass, new S3Object().setStorageClass(storageClass).getStorageClass());
    }

}
