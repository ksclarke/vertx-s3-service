
package info.freelibrary.vertx.s3;

import java.io.File;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
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
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Runs tests against a LocalStack S3 instance.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientFT extends AbstractS3FT {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientFT.class, MessageCodes.BUNDLE);

    private static final String TEST_FILE = "src/test/resources/green.gif";

    private static final String PREFIX = "prefix_";

    /**
     * A test rule to run the tests on the Vert.x context.
     */
    @Rule
    public final RunTestOnContext myContext = new RunTestOnContext();

    private S3Client myClient;

    private String myBucket;

    private String myKey;

    /**
     * Sets up the test about to be run.
     *
     * @param aContext A test context
     */
    @Before
    public void setUp(final TestContext aContext) {
        final AwsCredentials creds = new AwsCredentials(myAccessKey, mySecretKey);

        myClient = new S3Client(myContext.vertx(), new S3ClientOptions(myEndpoint).setCredentials(creds));
        myBucket = UUID.randomUUID().toString();
        myKey = UUID.randomUUID().toString();

        if (!myS3Client.doesBucketExistV2(myBucket)) {
            myS3Client.createBucket(myBucket);
        }
    }

    /**
     * Tests the HEAD request that returns a future.
     *
     * @param aContext A test context
     */
    @Test
    public final void testHeadBucketKey(final TestContext aContext) {
        final Async asyncTask = aContext.async();

        putGIF(myKey);

        myClient.head(myBucket, myKey).onComplete(head -> {
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

        putGIF(myKey);

        myClient.head(myBucket, myKey, head -> {
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

        putGIF(myKey);

        myClient.get(myBucket, myKey).onComplete(get -> {
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

        putGIF(myKey);

        myClient.get(myBucket, myKey, get -> {
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

        putGIF(myKey);

        myClient.list(myBucket).onComplete(list -> {
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

        putGIF(myKey);

        myClient.list(myBucket, list -> {
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
        final String prefixedKey1 = PREFIX + UUID.randomUUID().toString();
        final String prefixedKey2 = PREFIX + UUID.randomUUID().toString();
        final Async asyncTask = aContext.async();

        putGIF(prefixedKey1);
        putGIF(prefixedKey2);

        myClient.list(myBucket, PREFIX).onComplete(list -> {
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
        final String prefixedKey1 = PREFIX + UUID.randomUUID().toString();
        final String prefixedKey2 = PREFIX + UUID.randomUUID().toString();
        final Async asyncTask = aContext.async();

        putGIF(prefixedKey1);
        putGIF(prefixedKey2);

        myClient.list(myBucket, PREFIX, list -> {
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

        myClient.put(myBucket, myKey, buffer).onComplete(put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myS3Client.doesObjectExist(myBucket, myKey));

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

        myClient.put(myBucket, myKey, buffer, put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myS3Client.doesObjectExist(myBucket, myKey));
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

        myClient.put(myBucket, myKey, buffer, metadata).onComplete(put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myS3Client.doesObjectExist(myBucket, myKey));

                if (!asyncTask.isCompleted()) {
                    final ObjectMetadata objMetadata = myS3Client.getObjectMetadata(myBucket, myKey);
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

        myClient.put(myBucket, myKey, buffer, metadata, put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myS3Client.doesObjectExist(myBucket, myKey));

                if (!asyncTask.isCompleted()) {
                    final ObjectMetadata objMetadata = myS3Client.getObjectMetadata(myBucket, myKey);
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

        myClient.put(myBucket, myKey, file).onComplete(put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myS3Client.doesObjectExist(myBucket, myKey));

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

        myClient.put(myBucket, myKey, file, put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myS3Client.doesObjectExist(myBucket, myKey));

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

        myClient.put(myBucket, myKey, file, metadata).onComplete(put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myS3Client.doesObjectExist(myBucket, myKey));

                if (!asyncTask.isCompleted()) {
                    final S3Object s3Obj = myS3Client.getObject(myBucket, myKey);
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

        myClient.put(myBucket, myKey, file, metadata, put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myS3Client.doesObjectExist(myBucket, myKey));

                if (!asyncTask.isCompleted()) {
                    final ObjectMetadata objMetadata = myS3Client.getObjectMetadata(myBucket, myKey);
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

        putGIF(myKey);

        myClient.delete(myBucket, myKey).onComplete(deletion -> {
            if (deletion.failed()) {
                aContext.fail(deletion.cause());
            } else {
                aContext.assertFalse(myS3Client.doesObjectExist(myBucket, myKey));
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

        putGIF(myKey);

        myClient.delete(myBucket, myKey, delete -> {
            if (delete.succeeded()) {
                aContext.assertFalse(myS3Client.doesObjectExist(myBucket, myKey));
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
    public void putGIF(final String aKey) {
        myS3Client.putObject(myBucket, aKey, new File(TEST_FILE));
        LOGGER.debug(MessageCodes.VSS_015, myBucket, aKey);
    }

    /**
     * Gets fake user metadata for testing.
     *
     * @return User metadata for testing
     */
    private UserMetadata getTestUserMetadata() {
        final String name = UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();

        return new UserMetadata(name, value);
    }
}
