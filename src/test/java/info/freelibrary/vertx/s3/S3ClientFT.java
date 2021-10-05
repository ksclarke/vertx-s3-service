
package info.freelibrary.vertx.s3;

import java.io.File;
import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import info.freelibrary.vertx.s3.util.MessageCodes;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Runs tests against a LocalStack S3 instance.
 */
@RunWith(VertxUnitRunner.class)
@Ignore
public class S3ClientFT extends AbstractS3FT {

    /**
     * The S3 client test logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientFT.class, MessageCodes.BUNDLE);

    /**
     * A test file to use with the S3 client.
     */
    private static final String TEST_FILE = "src/test/resources/green.gif";

    /**
     * A S3 prefix.
     */
    private static final String PREFIX = "prefix_";

    /**
     * The S3 client being tested.
     */
    private S3Client myS3Client;

    /**
     * Sets up the testing environment.
     */
    @Override
    @Before
    public void setUp() {
        super.setUp();
        myS3Client = new S3Client(myContext.vertx(), getConfig());
    }

    /**
     * Tests the HEAD request that returns a future.
     *
     * @param aContext A test context
     */
    @Test
    public final void testHeadBucketKey(final TestContext aContext) {
        final Async asyncTask = aContext.async();

        putTestObject(myKey);

        myS3Client.head(myBucket, myKey).onComplete(head -> {
            if (head.succeeded()) {
                aContext.assertEquals("85", head.result().get(HttpHeaders.CONTENT_LENGTH));
                complete(asyncTask);
            } else {
                aContext.fail(head.cause());
            }
        });
    }

    /**
     * Tests getting the head of an object using the bucket name and object key.
     *
     * @param aContext A test context
     */
    @Test
    public final void testHeadBucketKeyWithHandler(final TestContext aContext) {
        final Async asyncTask = aContext.async();

        putTestObject(myKey);

        myS3Client.head(myBucket, myKey, head -> {
            if (head.succeeded()) {
                aContext.assertEquals(85, Integer.parseInt(head.result().get(HttpHeaders.CONTENT_LENGTH)));
                complete(asyncTask);
            } else {
                aContext.fail(head.cause());
            }
        });
    }

    /**
     * Tests the GET request that returns a future.
     *
     * @param aContext A test context
     */
    @Test
    public final void testGetBucketKey(final TestContext aContext) {
        final Async asyncTask = aContext.async();

        putTestObject(myKey);

        myS3Client.get(myBucket, myKey).onComplete(get -> {
            if (get.succeeded()) {
                get.result().body(body -> {
                    if (body.succeeded()) {
                        aContext.assertEquals(85, body.result().length());
                        complete(asyncTask);
                    } else {
                        aContext.fail(body.cause());
                    }
                });
            } else {
                aContext.fail(get.cause());
            }
        });
    }

    /**
     * Tests getting an object from a bucket using the bucket name and key.
     *
     * @param aContext A test context
     */
    @Test
    public final void testGetBucketKeyWithHandler(final TestContext aContext) {
        final Async asyncTask = aContext.async();

        putTestObject(myKey);

        myS3Client.get(myBucket, myKey, get -> {
            if (get.succeeded()) {
                get.result().body(body -> {
                    aContext.assertEquals(85, body.result().length());
                    complete(asyncTask);
                });
            } else {
                aContext.fail(get.cause());
            }
        });
    }

    /**
     * Tests listing the supplied bucket.
     *
     * @param aContext A testing context
     */
    @Test
    public final void testListBucket(final TestContext aContext) {
        final Async asyncTask = aContext.async();

        putTestObject(myKey);

        myS3Client.list(myBucket).onComplete(list -> {
            if (list.succeeded()) {
                aContext.assertEquals(1, list.result().size());
                complete(asyncTask);
            } else {
                aContext.fail(list.cause());
            }
        });
    }

    /**
     * Tests listing the supplied bucket.
     *
     * @param aContext A testing context
     */
    @Test
    public final void testListBucketWithHandler(final TestContext aContext) {
        final Async asyncTask = aContext.async();

        putTestObject(myKey);

        myS3Client.list(myBucket, list -> {
            if (list.succeeded()) {
                final S3BucketList bucketList = list.result();

                aContext.assertEquals(1, bucketList.size());
                aContext.assertTrue(bucketList.containsKey(myKey));

                complete(asyncTask);
            } else {
                aContext.fail(list.cause());
            }
        });
    }

