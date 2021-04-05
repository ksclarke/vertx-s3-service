
package info.freelibrary.vertx.s3;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import info.freelibrary.vertx.s3.util.MessageCodes;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Integration tests for {@link S3Client}.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientIT extends AbstractS3IT {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientIT.class, MessageCodes.BUNDLE);

    @Rule
    public RunTestOnContext myContext = new RunTestOnContext();

    @Rule
    public TestName myName = new TestName();

    /** The S3 client being used in the tests */
    private S3Client myClient;

    /** A test ID */
    private String myTestID;

    /**
     * Sets up tests.
     *
     * @param aContext A test context
     */
    @Override
    @Before
    public void setUp(final TestContext aContext) {
        final AwsProfile awsProfile = new AwsProfile(TestConstants.TEST_PROFILE);
        final Vertx vertx = myContext.vertx();

        myTestID = UUID.randomUUID().toString();
        myClient = new S3Client(vertx, new S3ClientOptions(S3Endpoint.US_EAST_1).setProfile(awsProfile));

        super.setUp(aContext);
    }

    /**
     * Tears down tests.
     *
     * @param aContext A test context
     */
    @Override
    @After
    public void tearDown(final TestContext aContext) {
        super.tearDown(aContext);

        if (myClient != null) {
            myClient.close();
        }
    }

    /**
     * Test empty constructor.
     *
     * @param aContext A test context
     */
    @Test
    public void testEmptyConstructor(final TestContext aContext) {
        new S3Client(myContext.vertx());
    }

    /**
     * Test constructor with client options.
     *
     * @param aContext A test context
     */
    @Test
    public void testConstructorWithClientOptions(final TestContext aContext) {
        new S3Client(myContext.vertx(), new S3ClientOptions());
    }

    /**
     * Tests the HEAD request that returns a future.
     *
     * @param aContext A test context
     */
    @Test
    public final void testHeadBucketKey(final TestContext aContext) {
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final Async asyncTask = aContext.async();

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.head(myTestBucket, s3Key).onComplete(head -> {
                if (head.succeeded()) {
                    aContext.assertEquals("85", head.result().get(HttpHeaders.CONTENT_LENGTH));
                    complete(asyncTask);
                } else {
                    aContext.fail(head.cause());
                }
            });
        }
    }

    /**
     * Test for {@link S3Client#head(String, String, Handler)}
     *
     * @param aContext A test context
     */
    @Test
    public void testHead(final TestContext aContext) {
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final Async asyncTask = aContext.async();

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.head(myTestBucket, s3Key, head -> {
                if (head.succeeded()) {
                    final String contentLength = head.result().get(HttpHeaders.CONTENT_LENGTH);

                    aContext.assertNotNull(contentLength);
                    aContext.assertTrue(Integer.parseInt(contentLength) > 0);

                    complete(asyncTask);
                } else {
                    aContext.fail(head.cause());
                }
            });
        }
    }

    /**
     * Test for {@link S3Client#head(String, String, Handler, Handler)}
     *
     * @param aContext A test context
     */
    @Test
    public void testHeadExceptionHandler(final TestContext aContext) {
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final Async asyncTask = aContext.async();

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.head(myTestBucket, s3Key, head -> {
                if (head.succeeded()) {
                    final String contentLength = head.result().get(HttpHeaders.CONTENT_LENGTH);

                    aContext.assertNotNull(contentLength);
                    aContext.assertTrue(Integer.parseInt(contentLength) > 0);

                    complete(asyncTask);
                } else {
                    aContext.fail(head.cause());
                }
            });
        }
    }

    /**
     * Tests listing the supplied bucket.
     *
     * @param aContext A testing context
     */
    @Test
    public final void testListBucket(final TestContext aContext) {
        final String[] keysArray = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };
        final Async asyncTask = aContext.async();

        if (createResources(keysArray, aContext, asyncTask)) {
            myClient.list(myTestBucket).onComplete(list -> {
                if (list.succeeded()) {
                    aContext.assertEquals(4, list.result().size());
                    complete(asyncTask);
                } else {
                    aContext.fail(list.cause());
                }
            });
        }
    }

    /**
     * Tests listing an S3 bucket.
     *
     * @param aContext A testing environment
     */
    @Test
    public void testList(final TestContext aContext) {
        final String[] keysArray = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };
        final Async asyncTask = aContext.async();

        if (createResources(keysArray, aContext, asyncTask)) {
            myClient.list(myTestBucket, list -> {
                if (list.succeeded()) {
                    final S3BucketList bucketList = list.result();

                    for (final String key : keysArray) {
                        aContext.assertTrue(bucketList.containsKey(key));
                    }

                    aContext.assertEquals(bucketList.size(), keysArray.length);
                    complete(asyncTask);
                } else {
                    aContext.fail(list.cause());
                }
            });
        }
    }

    /**
     * Tests listing an S3 bucket.
     *
     * @param aContext A testing environment
     */
    @Test
    public void testListExceptionHandler(final TestContext aContext) {
        final String[] keysArray = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };
        final Async asyncTask = aContext.async();

        if (createResources(keysArray, aContext, asyncTask)) {
            myClient.list(myTestBucket, list -> {
                if (list.succeeded()) {
                    final S3BucketList bucketList = list.result();

                    for (final String key : keysArray) {
                        aContext.assertTrue(bucketList.containsKey(key));
                    }

                    aContext.assertEquals(bucketList.size(), keysArray.length);
                    complete(asyncTask);
                } else {
                    aContext.fail(list.cause());
                }
            });
        }
    }

    /**
     * Tests listing the supplied bucket.
     *
     * @param aContext A testing context
     */
    @Test
    public final void testListBucketPrefix(final TestContext aContext) {
        final String[] keysArray = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };
        final Async asyncTask = aContext.async();

        if (createResources(keysArray, aContext, asyncTask)) {
            myClient.list(myTestBucket, TestConstants.PATH_FROM).onComplete(list -> {
                if (list.succeeded()) {
                    final S3BucketList s3BucketList = list.result();

                    aContext.assertEquals(2, list.result().size());
                    aContext.assertTrue(s3BucketList.containsKey(keysArray[2]));
                    aContext.assertTrue(s3BucketList.containsKey(keysArray[3]));

                    complete(asyncTask);
                } else {
                    aContext.fail(list.cause());
                }
            });
        }
    }

    /**
     * Tests LISTing with a prefix.
     *
     * @param aContext A testing environment
     */
    @Test
    public void testListWithPrefix(final TestContext aContext) {
        final String[] keysArray = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };
        final Async asyncTask = aContext.async();

        if (createResources(keysArray, aContext, asyncTask)) {
            myClient.list(myTestBucket, TestConstants.PATH_FROM, list -> {
                if (list.succeeded()) {
                    final S3BucketList s3BucketList = list.result();

                    aContext.assertEquals(s3BucketList.size(), 2);
                    aContext.assertTrue(s3BucketList.containsKey(keysArray[2]));
                    aContext.assertTrue(s3BucketList.containsKey(keysArray[3]));

                    complete(asyncTask);
                } else {
                    aContext.fail(list.cause());
                }
            });
        }
    }

    /**
     * Tests LISTing with a prefix.
     *
     * @param aContext A testing environment
     */
    @Test
    public void testListWithPrefixExceptionHandler(final TestContext aContext) {
        final String[] keysArray = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };
        final Async asyncTask = aContext.async();

        if (createResources(keysArray, aContext, asyncTask)) {
            myClient.list(myTestBucket, TestConstants.PATH_FROM, list -> {
                if (list.succeeded()) {
                    final S3BucketList s3BucketList = list.result();

                    aContext.assertEquals(s3BucketList.size(), 2);
                    aContext.assertTrue(s3BucketList.containsKey(keysArray[2]));
                    aContext.assertTrue(s3BucketList.containsKey(keysArray[3]));

                    complete(asyncTask);
                } else {
                    aContext.fail(list.cause());
                }
            });
        }
    }

    /**
     * Tests the GET request.
     *
     * @param aContext A test context
     */
    @Test
    public final void testGetBucketKey(final TestContext aContext) {
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final Async asyncTask = aContext.async();

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.get(myTestBucket, s3Key).onComplete(get -> {
                if (get.succeeded()) {
                    get.result().body().onComplete(body -> {
                        aContext.assertEquals(85, body.result().length());
                        complete(asyncTask);
                    });
                } else {
                    aContext.fail(get.cause());
                }
            });
        }
    }

    /**
     * Test for {@link S3Client#get(String, String, Handler)}
     *
     * @param aContext A test context
     */
    @Test
    public void testGet(final TestContext aContext) {
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final Async asyncTask = aContext.async();

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.get(myTestBucket, s3Key, get -> {
                if (get.succeeded()) {
                    get.result().body(body -> {
                        final int length = body.result().length();

                        if (length != myResource.length) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.VSS_004, length));
                        }

                        complete(asyncTask);
                    });
                } else {
                    aContext.fail(get.cause());
                }
            });
        }
    }

    /**
     * Test for {@link S3Client#get(String, String, Handler)}
     *
     * @param aContext A test context
     */
    @Test
    public void testGetExceptionHandler(final TestContext aContext) {
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final Async asyncTask = aContext.async();

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.get(myTestBucket, s3Key, get -> {
                if (get.succeeded()) {
                    get.result().body(body -> {
                        final int length = body.result().length();

                        if (length != myResource.length) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.VSS_004, length));
                        }

                        complete(asyncTask);
                    });
                } else {
                    aContext.fail(get.cause());
                }
            });
        }
    }

    /**
     * Tests putting a buffer using a future.
     *
     * @param aContext A test context
     */
    @Test
    @Ignore
    public final void testPutBucketKeyBuffer(final TestContext aContext) {
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final Async asyncTask = aContext.async();

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource)).onComplete(put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myS3Client.doesObjectExist(myTestBucket, s3Key));

                if (!asyncTask.isCompleted()) {
                    assertTrue(put.result().contains(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
                }

                complete(asyncTask);
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Testing PUTing data to an S3 object
     *
     * @param aContext A testing environment
     */
    @Test
    @Ignore
    public void testPut(final TestContext aContext) {
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final Async asyncTask = aContext.async();

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), put -> {
            if (put.succeeded()) {
                asyncTask.complete();
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Testing PUTing data to an S3 object
     *
     * @param aContext A testing environment
     */
    @Test
    @Ignore
    public void testPutExceptionHandler(final TestContext aContext) {
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final Async asyncTask = aContext.async();

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), put -> {
            if (put.succeeded()) {
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
    @Ignore
    public final void testPutBucketKeyBufferUserMetadata(final TestContext aContext) {
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.ONE, TestConstants.TWO);
        final Async asyncTask = aContext.async();

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), metadata).onComplete(put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myS3Client.doesObjectExist(myTestBucket, s3Key));

                if (!asyncTask.isCompleted()) {
                    final ObjectMetadata objMetadata = myS3Client.getObjectMetadata(myTestBucket, s3Key);
                    final String metadataValue = objMetadata.getUserMetaDataOf(metadata.getName(0));

                    aContext.assertEquals(metadata.getValue(metadata.getName(0)), metadataValue);
                }

                if (!asyncTask.isCompleted()) {
                    assertTrue(put.result().contains(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
                }

                complete(asyncTask);
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Test PUTing with user metadata.
     *
     * @param aContext A testing context
     */
    @Test
    @Ignore
    public void testPutUserMetadata(final TestContext aContext) {
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.ONE, TestConstants.TWO);
        final Async asyncTask = aContext.async();

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), metadata, put -> {
            if (put.succeeded()) {
                complete(asyncTask);
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Test PUTing with user metadata.
     *
     * @param aContext A testing context
     */
    @Test
    @Ignore
    public void testPutUserMetadataExceptionHandler(final TestContext aContext) {
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.ONE, TestConstants.TWO);
        final Async asyncTask = aContext.async();

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), metadata, put -> {
            if (put.succeeded()) {
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
    @Ignore
    public final void testPutBucketKeyAsyncFile(final TestContext aContext) {
        final FileSystem fileSystem = myContext.vertx().fileSystem();
        final AsyncFile file = fileSystem.openBlocking(TEST_FILE.getAbsolutePath(), new OpenOptions());
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final Async asyncTask = aContext.async();

        myClient.put(myTestBucket, s3Key, file).onComplete(put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myS3Client.doesObjectExist(myTestBucket, s3Key));

                if (!asyncTask.isCompleted()) {
                    assertTrue(put.result().contains(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
                }

                complete(asyncTask);
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Tests PUTing an AsyncFile to S3 bucket.
     *
     * @param aContext A testing environment
     */
    @Test
    @Ignore
    public void testPutAsyncFile(final TestContext aContext) {
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        myContext.vertx().fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), open -> {
            if (open.succeeded()) {
                myClient.put(myTestBucket, s3Key, open.result(), put -> {
                    if (put.succeeded()) {
                        complete(asyncTask);
                    } else {
                        aContext.fail(put.cause());
                    }
                });
            } else {
                aContext.fail(open.cause());
            }
        });
    }

    /**
     * Tests PUTing an AsyncFile to S3 bucket.
     *
     * @param aContext A testing environment
     */
    @Test
    @Ignore
    public void testPutAsyncFileExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        myContext.vertx().fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), open -> {
            if (open.succeeded()) {
                myClient.put(myTestBucket, s3Key, open.result(), put -> {
                    if (put.succeeded()) {
                        complete(asyncTask);
                    } else {
                        aContext.fail(put.cause());
                    }
                });
            } else {
                aContext.fail(open.cause());
            }
        });
    }

    /**
     * Tests putting an AsyncFile with metadata using a future.
     *
     * @param aContext A test context
     */
    @Test
    @Ignore
    public final void testPutBucketKeyAsyncFileMetadata(final TestContext aContext) {
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.THREE, TestConstants.FOUR);
        final Async asyncTask = aContext.async();

        myContext.vertx().fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), open -> {
            if (open.succeeded()) {
                myClient.put(myTestBucket, s3Key, open.result(), metadata).onComplete(put -> {
                    if (put.succeeded()) {
                        aContext.assertTrue(myS3Client.doesObjectExist(myTestBucket, s3Key));

                        if (!asyncTask.isCompleted()) {
                            final S3Object s3Obj = myS3Client.getObject(myTestBucket, s3Key);
                            final String name = metadata.getName(0);
                            final String value = metadata.getValue(0);

                            aContext.assertEquals(value, s3Obj.getObjectMetadata().getUserMetaDataOf(name));
                        }

                        if (!asyncTask.isCompleted()) {
                            assertTrue(put.result().contains(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
                        }

                        complete(asyncTask);
                    } else {
                        aContext.fail(put.cause());
                    }
                });
            } else {
                aContext.fail(open.cause());
            }
        });
    }

    /**
     * Tests PUTing an AsyncFile to S3 bucket.
     *
     * @param aContext A testing environment
     */
    @Test
    @Ignore
    public void testPutAsyncFileUserMetadata(final TestContext aContext) {
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.THREE, TestConstants.FOUR);

        myContext.vertx().fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), open -> {
            if (open.succeeded()) {
                myClient.put(myTestBucket, s3Key, open.result(), metadata, put -> {
                    if (put.succeeded()) {
                        complete(asyncTask);
                    } else {
                        aContext.fail(put.cause());
                    }
                });
            } else {
                aContext.fail(open.cause());
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
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final Async asyncTask = aContext.async();

        myClient.delete(myTestBucket, s3Key).onComplete(deletion -> {
            if (deletion.failed()) {
                aContext.fail(deletion.cause());
            } else {
                aContext.assertFalse(myS3Client.doesObjectExist(myTestBucket, s3Key));
                complete(asyncTask);
            }
        });
    }

    /**
     * Test for {@link S3Client#delete(String, String, Handler)}
     *
     * @param aContext A test context
     */
    @Test
    public void testDelete(final TestContext aContext) {
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final Async asyncTask = aContext.async();

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.delete(myTestBucket, s3Key, delete -> {
                if (delete.succeeded()) {
                    complete(asyncTask);
                } else {
                    aContext.fail(delete.cause());
                }
            });
        }
    }

    /**
     * Tests PUTing an AsyncFile to S3 bucket.
     *
     * @param aContext A testing environment
     */
    @Test
    public void testPutAsyncFileUserMetadataExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.THREE, TestConstants.FOUR);

        myContext.vertx().fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), open -> {
            if (open.succeeded()) {
                final AsyncFile file = open.result();

                myClient.put(myTestBucket, s3Key, file, metadata, put -> {
                    if (put.succeeded()) {
                        complete(asyncTask);
                    } else {
                        aContext.fail(put.cause());
                    }

                    file.close();
                });
            } else {
                aContext.fail(open.cause());
            }
        });
    }

    /**
     * Test for {@link S3Client#delete(String, String, Handler)}
     *
     * @param aContext A test context
     */
    @Test
    public void testDeleteExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.delete(myTestBucket, s3Key, delete -> {
                if (delete.succeeded()) {
                    complete(asyncTask);
                } else {
                    aContext.fail(delete.cause());
                }
            });
        }
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(S3ClientIT.class, MessageCodes.BUNDLE);
    }

    private boolean createResource(final String aResource, final TestContext aContext, final Async aAsync) {
        return createResources(new String[] { aResource }, aContext, aAsync);
    }

    private boolean createResources(final String[] aResourceArray, final TestContext aContext, final Async aAsync) {
        for (final String resource : aResourceArray) {
            try {
                myS3Client.putObject(myTestBucket, resource, TEST_FILE);
            } catch (final AmazonClientException details) {
                LOGGER.error(details, details.getMessage());
                aContext.fail(details);
                return false;
            }
        }

        return true;
    }

}
