
package info.freelibrary.vertx.s3;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.amazonaws.services.s3.model.ObjectMetadata;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Runs tests against a LocalStack S3 instance.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientFT extends AbstractS3FT {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientFT.class, Constants.BUNDLE_NAME);

    private static final String TEST_FILE = "src/test/resources/green.gif";

    private static final String CONTENT_LENGTH = "Content-Length";

    private static final Vertx VERTX = Vertx.vertx();

    private static final String PREFIX = "prefix_";

    /**
     * A test rule to run the tests on the Vert.x context.
     */
    @Rule
    public final RunTestOnContext myContext = new RunTestOnContext();

    private String myBucket;

    /**
     * Sets up the test about to be run.
     *
     * @param aContext A test context
     */
    @Before
    public void setUp(final TestContext aContext) {
        myBucket = UUID.randomUUID().toString();
    }

    /**
     * Tears down any remaining test artifacts.
     *
     * @param aContext A test context
     */
    @After
    public void tearDown(final TestContext aContext) {
        if (myS3Client.listObjectsV2(myBucket).getObjectSummaries().size() == 0) {
            myS3Client.deleteBucket(myBucket);
        }
    }

    /**
     * Tests getting the head of an object using the bucket name and object key.
     *
     * @param aContext A test context
     */
    @Test
    public final void testHeadBucketKeyWithHandler(final TestContext aContext) throws MalformedURLException {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final String key = UUID.randomUUID().toString();
        final Async asyncTask = aContext.async();

        storeGIF(key);

        s3Client.head(myBucket, key, head -> {
            if (head.succeeded()) {
                final HttpClientResponse response = head.result();
                final int statusCode = response.statusCode();

                if (statusCode == HTTP.OK) {
                    aContext.assertEquals(85, Integer.parseInt(response.getHeader(CONTENT_LENGTH)));
                    complete(asyncTask);
                } else {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VSS_017, statusCode, response.statusMessage()));
                }
            } else {
                aContext.fail(head.cause());
            }

            removeGIF(key);
        }, error -> {
            removeGIF(key);
            aContext.fail(error);
        });
    }

    /**
     * Tests getting an object from a bucket using the bucket name and key.
     *
     * @param aContext A test context
     */
    @Test
    public final void testGetBucketKeyWithHandler(final TestContext aContext) throws MalformedURLException {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final String key = UUID.randomUUID().toString();
        final Async asyncTask = aContext.async();

        storeGIF(key);

        s3Client.get(myBucket, key, get -> {
            if (get.succeeded()) {
                final HttpClientResponse response = get.result();
                final int statusCode = response.statusCode();

                if (statusCode == HTTP.OK) {
                    response.bodyHandler(body -> {
                        aContext.assertEquals(85, body.length());

                        removeGIF(key);
                        complete(asyncTask);
                    });
                } else {
                    removeGIF(key);
                    aContext.fail(LOGGER.getMessage(MessageCodes.VSS_017, statusCode, response.statusMessage()));
                }
            } else {
                removeGIF(key);
                aContext.fail(get.cause());
            }
        }, error -> {
            removeGIF(key);
            aContext.fail(error);
        });
    }

    /**
     * Tests listing the supplied bucket.
     *
     * @param aContext A testing context
     */
    @Test
    public final void testListBucketWithHandler(final TestContext aContext) throws MalformedURLException {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final String key = UUID.randomUUID().toString();
        final Async asyncTask = aContext.async();

        storeGIF(key);

        s3Client.list(myBucket, list -> {
            if (list.succeeded()) {
                final HttpClientResponse response = list.result();
                final int statusCode = response.statusCode();

                if (statusCode == HTTP.OK) {
                    response.bodyHandler(body -> {
                        try {
                            final BucketList bucketList = new BucketList(body);

                            aContext.assertEquals(1, bucketList.size());
                            aContext.assertTrue(bucketList.containsKey(key));

                            complete(asyncTask);
                        } catch (final IOException details) {
                            aContext.fail(details);
                        } finally {
                            removeGIF(key);
                        }
                    });
                } else {
                    removeGIF(key);
                    aContext.fail(LOGGER.getMessage(MessageCodes.VSS_017, statusCode, response.statusMessage()));
                }
            } else {
                removeGIF(key);
                aContext.fail(list.cause());
            }
        }, error -> {
            removeGIF(key);
            aContext.fail(error);
        });
    }

    /**
     * Tests listing a bucket using a prefix.
     *
     * @param aContext A test context
     */
    @Test
    public final void testListBucketPrefixWithHandler(final TestContext aContext) throws MalformedURLException {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final String prefixedKey2 = PREFIX + UUID.randomUUID().toString();
        final String key = UUID.randomUUID().toString();
        final String prefixedKey1 = PREFIX + key;
        final Async asyncTask = aContext.async();

        storeGIF(prefixedKey1);
        storeGIF(prefixedKey2);

        s3Client.list(myBucket, PREFIX, list -> {
            if (list.succeeded()) {
                final HttpClientResponse response = list.result();
                final int statusCode = response.statusCode();

                if (statusCode == HTTP.OK) {
                    response.bodyHandler(body -> {
                        try {
                            final BucketList bucketList = new BucketList(body);

                            aContext.assertEquals(2, bucketList.size());
                            aContext.assertTrue(bucketList.containsKey(prefixedKey1));
                            aContext.assertTrue(bucketList.containsKey(prefixedKey2));

                            complete(asyncTask);
                        } catch (final IOException details) {
                            aContext.fail(details);
                        } finally {
                            removeGIF(prefixedKey1);
                            removeGIF(prefixedKey2);
                        }
                    });
                } else {
                    removeGIF(prefixedKey1);
                    removeGIF(prefixedKey2);
                    aContext.fail(LOGGER.getMessage(MessageCodes.VSS_017, statusCode, response.statusMessage()));
                }
            } else {
                removeGIF(prefixedKey1);
                removeGIF(prefixedKey2);
                aContext.fail(list.cause());
            }
        }, error -> {
            removeGIF(prefixedKey1);
            removeGIF(prefixedKey2);
            aContext.fail(error);
        });
    }

    /**
     * Tests putting a Buffer.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyBufferHandler(final TestContext aContext) throws MalformedURLException {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Buffer buffer = myContext.vertx().fileSystem().readFileBlocking(TEST_FILE);
        final String key = UUID.randomUUID().toString();
        final Async asyncTask = aContext.async();

        myS3Client.createBucket(myBucket);

        s3Client.put(myBucket, key, buffer, put -> {
            if (put.succeeded()) {
                final HttpClientResponse response = put.result();
                final int statusCode = response.statusCode();

                if (statusCode == HTTP.OK) {
                    aContext.assertTrue(gifIsFound(key));
                    complete(asyncTask);
                } else {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VSS_017, statusCode, response.statusMessage()));
                }
            } else {
                aContext.fail(put.cause());
            }

            removeGIF(key);
        }, error -> {
            removeGIF(key);
            aContext.fail(error);
        });
    }

    /**
     * Tests putting a Buffer with UserMetadata.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyBufferUserMetadataHandler(final TestContext aContext)
            throws MalformedURLException {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Buffer buffer = myContext.vertx().fileSystem().readFileBlocking(TEST_FILE);
        final UserMetadata metadata = getTestUserMetadata();
        final String key = UUID.randomUUID().toString();
        final Async asyncTask = aContext.async();

        myS3Client.createBucket(myBucket);

        s3Client.put(myBucket, key, buffer, metadata, put -> {
            if (put.succeeded()) {
                final HttpClientResponse response = put.result();
                final int statusCode = response.statusCode();

                if (statusCode == HTTP.OK) {
                    aContext.assertTrue(gifIsFound(key));

                    if (!asyncTask.isCompleted()) {
                        final ObjectMetadata objMetadata = myS3Client.getObjectMetadata(myBucket, key);
                        final String metadataValue = objMetadata.getUserMetaDataOf(metadata.getName(0));

                        aContext.assertEquals(metadata.getValue(metadata.getName(0)), metadataValue);
                    }

                    complete(asyncTask);
                } else {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VSS_017, statusCode, response.statusMessage()));
                }
            } else {
                aContext.fail(put.cause());
            }

            removeGIF(key);
        }, error -> {
            removeGIF(key);
            aContext.fail(error);
        });
    }

    /**
     * Tests putting an AsyncFile.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyAsyncFileHandler(final TestContext aContext) throws MalformedURLException {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final AsyncFile file = myContext.vertx().fileSystem().openBlocking(TEST_FILE, new OpenOptions());
        final String key = UUID.randomUUID().toString();
        final Async asyncTask = aContext.async();

        myS3Client.createBucket(myBucket);

        s3Client.put(myBucket, key, file, put -> {
            if (put.succeeded()) {
                final HttpClientResponse response = put.result();
                final int statusCode = response.statusCode();

                if (statusCode == HTTP.OK) {
                    aContext.assertTrue(gifIsFound(key));
                    complete(asyncTask);
                } else {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VSS_017, statusCode, response.statusMessage()));
                }
            } else {
                aContext.fail(put.cause());
            }

            removeGIF(key);
        }, error -> {
            removeGIF(key);
            aContext.fail(error);
        });
    }

    /**
     * Tests putting an AsyncFile with UserMetadata.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyAsyncFileUserMetadataHandler(final TestContext aContext)
            throws MalformedURLException {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final AsyncFile file = myContext.vertx().fileSystem().openBlocking(TEST_FILE, new OpenOptions());
        final UserMetadata metadata = getTestUserMetadata();
        final String key = UUID.randomUUID().toString();
        final Async asyncTask = aContext.async();

        myS3Client.createBucket(myBucket);

        s3Client.put(myBucket, key, file, metadata, put -> {
            if (put.succeeded()) {
                final HttpClientResponse response = put.result();
                final int statusCode = response.statusCode();

                if (statusCode == HTTP.OK) {
                    aContext.assertTrue(gifIsFound(key));

                    if (!asyncTask.isCompleted()) {
                        final ObjectMetadata objMetadata = myS3Client.getObjectMetadata(myBucket, key);
                        final String metadataValue = objMetadata.getUserMetaDataOf(metadata.getName(0));

                        aContext.assertEquals(metadata.getValue(metadata.getName(0)), metadataValue);
                    }

                    complete(asyncTask);
                } else {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VSS_017, statusCode, response.statusMessage()));
                }
            }

            removeGIF(key);
        }, error -> {
            removeGIF(key);
            aContext.fail(error);
        });
    }

    /**
     * Tests deleting an object.
     *
     * @param aContext A test context
     */
    @Test
    public final void testDeleteBucketKeyHandler(final TestContext aContext) throws MalformedURLException {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final String key = UUID.randomUUID().toString();
        final Async asyncTask = aContext.async();

        storeGIF(key);

        s3Client.delete(myBucket, key, delete -> {
            if (delete.succeeded()) {
                final HttpClientResponse response = delete.result();
                final int statusCode = response.statusCode();

                if (statusCode == HTTP.NO_CONTENT) {
                    aContext.assertFalse(gifIsFound(key));
                    complete(asyncTask);
                } else {
                    response.bodyHandler(body -> {
                        LOGGER.info(body.toString());
                    });

                    aContext.fail(LOGGER.getMessage(MessageCodes.VSS_017, statusCode, response.statusMessage()));
                    removeGIF(key);
                }
            } else {
                aContext.fail(delete.cause());
                removeGIF(key);
            }
        }, error -> {
            removeGIF(key);
            aContext.fail(error);
        });
    }

    /**
     * Stores a test GIF in our S3 compatible test environment.
     */
    private void storeGIF(final String aKey) {
        if (!myS3Client.doesBucketExistV2(myBucket)) {
            myS3Client.createBucket(myBucket);
        }

        myS3Client.putObject(myBucket, aKey, new File(TEST_FILE));
        LOGGER.debug(MessageCodes.VSS_015, myBucket, aKey);
    }

    /**
     * Remove a GIF we've put in the bucket. This isn't strictly necessary since the bucket is an in-memory thing that
     * goes away once the container is shutdown, but we'll do it anyway.
     */
    private void removeGIF(final String aKey) {
        myS3Client.deleteObject(myBucket, aKey);
        LOGGER.debug(MessageCodes.VSS_016, myBucket, aKey);
    }

    /**
     * Checks to see that our test GIF exists.
     *
     * @return True if the test GIF exists; else, false
     */
    private boolean gifIsFound(final String aKey) {
        return myS3Client.doesObjectExist(myBucket, aKey);
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