    /**
     * Tests listing the supplied bucket.
     *
     * @param aContext A testing context
     */
    @Test
    public final void testListBucketPrefix(final TestContext aContext) {
        final String prefixedKey1 = PREFIX + myKey.toUpperCase(Locale.US);
        final String prefixedKey2 = PREFIX + myKey;
        final Async asyncTask = aContext.async();

        putTestObject(prefixedKey1);
        putTestObject(prefixedKey2);

        myS3Client.list(myBucket, PREFIX).onComplete(list -> {
            if (list.succeeded()) {
                aContext.assertEquals(2, list.result().size());
                complete(asyncTask);
            } else {
                aContext.fail(list.cause());
            }
        });
    }

    /**
     * Tests listing a bucket using a prefix.
     *
     * @param aContext A test context
     */
    @Test
    public final void testListBucketPrefixWithHandler(final TestContext aContext) {
        final String prefixedKey1 = PREFIX + myKey.toUpperCase(Locale.US);
        final String prefixedKey2 = PREFIX + myKey;
        final Async asyncTask = aContext.async();

        putTestObject(prefixedKey1);
        putTestObject(prefixedKey2);

        try {
            myS3Client.list(myBucket, PREFIX, list -> {
                if (list.succeeded()) {
                    final S3BucketList bucketList = list.result();

                    aContext.assertEquals(2, bucketList.size());
                    aContext.assertTrue(bucketList.containsKey(prefixedKey1));
                    aContext.assertTrue(bucketList.containsKey(prefixedKey2));

                    complete(asyncTask);
                } else {
                    aContext.fail(list.cause());
                }
            });
        } catch (final NullPointerException details) {
            details.printStackTrace();
            throw details;
        }
    }

