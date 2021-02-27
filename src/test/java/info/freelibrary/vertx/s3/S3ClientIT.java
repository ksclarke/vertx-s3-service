
package info.freelibrary.vertx.s3;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
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

import info.freelibrary.util.HTTP;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import info.freelibrary.vertx.s3.util.MessageCodes;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
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

    private static final String EOL = System.lineSeparator();

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
        final Endpoint endpoint = S3Endpoint.US_EAST_1; //
                                                        // myRegion.getServiceEndpoint(TestConstants.S3_SERVICE));
        final AwsProfile awsProfile = new AwsProfile(TestConstants.TEST_PROFILE);
        final Vertx vertx = myContext.vertx();

        myTestID = UUID.randomUUID().toString();
        myClient = new S3Client(vertx, awsProfile, new S3ClientOptions().setEndpoint(endpoint));

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
        final Async asyncTask = aContext.async();

        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

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
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.head(myTestBucket, s3Key, head -> {
                if (head.succeeded()) {
                    final HttpClientResponse response = head.result();
                    final int statusCode = response.statusCode();

                    if (statusCode != HTTP.OK) {
                        aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, HttpMethod.HEAD, s3Key, statusCode,
                                response.statusMessage()));
                    } else {
                        final String contentLength = response.getHeader(HttpHeaders.CONTENT_LENGTH);

                        aContext.assertNotNull(contentLength);
                        aContext.assertTrue(Integer.parseInt(contentLength) > 0);

                        complete(asyncTask);
                    }
                } else {
                    aContext.fail(head.cause());
                }
            }, error -> {
                aContext.fail(error);
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
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.head(myTestBucket, s3Key, head -> {
                if (head.succeeded()) {
                    final HttpClientResponse response = head.result();
                    final int statusCode = response.statusCode();

                    if (statusCode != HTTP.OK) {
                        aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, HttpMethod.HEAD, s3Key, statusCode,
                                response.statusMessage()));
                    } else {
                        final String contentLength = response.getHeader(HttpHeaders.CONTENT_LENGTH);

                        aContext.assertNotNull(contentLength);
                        aContext.assertTrue(Integer.parseInt(contentLength) > 0);

                        complete(asyncTask);
                    }
                } else {
                    aContext.fail(head.cause());
                }
            }, exception -> {
                aContext.fail(exception);
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
     * Tests LISTing an S3 bucket.
     *
     * @param aContext A testing environment
     */
    @Test
    public void testList(final TestContext aContext) {
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keysArray = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };

        if (createResources(keysArray, aContext, asyncTask)) {
            myClient.list(myTestBucket, list -> {
                if (list.succeeded()) {
                    final HttpClientResponse response = list.result();
                    final int statusCode = response.statusCode();

                    if (statusCode == HTTP.OK) {
                        response.bodyHandler(body -> {
                            try {
                                final BucketList bucketList = new BucketList(body);

                                for (final String key : keysArray) {
                                    aContext.assertTrue(bucketList.containsKey(key));
                                }

                                aContext.assertEquals(bucketList.size(), keysArray.length);
                                complete(asyncTask);
                            } catch (final IOException details) {
                                aContext.fail(details);
                            }
                        });
                    } else {
                        aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, HttpMethod.GET, TestConstants.PATH_FROM,
                                statusCode, response.statusMessage()));
                    }
                } else {
                    aContext.fail(list.cause());
                }
            }, error -> {
                aContext.fail(error);
            });
        }
    }

    /**
     * Tests LISTing an S3 bucket.
     *
     * @param aContext A testing environment
     */
    @Test
    public void testListExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keysArray = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };

        if (createResources(keysArray, aContext, asyncTask)) {
            myClient.list(myTestBucket, list -> {
                if (list.succeeded()) {
                    final HttpClientResponse response = list.result();
                    final int statusCode = response.statusCode();

                    if (statusCode == HTTP.OK) {
                        response.bodyHandler(body -> {
                            try {
                                final BucketList bucketList = new BucketList(body);

                                for (final String key : keysArray) {
                                    aContext.assertTrue(bucketList.containsKey(key));
                                }

                                aContext.assertEquals(bucketList.size(), keysArray.length);
                                complete(asyncTask);
                            } catch (final IOException details) {
                                aContext.fail(details);
                            }
                        });
                    } else {
                        aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, HttpMethod.GET, TestConstants.PATH_FROM,
                                statusCode, response.statusMessage()));
                    }
                } else {
                    aContext.fail(list.cause());
                }
            }, error -> {
                aContext.fail(error.getMessage());
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
                    final BucketList bucketList = list.result();

                    aContext.assertEquals(2, list.result().size());
                    aContext.assertTrue(bucketList.containsKey(keysArray[2]));
                    aContext.assertTrue(bucketList.containsKey(keysArray[3]));

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
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keysArray = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };

        if (createResources(keysArray, aContext, asyncTask)) {
            myClient.list(myTestBucket, TestConstants.PATH_FROM, list -> {
                if (list.succeeded()) {
                    final HttpClientResponse response = list.result();
                    final int statusCode = response.statusCode();

                    if (statusCode == HTTP.OK) {
                        response.bodyHandler(body -> {
                            try {
                                final BucketList bucketList = new BucketList(body);

                                aContext.assertEquals(bucketList.size(), 2);
                                aContext.assertTrue(bucketList.containsKey(keysArray[2]));
                                aContext.assertTrue(bucketList.containsKey(keysArray[3]));

                                complete(asyncTask);
                            } catch (final IOException details) {
                                aContext.fail(details);
                            }
                        });
                    } else {
                        aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, HttpMethod.GET, TestConstants.PATH_FROM,
                                statusCode, response.statusMessage()));
                    }
                } else {
                    aContext.fail(list.cause());
                }
            }, error -> {
                aContext.fail(error);
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
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keysArray = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };

        if (createResources(keysArray, aContext, asyncTask)) {
            myClient.list(myTestBucket, TestConstants.PATH_FROM, list -> {
                if (list.succeeded()) {
                    final HttpClientResponse response = list.result();
                    final int statusCode = response.statusCode();

                    if (statusCode == HTTP.OK) {
                        response.bodyHandler(body -> {
                            try {
                                final BucketList bucketList = new BucketList(body);

                                aContext.assertEquals(bucketList.size(), 2);
                                aContext.assertTrue(bucketList.containsKey(keysArray[2]));
                                aContext.assertTrue(bucketList.containsKey(keysArray[3]));

                                complete(asyncTask);
                            } catch (final IOException details) {
                                aContext.fail(details);
                            }
                        });
                    } else {
                        aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, HttpMethod.GET, TestConstants.PATH_FROM,
                                statusCode, response.statusMessage()));
                    }
                } else {
                    aContext.fail(list.cause());
                }
            }, error -> {
                aContext.fail(error.getMessage());
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
                    aContext.assertEquals(85, get.result().length());
                    complete(asyncTask);
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
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.get(myTestBucket, s3Key, get -> {
                if (get.succeeded()) {
                    final HttpClientResponse response = get.result();
                    final int statusCode = response.statusCode();

                    if (statusCode != HTTP.OK) {
                        aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, HttpMethod.GET, s3Key, statusCode,
                                response.statusMessage()));
                    } else {
                        response.bodyHandler(buffer -> {
                            if (buffer.length() != myResource.length) {
                                aContext.fail(LOGGER.getMessage(MessageCodes.VSS_004, buffer.length()));
                            } else {
                                complete(asyncTask);
                            }
                        });
                    }
                } else {
                    aContext.fail(get.cause());
                }
            }, error -> {
                aContext.fail(error);
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
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.get(myTestBucket, s3Key, get -> {
                if (get.succeeded()) {
                    final HttpClientResponse response = get.result();
                    final int statusCode = response.statusCode();

                    if (statusCode != HTTP.OK) {
                        aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, HttpMethod.GET, s3Key, statusCode,
                                response.statusMessage()));
                    } else {
                        response.bodyHandler(body -> {
                            if (body.length() != myResource.length) {
                                aContext.fail(LOGGER.getMessage(MessageCodes.VSS_004, body.length()));
                            } else {
                                complete(asyncTask);
                            }
                        });
                    }
                } else {
                    aContext.fail(get.cause());
                }
            }, error -> {
                aContext.fail(error.getMessage());
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
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), put -> {
            if (put.succeeded()) {
                final HttpClientResponse response = put.result();
                final int statusCode = response.statusCode();

                if (statusCode != HTTP.OK) {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, HttpMethod.PUT, s3Key, statusCode,
                            response.statusMessage()));
                } else {
                    asyncTask.complete();
                }
            } else {
                aContext.fail(put.cause());
            }
        }, error -> {
            aContext.fail(error);
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
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), put -> {
            if (put.succeeded()) {
                final HttpClientResponse response = put.result();
                final int statusCode = response.statusCode();

                if (statusCode != HTTP.OK) {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, HttpMethod.PUT, s3Key, statusCode,
                            response.statusMessage()));
                } else {
                    complete(asyncTask);
                }
            } else {
                aContext.fail(put.cause());
            }
        }, error -> {
            aContext.fail(error.getMessage());
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
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.ONE, TestConstants.TWO);

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), metadata, put -> {
            if (put.succeeded()) {
                final HttpClientResponse response = put.result();
                final int statusCode = response.statusCode();

                if (statusCode != HTTP.OK) {
                    response.bodyHandler(body -> {
                        aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, HttpMethod.PUT, s3Key, statusCode,
                                String.join(EOL, response.statusMessage(), body.toString())));
                    });
                } else {
                    complete(asyncTask);
                }
            } else {
                aContext.fail(put.cause());
            }
        }, error -> {
            aContext.fail(error);
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
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.ONE, TestConstants.TWO);

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), metadata, put -> {
            if (put.succeeded()) {
                final HttpClientResponse response = put.result();
                final int statusCode = response.statusCode();

                if (statusCode != HTTP.OK) {
                    response.bodyHandler(body -> {
                        aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, HttpMethod.PUT, s3Key, statusCode,
                                String.join(EOL, response.statusMessage(), body.toString())));
                    });
                } else {
                    complete(asyncTask);
                }
            } else {
                aContext.fail(put.cause());
            }
        }, error -> {
            aContext.fail(error.getMessage());
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

        myClient.put(myTestBucket, s3Key, file, TEST_FILE.length()).onComplete(put -> {
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
                myClient.put(myTestBucket, s3Key, open.result(), TEST_FILE.length(), put -> {
                    if (put.succeeded()) {
                        final HttpClientResponse response = put.result();
                        final int statusCode = response.statusCode();

                        if (statusCode != HTTP.OK) {
                            response.bodyHandler(body -> {
                                aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, HttpMethod.PUT, s3Key, statusCode,
                                        String.join(EOL, response.statusMessage(), body.toString())));
                            });
                        } else {
                            complete(asyncTask);
                        }
                    } else {
                        aContext.fail(put.cause());
                    }
                }, error -> {
                    aContext.fail(error);
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
                myClient.put(myTestBucket, s3Key, open.result(), TEST_FILE.length(), put -> {
                    if (put.succeeded()) {
                        final HttpClientResponse response = put.result();
                        final int statusCode = response.statusCode();

                        if (statusCode != HTTP.OK) {
                            response.bodyHandler(body -> {
                                final String message = String.join(EOL, response.statusMessage(), body.toString());
                                final Object[] details = new Object[] { HttpMethod.PUT, s3Key, statusCode, message };

                                aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, details));
                            });
                        } else {
                            complete(asyncTask);
                        }
                    } else {
                        aContext.fail(put.cause());
                    }
                }, error -> {
                    aContext.fail(error);
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
                myClient.put(myTestBucket, s3Key, open.result(), TEST_FILE.length(), metadata).onComplete(put -> {
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
                myClient.put(myTestBucket, s3Key, open.result(), TEST_FILE.length(), metadata, put -> {
                    if (put.succeeded()) {
                        final HttpClientResponse response = put.result();
                        final int statusCode = response.statusCode();

                        if (statusCode != HTTP.OK) {
                            response.bodyHandler(body -> {
                                final String message = String.join(EOL, response.statusMessage(), body.toString());
                                final Object[] details = new Object[] { HttpMethod.PUT, s3Key, statusCode, message };

                                aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, details));
                            });
                        } else {
                            complete(asyncTask);
                        }
                    } else {
                        aContext.fail(put.cause());
                    }
                }, error -> {
                    aContext.fail(error);
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
        LOGGER.info(MessageCodes.VSS_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.delete(myTestBucket, s3Key, delete -> {
                if (delete.succeeded()) {
                    final HttpClientResponse response = delete.result();
                    final int statusCode = response.statusCode();

                    if (statusCode != HTTP.NO_CONTENT) {
                        response.bodyHandler(body -> {
                            final String message = String.join(EOL, response.statusMessage(), body.toString());
                            final Object[] details = new Object[] { HttpMethod.DELETE, s3Key, statusCode, message };

                            aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, details));
                        });
                    } else {
                        complete(asyncTask);
                    }
                }
            }, error -> {
                aContext.fail(error);
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
                myClient.put(myTestBucket, s3Key, open.result(), TEST_FILE.length(), metadata, put -> {
                    if (put.succeeded()) {
                        final HttpClientResponse response = put.result();
                        final int statusCode = response.statusCode();

                        if (statusCode != HTTP.OK) {
                            response.bodyHandler(body -> {
                                final String message = String.join(EOL, response.statusMessage(), body.toString());
                                final Object[] details = new Object[] { HttpMethod.PUT, s3Key, statusCode, message };

                                aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, details));
                            });
                        } else {
                            complete(asyncTask);
                        }
                    } else {
                        aContext.fail(put.cause());
                    }
                }, error -> {
                    aContext.fail(error.getMessage());
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
                    final HttpClientResponse response = delete.result();
                    final int statusCode = response.statusCode();

                    if (statusCode != HTTP.NO_CONTENT) {
                        response.bodyHandler(body -> {
                            final String message = String.join(EOL, response.statusMessage(), body.toString());
                            final Object[] details = new Object[] { HttpMethod.DELETE, s3Key, statusCode, message };

                            aContext.fail(LOGGER.getMessage(MessageCodes.VSS_001, details));
                        });
                    } else {
                        complete(asyncTask);
                    }
                } else {
                    aContext.fail(delete.cause());
                }
            }, error -> {
                aContext.fail(error.getMessage());
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
