
package info.freelibrary.vertx.s3;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.Vertx;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientFT.class, Constants.BUNDLE_NAME);

    private static final String TEST_FILE = "src/test/resources/green.gif";

    private static final String CONTENT_LENGTH = "Content-Length";

    private static final Vertx VERTX = Vertx.vertx();

    private static final String PREFIX = "prefix_";

    @Rule
    public final RunTestOnContext myContext = new RunTestOnContext();

    private String myEndpoint;

    private String myBucket;

    private String myKey;

    /**
     * Sets up the test about to be run.
     *
     * @param aContext A test context
     */
    @Before
    public void setUp(final TestContext aContext) {
        final AmazonS3ClientBuilder s3ClientBuilder = AmazonS3ClientBuilder.standard();

        s3ClientBuilder.withEndpointConfiguration(myEndpointConfig).withCredentials(myCredentialsProvider);
        s3ClientBuilder.withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP));

        myEndpoint = myEndpointConfig.getServiceEndpoint();
        myAwsS3Client = s3ClientBuilder.build();
        myBucket = UUID.randomUUID().toString();
        myKey = UUID.randomUUID().toString();
    }

    /**
     * Tests getting the head of an object using the bucket name and object key.
     *
     * @param aContext A test context
     */
    @Test
    public final void testHeadBucketKeyWithHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Async asyncTask = aContext.async();

        storeGIF(myKey);

        s3Client.head(myBucket, myKey, head -> {
            if (head.statusCode() == HTTP.OK) {
                aContext.assertEquals(85, Integer.parseInt(head.getHeader(CONTENT_LENGTH)));
                complete(asyncTask);
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, head.statusCode(), head.statusMessage()));
            }

            removeGIF(myKey);
        });
    }

    /**
     * Tests getting the head of an object using the bucket name and object key.
     *
     * @param aContext A test context
     */
    @Test
    @SuppressWarnings("checkstyle:indentation")
    public final void testHeadBucketKeyWithHandlerAndExceptionHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Async asyncTask = aContext.async();

        storeGIF(myKey);

        s3Client.head(myBucket, myKey, head -> {
            if (head.statusCode() == HTTP.OK) {
                aContext.assertEquals(85, Integer.parseInt(head.getHeader(CONTENT_LENGTH)));
                complete(asyncTask);
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, head.statusCode(), head.statusMessage()));
            }

            removeGIF(myKey);
        }, error -> {
            removeGIF(myKey);
            aContext.fail(error);
        });
    }

    /**
     * Tests getting an object from a bucket using the bucket name and key.
     *
     * @param aContext A test context
     */
    @Test
    public final void testGetBucketKeyWithHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Async asyncTask = aContext.async();

        storeGIF(myKey);

        s3Client.get(myBucket, myKey, get -> {
            if (get.statusCode() == HTTP.OK) {
                get.bodyHandler(body -> {
                    aContext.assertEquals(85, body.length());

                    removeGIF(myKey);
                    complete(asyncTask);
                });
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, get.statusCode(), get.statusMessage()));
                removeGIF(myKey);
            }
        });
    }

    /**
     * Tests getting an object from a bucket using the bucket name and key.
     *
     * @param aContext A test context
     */
    @Test
    @SuppressWarnings("checkstyle:indentation")
    public final void testGetBucketKeyWithHandlerAndExceptionHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Async asyncTask = aContext.async();

        storeGIF(myKey);

        s3Client.get(myBucket, myKey, get -> {
            if (get.statusCode() == HTTP.OK) {
                get.bodyHandler(body -> {
                    aContext.assertEquals(85, body.length());

                    removeGIF(myKey);
                    complete(asyncTask);
                });
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, get.statusCode(), get.statusMessage()));
                removeGIF(myKey);
            }
        }, error -> {
            aContext.fail(error);
            removeGIF(myKey);
        });
    }

    /**
     * Tests listing the supplied bucket.
     *
     * @param aContext A testing context
     */
    @Test
    public final void testListBucketWithHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Async asyncTask = aContext.async();

        storeGIF(myKey);

        s3Client.list(myBucket, list -> {
            if (list.statusCode() == HTTP.OK) {
                list.bodyHandler(body -> {
                    try {
                        final BucketList bucketList = new BucketList(body);

                        aContext.assertEquals(1, bucketList.size());
                        aContext.assertTrue(bucketList.containsKey(myKey));

                        complete(asyncTask);
                    } catch (final IOException details) {
                        aContext.fail(details);
                    } finally {
                        removeGIF(myKey);
                    }
                });
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, list.statusCode(), list.statusMessage()));
                removeGIF(myKey);
            }
        });
    }

    /**
     * Tests listing the supplied bucket.
     *
     * @param aContext A testing context
     */
    @Test
    @SuppressWarnings("checkstyle:indentation")
    public final void testListBucketWithHandlerAndExceptionHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Async asyncTask = aContext.async();

        storeGIF(myKey);

        s3Client.list(myBucket, list -> {
            if (list.statusCode() == HTTP.OK) {
                list.bodyHandler(body -> {
                    try {
                        final BucketList bucketList = new BucketList(body.toString(StandardCharsets.UTF_8));

                        aContext.assertEquals(1, bucketList.size());
                        aContext.assertTrue(bucketList.containsKey(myKey));

                        complete(asyncTask);
                    } catch (final IOException details) {
                        aContext.fail(details);
                    } finally {
                        removeGIF(myKey);
                    }
                });
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, list.statusCode(), list.statusMessage()));
                removeGIF(myKey);
            }
        }, error -> {
            aContext.fail(error);
            removeGIF(myKey);
        });
    }

    /**
     * Tests listing a bucket using a prefix.
     *
     * @param aContext A test context
     */
    @Test
    public final void testListBucketPrefixWithHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Async asyncTask = aContext.async();
        final String pKey2 = PREFIX + UUID.randomUUID().toString();
        final String pKey1 = PREFIX + myKey;

        storeGIF(pKey1);
        storeGIF(pKey2);

        s3Client.list(myBucket, PREFIX, list -> {
            if (list.statusCode() == HTTP.OK) {
                list.bodyHandler(body -> {
                    try {
                        final BucketList bucketList = new BucketList(body);

                        aContext.assertEquals(2, bucketList.size());
                        aContext.assertTrue(bucketList.containsKey(pKey1));
                        aContext.assertTrue(bucketList.containsKey(pKey2));

                        complete(asyncTask);
                    } catch (final IOException details) {
                        aContext.fail(details);
                    } finally {
                        removeGIF(pKey1);
                        removeGIF(pKey2);
                    }
                });
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, list.statusCode(), list.statusMessage()));
                removeGIF(pKey1);
                removeGIF(pKey2);
            }
        });
    }

    /**
     * Tests listing a bucket using a prefix, with an exception handler.
     *
     * @param aContext A test context
     */
    @Test
    @SuppressWarnings("checkstyle:indentation")
    public final void testListBucketPrefixWithHandlerWithExceptionHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Async asyncTask = aContext.async();
        final String prefixedKey = PREFIX + myKey;

        storeGIF(prefixedKey);

        s3Client.list(myBucket, PREFIX, list -> {
            if (list.statusCode() == HTTP.OK) {
                list.bodyHandler(body -> {
                    try {
                        final BucketList bucketList = new BucketList(body);

                        aContext.assertEquals(1, bucketList.size());
                        aContext.assertTrue(bucketList.containsKey(prefixedKey));
                        complete(asyncTask);
                    } catch (final IOException details) {
                        aContext.fail(details);
                    } finally {
                        removeGIF(prefixedKey);
                    }
                });
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, list.statusCode(), list.statusMessage()));
                removeGIF(prefixedKey);
            }
        }, error -> {
            aContext.fail(error);
        });
    }

    /**
     * Tests putting a Buffer.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyBufferHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Buffer buffer = myContext.vertx().fileSystem().readFileBlocking(TEST_FILE);
        final Async asyncTask = aContext.async();

        myAwsS3Client.createBucket(myBucket);

        s3Client.put(myBucket, myKey, buffer, put -> {
            if (put.statusCode() == HTTP.OK) {
                aContext.assertTrue(gifIsFound(myKey));
                complete(asyncTask);
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, put.statusCode(), put.statusMessage()));
            }

            removeGIF(myKey);
        });
    }

    /**
     * Tests putting a Buffer, with an exception handler.
     *
     * @param aContext A test context
     */
    @Test
    @SuppressWarnings("checkstyle:indentation")
    public final void testPutBucketKeyBufferHandlerExceptionHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Buffer buffer = myContext.vertx().fileSystem().readFileBlocking(TEST_FILE);
        final Async asyncTask = aContext.async();

        myAwsS3Client.createBucket(myBucket);

        s3Client.put(myBucket, myKey, buffer, put -> {
            if (put.statusCode() == HTTP.OK) {
                aContext.assertTrue(gifIsFound(myKey));
                complete(asyncTask);
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, put.statusCode(), put.statusMessage()));
            }

            removeGIF(myKey);
        }, error -> {
            removeGIF(myKey);
            aContext.fail(error);
        });
    }

    /**
     * Tests putting a Buffer with UserMetadata.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyBufferUserMetadataHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Buffer buffer = myContext.vertx().fileSystem().readFileBlocking(TEST_FILE);
        final UserMetadata metadata = getTestUserMetadata();
        final Async asyncTask = aContext.async();

        myAwsS3Client.createBucket(myBucket);

        s3Client.put(myBucket, myKey, buffer, metadata, put -> {
            final String value;

            if (put.statusCode() == HTTP.OK) {
                aContext.assertTrue(gifIsFound(myKey));
                value = myAwsS3Client.getObjectMetadata(myBucket, myKey).getUserMetaDataOf(metadata.getName(0));
                aContext.assertEquals(metadata.getValue(metadata.getName(0)), value);
                complete(asyncTask);
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, put.statusCode(), put.statusMessage()));
            }

            removeGIF(myKey);
        });
    }

    /**
     * Tests putting a Buffer with UserMetadata, with an exception handler.
     *
     * @param aContext A test context
     */
    @Test
    @SuppressWarnings("checkstyle:indentation")
    public final void testPutBucketKeyBufferUserMetadataHandlerExceptionHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Buffer buffer = myContext.vertx().fileSystem().readFileBlocking(TEST_FILE);
        final UserMetadata metadata = getTestUserMetadata();
        final Async asyncTask = aContext.async();

        myAwsS3Client.createBucket(myBucket);

        s3Client.put(myBucket, myKey, buffer, metadata, put -> {
            final String value;

            if (put.statusCode() == HTTP.OK) {
                aContext.assertTrue(gifIsFound(myKey));
                value = myAwsS3Client.getObjectMetadata(myBucket, myKey).getUserMetaDataOf(metadata.getName(0));
                aContext.assertEquals(metadata.getValue(metadata.getName(0)), value);
                complete(asyncTask);
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, put.statusCode(), put.statusMessage()));
            }

            removeGIF(myKey);
        }, error -> {
            removeGIF(myKey);
            aContext.fail(error);
        });
    }

    /**
     * Tests putting an AsyncFile.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyAsyncFileHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final AsyncFile file = myContext.vertx().fileSystem().openBlocking(TEST_FILE, new OpenOptions());
        final Async asyncTask = aContext.async();

        myAwsS3Client.createBucket(myBucket);

        s3Client.put(myBucket, myKey, file, put -> {
            if (put.statusCode() == HTTP.OK) {
                aContext.assertTrue(gifIsFound(myKey));
                complete(asyncTask);
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, put.statusCode(), put.statusMessage()));
            }

            removeGIF(myKey);
        });
    }

    /**
     * Tests putting an AsyncFile, with an exception handler.
     *
     * @param aContext A test context
     */
    @Test
    @SuppressWarnings("checkstyle:indentation")
    public final void testPutBucketKeyAsyncFileHandlerExceptionHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final AsyncFile file = myContext.vertx().fileSystem().openBlocking(TEST_FILE, new OpenOptions());
        final Async asyncTask = aContext.async();

        myAwsS3Client.createBucket(myBucket);

        s3Client.put(myBucket, myKey, file, put -> {
            if (put.statusCode() == HTTP.OK) {
                aContext.assertTrue(gifIsFound(myKey));
                complete(asyncTask);
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, put.statusCode(), put.statusMessage()));
            }

            removeGIF(myKey);
        }, error -> {
            removeGIF(myKey);
            aContext.fail(error);
        });
    }

    /**
     * Tests putting an AsyncFile with UserMetadata.
     *
     * @param aContext A test context
     */
    @Test
    public final void testPutBucketKeyAsyncFileUserMetadataHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final AsyncFile file = myContext.vertx().fileSystem().openBlocking(TEST_FILE, new OpenOptions());
        final UserMetadata metadata = getTestUserMetadata();
        final Async asyncTask = aContext.async();

        myAwsS3Client.createBucket(myBucket);

        s3Client.put(myBucket, myKey, file, metadata, put -> {
            final String value;

            if (put.statusCode() == HTTP.OK) {
                aContext.assertTrue(gifIsFound(myKey));
                value = myAwsS3Client.getObjectMetadata(myBucket, myKey).getUserMetaDataOf(metadata.getName(0));
                aContext.assertEquals(metadata.getValue(metadata.getName(0)), value);
                complete(asyncTask);
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, put.statusCode(), put.statusMessage()));
            }

            removeGIF(myKey);
        });
    }

    /**
     * Tests putting an AsyncFile with UserMetadata, with an exception handler.
     *
     * @param aContext A test context
     */
    @Test
    @SuppressWarnings("checkstyle:indentation")
    public final void testPutBucketKeyAsyncFileUserMetadataHandlerExceptionHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final AsyncFile file = myContext.vertx().fileSystem().openBlocking(TEST_FILE, new OpenOptions());
        final UserMetadata metadata = getTestUserMetadata();
        final Async asyncTask = aContext.async();

        myAwsS3Client.createBucket(myBucket);

        s3Client.put(myBucket, myKey, file, metadata, put -> {
            final String value;

            if (put.statusCode() == HTTP.OK) {
                aContext.assertTrue(gifIsFound(myKey));
                value = myAwsS3Client.getObjectMetadata(myBucket, myKey).getUserMetaDataOf(metadata.getName(0));
                aContext.assertEquals(metadata.getValue(metadata.getName(0)), value);
                complete(asyncTask);
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, put.statusCode(), put.statusMessage()));
            }

            removeGIF(myKey);
        }, error -> {
            removeGIF(myKey);
            aContext.fail(error);
        });
    }

    /**
     * Tests deleting an object.
     *
     * @param aContext A test context
     */
    @Test
    public final void testDeleteBucketKeyHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Async asyncTask = aContext.async();

        storeGIF(myKey);

        s3Client.delete(myBucket, myKey, delete -> {
            if (delete.statusCode() == HTTP.NO_CONTENT) {
                aContext.assertFalse(gifIsFound(myKey));
                complete(asyncTask);
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, delete.statusCode(), delete.statusMessage()));
                removeGIF(myKey);
            }
        });
    }

    /**
     * Tests deleting an object with an exception handler.
     *
     * @param aContext A test context
     */
    @Test
    @SuppressWarnings("checkstyle:indentation")
    public final void testDeleteBucketKeyHandlerExceptionHandler(final TestContext aContext) {
        final S3Client s3Client = new S3Client(VERTX, myAccessKey, mySecretKey, myEndpoint);
        final Async asyncTask = aContext.async();

        storeGIF(myKey);

        s3Client.delete(myBucket, myKey, delete -> {
            if (delete.statusCode() == HTTP.NO_CONTENT) {
                aContext.assertFalse(gifIsFound(myKey));
                complete(asyncTask);
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_017, delete.statusCode(), delete.statusMessage()));
                removeGIF(myKey);
            }
        }, error -> {
            aContext.fail(error);
            removeGIF(myKey);
        });
    }

    /**
     * Stores a test GIF in our S3 compatible test environment.
     */
    private void storeGIF(final String aKey) {
        if (!myAwsS3Client.doesBucketExistV2(myBucket)) {
            myAwsS3Client.createBucket(myBucket);
        }

        myAwsS3Client.putObject(myBucket, aKey, new File(TEST_FILE));
        LOGGER.debug(MessageCodes.VS3_015, myBucket, aKey);
    }

    /**
     * Remove a GIF we've put in the bucket. This isn't strictly necessary since the bucket is an in-memory thing that
     * goes away once the container is shutdown, but we'll do it anyway.
     */
    private void removeGIF(final String aKey) {
        myAwsS3Client.deleteObject(myBucket, aKey);
        LOGGER.debug(MessageCodes.VS3_016, myBucket, aKey);

        if (myAwsS3Client.listObjectsV2(myBucket).getObjectSummaries().size() == 0) {
            myAwsS3Client.deleteBucket(myBucket);
        }
    }

    /**
     * Checks to see that our test GIF exists.
     *
     * @return True if the test GIF exists; else, false
     */
    private boolean gifIsFound(final String aKey) {
        return myAwsS3Client.doesObjectExist(myBucket, aKey);
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