    /**
     * Tests putting a buffer using a future.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyBuffer(final TestContext aContext) {
        final Buffer buffer = myContext.vertx().fileSystem().readFileBlocking(TEST_FILE);
        final Async asyncTask = aContext.async();

        myS3Client.put(myBucket, myKey, buffer).onComplete(put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myAwsS3Client.doesObjectExist(myBucket, myKey));

                if (!asyncTask.isCompleted()) {
                    aContext.assertTrue(put.result().contains(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
                }

                complete(asyncTask);
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Tests putting a buffer using handlers.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyBufferHandler(final TestContext aContext) {
        final Buffer buffer = myContext.vertx().fileSystem().readFileBlocking(TEST_FILE);
        final Async asyncTask = aContext.async();

        myS3Client.put(myBucket, myKey, buffer, put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myAwsS3Client.doesObjectExist(myBucket, myKey));
                complete(asyncTask);
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Tests putting a buffer with metadata using a future.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyBufferUserMetadata(final TestContext aContext) {
        final Buffer buffer = myContext.vertx().fileSystem().readFileBlocking(TEST_FILE);
        final UserMetadata metadata = getTestUserMetadata();
        final Async asyncTask = aContext.async();

        myS3Client.put(myBucket, myKey, buffer, metadata).onComplete(put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myAwsS3Client.doesObjectExist(myBucket, myKey));

                if (!asyncTask.isCompleted()) {
                    final ObjectMetadata objMetadata = myAwsS3Client.getObjectMetadata(myBucket, myKey);
                    final String metadataValue = objMetadata.getUserMetaDataOf(metadata.getName(0));

                    aContext.assertEquals(metadata.getValue(metadata.getName(0)), metadataValue);
                }

                if (!asyncTask.isCompleted()) {
                    aContext.assertTrue(put.result().contains(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
                }

                complete(asyncTask);
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Tests putting a Buffer with UserMetadata.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyBufferUserMetadataHandler(final TestContext aContext) {
        final Buffer buffer = myContext.vertx().fileSystem().readFileBlocking(TEST_FILE);
        final UserMetadata metadata = getTestUserMetadata();
        final Async asyncTask = aContext.async();

        myS3Client.put(myBucket, myKey, buffer, metadata, put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myAwsS3Client.doesObjectExist(myBucket, myKey));

                if (!asyncTask.isCompleted()) {
                    final ObjectMetadata objMetadata = myAwsS3Client.getObjectMetadata(myBucket, myKey);
                    final String metadataValue = objMetadata.getUserMetaDataOf(metadata.getName(0));

                    aContext.assertEquals(metadata.getValue(metadata.getName(0)), metadataValue);
                }

                complete(asyncTask);
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Tests putting an AsyncFile using a future.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyAsyncFile(final TestContext aContext) {
        final AsyncFile file = myContext.vertx().fileSystem().openBlocking(TEST_FILE, new OpenOptions());
        final Async asyncTask = aContext.async();

        myS3Client.put(myBucket, myKey, file).onComplete(put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myAwsS3Client.doesObjectExist(myBucket, myKey));

                if (!asyncTask.isCompleted()) {
                    aContext.assertTrue(put.result().contains(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
                }

                complete(asyncTask);
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Tests putting an AsyncFile using handlers.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyAsyncFileHandler(final TestContext aContext) {
        final AsyncFile file = myContext.vertx().fileSystem().openBlocking(TEST_FILE, new OpenOptions());
        final Async asyncTask = aContext.async();

        myS3Client.put(myBucket, myKey, file, put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myAwsS3Client.doesObjectExist(myBucket, myKey));

                if (!asyncTask.isCompleted()) {
                    aContext.assertTrue(put.result().contains(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
                }

                complete(asyncTask);
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Tests putting an AsyncFile with metadata using a future.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyAsyncFileMetadata(final TestContext aContext) {
        final AsyncFile file = myContext.vertx().fileSystem().openBlocking(TEST_FILE, new OpenOptions());
        final UserMetadata metadata = getTestUserMetadata();
        final Async asyncTask = aContext.async();

        myS3Client.put(myBucket, myKey, file, metadata).onComplete(put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myAwsS3Client.doesObjectExist(myBucket, myKey));

                if (!asyncTask.isCompleted()) {
                    final S3Object s3Obj = myAwsS3Client.getObject(myBucket, myKey);
                    final String name = metadata.getName(0);
                    final String value = metadata.getValue(0);

                    aContext.assertEquals(value, s3Obj.getObjectMetadata().getUserMetaDataOf(name));
                }

                if (!asyncTask.isCompleted()) {
                    aContext.assertTrue(put.result().contains(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
                }

                complete(asyncTask);
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Tests putting an AsyncFile with UserMetadata.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyAsyncFileUserMetadataHandler(final TestContext aContext) {
        final AsyncFile file = myContext.vertx().fileSystem().openBlocking(TEST_FILE, new OpenOptions());
        final UserMetadata metadata = getTestUserMetadata();
        final Async asyncTask = aContext.async();

        myS3Client.put(myBucket, myKey, file, metadata, put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myAwsS3Client.doesObjectExist(myBucket, myKey));

                if (!asyncTask.isCompleted()) {
                    final ObjectMetadata objMetadata = myAwsS3Client.getObjectMetadata(myBucket, myKey);
                    final String metadataValue = objMetadata.getUserMetaDataOf(metadata.getName(0));

                    aContext.assertEquals(metadata.getValue(metadata.getName(0)), metadataValue);
                }

                complete(asyncTask);
            }
        });
    }

    /**
     * Tests deleting an object.
     *
     * @param aContext A test context
     */
    @Test
    public final void testDeleteBucketKey(final TestContext aContext) {
        final Async asyncTask = aContext.async();

        putTestObject(myKey);

        myS3Client.delete(myBucket, myKey).onComplete(deletion -> {
            if (deletion.failed()) {
                aContext.fail(deletion.cause());
            } else {
                aContext.assertFalse(myAwsS3Client.doesObjectExist(myBucket, myKey));
                complete(asyncTask);
            }
        });
    }

    /**
     * Tests deleting an object.
     *
     * @param aContext A test context
     */
    @Test
    public final void testDeleteBucketKeyHandler(final TestContext aContext) {
        final Async asyncTask = aContext.async();

        putTestObject(myKey);

        myS3Client.delete(myBucket, myKey, delete -> {
            if (delete.succeeded()) {
                aContext.assertFalse(myAwsS3Client.doesObjectExist(myBucket, myKey));
                complete(asyncTask);
            } else {
                aContext.fail(delete.cause());
            }
        });
    }

    /**
     * PUTs a test artifact into an S3 bucket.
     *
     * @param aKey The S3 object key of the artifact
     */
    public void putTestObject(final String aKey) {
        myAwsS3Client.putObject(myBucket, aKey, new File(TEST_FILE));
        LOGGER.debug(MessageCodes.VSS_015, myBucket, aKey);
    }

    /**
     * Gets fake user metadata for testing.
     *
     * @return User metadata for testing
     */
    private UserMetadata getTestUserMetadata() {
        return new UserMetadata(myKey.toUpperCase(Locale.US), myKey);
    }
}
