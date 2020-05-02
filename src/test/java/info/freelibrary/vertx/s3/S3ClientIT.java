
package info.freelibrary.vertx.s3;

import static info.freelibrary.vertx.s3.Constants.BUNDLE_NAME;

import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import com.amazonaws.AmazonClientException;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Integration tests for {@link S3Client}.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientIT extends AbstractS3IT {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientIT.class, BUNDLE_NAME);

    @Rule
    public RunTestOnContext myContext = new RunTestOnContext();

    @Rule
    public TestName myName = new TestName();

    /** The S3 client being used in the tests */
    private S3Client myClient;

    /** The S3 client used for V2 signing in the tests */
    private S3Client myV2SigClient;

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
        final String endpoint = myRegion.getServiceEndpoint(TestConstants.S3_SERVICE);
        final Profile profile = new Profile(TestConstants.TEST_PROFILE);
        final Vertx vertx = myContext.vertx();

        myTestID = UUID.randomUUID().toString();
        myClient = new S3Client(vertx, profile, endpoint);
        myV2SigClient = new S3Client(vertx, profile, endpoint).useV2Signature(true);

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

        if (myV2SigClient != null) {
            myV2SigClient.close();
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
        new S3Client(myContext.vertx(), new HttpClientOptions());
    }

    /**
     * Test constructor with endpoint.
     *
     * @param aContext A test context
     */
    @Test
    public void testConstructorWithEndpoint(final TestContext aContext) {
        new S3Client(myContext.vertx(), S3Client.DEFAULT_ENDPOINT);
    }

    /**
     * Test constructor with access and secret keys.
     *
     * @param aContext A test context
     */
    @Test
    public void testConstructorWithAccessSecretKeys(final TestContext aContext) {
        new S3Client(myContext.vertx(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    /**
     * Test for {@link S3Client#head(String, String, Handler)}
     *
     * @param aContext A test context
     */
    @Test
    public void testHead(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.head(myTestBucket, s3Key, head -> {
                final int statusCode = head.statusCode();

                if (statusCode != HTTP.OK) {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.HEAD, s3Key, statusCode, head
                            .statusMessage()));
                } else {
                    final String contentLength = head.getHeader(HTTP.CONTENT_LENGTH);

                    aContext.assertNotNull(contentLength);
                    aContext.assertTrue(Integer.parseInt(contentLength) > 0);
                    complete(asyncTask);
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
    public void testHeadV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myV2SigClient.head(myTestBucket, s3Key, head -> {
                final int statusCode = head.statusCode();

                if (statusCode != HTTP.OK) {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.HEAD, s3Key, statusCode, head
                            .statusMessage()));
                } else {
                    final String contentLength = head.getHeader(HTTP.CONTENT_LENGTH);

                    aContext.assertNotNull(contentLength);
                    aContext.assertTrue(Integer.parseInt(contentLength) > 0);
                    complete(asyncTask);
                }
            });
        }
    }

    /**
     * Test for {@link S3Client#head(String, String, Handler, Handler)}
     *
     * @param aContext A test context
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testHeadExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.head(myTestBucket, s3Key, head -> {
                final int statusCode = head.statusCode();

                if (statusCode != HTTP.OK) {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.HEAD, s3Key, statusCode, head
                            .statusMessage()));
                } else {
                    final String contentLength = head.getHeader(HTTP.CONTENT_LENGTH);

                    aContext.assertNotNull(contentLength);
                    aContext.assertTrue(Integer.parseInt(contentLength) > 0);
                    complete(asyncTask);
                }
            }, exception -> {
                aContext.fail(exception);
            });
        }
    }

    /**
     * Test for {@link S3Client#head(String, String, Handler, Handler)}
     *
     * @param aContext A test context
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testHeadExceptionHandlerV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myV2SigClient.head(myTestBucket, s3Key, head -> {
                final int statusCode = head.statusCode();

                if (statusCode != HTTP.OK) {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.HEAD, s3Key, statusCode, head
                            .statusMessage()));
                } else {
                    final String contentLength = head.getHeader(HTTP.CONTENT_LENGTH);

                    aContext.assertNotNull(contentLength);
                    aContext.assertTrue(Integer.parseInt(contentLength) > 0);
                    complete(asyncTask);
                }
            }, exception -> {
                aContext.fail(exception);
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
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keys = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };

        if (createResources(keys, aContext, asyncTask)) {
            myClient.list(myTestBucket, list -> {
                final int statusCode = list.statusCode();

                if (statusCode == HTTP.OK) {
                    list.bodyHandler(body -> {
                        try {
                            final BucketList bucketList = new BucketList(body);

                            for (final String key : keys) {
                                aContext.assertTrue(bucketList.containsKey(key));
                            }

                            aContext.assertEquals(bucketList.size(), keys.length);
                            complete(asyncTask);
                        } catch (final IOException details) {
                            aContext.fail(details);
                        }
                    });
                } else {
                    final String keyValues = StringUtils.toString(keys, '|');
                    final String status = list.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.LIST, keyValues, statusCode, status));
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
    public void testListV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keys = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };

        if (createResources(keys, aContext, asyncTask)) {
            myV2SigClient.list(myTestBucket, list -> {
                final int statusCode = list.statusCode();

                if (statusCode == HTTP.OK) {
                    list.bodyHandler(body -> {
                        try {
                            final BucketList bucketList = new BucketList(body);

                            for (final String key : keys) {
                                aContext.assertTrue(bucketList.containsKey(key));
                            }

                            aContext.assertEquals(bucketList.size(), keys.length);
                            complete(asyncTask);
                        } catch (final IOException details) {
                            aContext.fail(details);
                        }
                    });
                } else {
                    final String keyValues = StringUtils.toString(keys, '|');
                    final String status = list.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.LIST, keyValues, statusCode, status));
                }
            });
        }
    }

    /**
     * Tests LISTing an S3 bucket.
     *
     * @param aContext A testing environment
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testListExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keys = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };

        if (createResources(keys, aContext, asyncTask)) {
            myClient.list(myTestBucket, list -> {
                final int statusCode = list.statusCode();

                if (statusCode == HTTP.OK) {
                    list.bodyHandler(body -> {
                        try {
                            final BucketList bucketList = new BucketList(body);

                            for (final String key : keys) {
                                aContext.assertTrue(bucketList.containsKey(key));
                            }

                            aContext.assertEquals(bucketList.size(), keys.length);
                            complete(asyncTask);
                        } catch (final IOException details) {
                            aContext.fail(details);
                        }
                    });
                } else {
                    final String keyValues = StringUtils.toString(keys, '|');
                    final String status = list.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.LIST, keyValues, statusCode, status));
                }
            }, exception -> {
                aContext.fail(exception.getMessage());
            });
        }
    }

    /**
     * Tests LISTing an S3 bucket.
     *
     * @param aContext A testing environment
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testListExceptionHandlerV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keys = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };

        if (createResources(keys, aContext, asyncTask)) {
            myV2SigClient.list(myTestBucket, list -> {
                final int statusCode = list.statusCode();

                if (statusCode == HTTP.OK) {
                    list.bodyHandler(body -> {
                        try {
                            final BucketList bucketList = new BucketList(body);

                            for (final String key : keys) {
                                aContext.assertTrue(bucketList.containsKey(key));
                            }

                            aContext.assertEquals(bucketList.size(), keys.length);
                            complete(asyncTask);
                        } catch (final IOException details) {
                            aContext.fail(details);
                        }
                    });
                } else {
                    final String keyValues = StringUtils.toString(keys, '|');
                    final String status = list.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.LIST, keyValues, statusCode, status));
                }
            }, exception -> {
                aContext.fail(exception.getMessage());
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
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keys = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };

        if (createResources(keys, aContext, asyncTask)) {
            myClient.list(myTestBucket, TestConstants.PATH_FROM, list -> {
                final int statusCode = list.statusCode();

                if (statusCode == HTTP.OK) {
                    list.bodyHandler(body -> {
                        try {
                            final BucketList bucketList = new BucketList(body);

                            aContext.assertEquals(bucketList.size(), 2);
                            aContext.assertTrue(bucketList.containsKey(keys[2]));
                            aContext.assertTrue(bucketList.containsKey(keys[3]));

                            complete(asyncTask);
                        } catch (final IOException details) {
                            aContext.fail(details);
                        }
                    });
                } else {
                    final String keyValues = StringUtils.toString(keys, '|');
                    final String status = list.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.LIST, keyValues, statusCode, status));
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
    public void testListWithPrefixV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keys = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };

        if (createResources(keys, aContext, asyncTask)) {
            myV2SigClient.list(myTestBucket, TestConstants.PATH_FROM, list -> {
                final int statusCode = list.statusCode();

                if (statusCode == HTTP.OK) {
                    list.bodyHandler(body -> {
                        try {
                            final BucketList bucketList = new BucketList(body);

                            aContext.assertEquals(bucketList.size(), 2);
                            aContext.assertTrue(bucketList.containsKey(keys[2]));
                            aContext.assertTrue(bucketList.containsKey(keys[3]));

                            complete(asyncTask);
                        } catch (final IOException details) {
                            aContext.fail(details);
                        }
                    });
                } else {
                    final String keyValues = StringUtils.toString(keys, '|');
                    final String status = list.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.LIST, keyValues, statusCode, status));
                }
            });
        }
    }

    /**
     * Tests LISTing with a prefix.
     *
     * @param aContext A testing environment
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testListWithPrefixExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keys = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };

        if (createResources(keys, aContext, asyncTask)) {
            myClient.list(myTestBucket, TestConstants.PATH_FROM, list -> {
                final int statusCode = list.statusCode();

                if (statusCode == HTTP.OK) {
                    list.bodyHandler(body -> {
                        try {
                            final BucketList bucketList = new BucketList(body);

                            aContext.assertEquals(bucketList.size(), 2);
                            aContext.assertTrue(bucketList.containsKey(keys[2]));
                            aContext.assertTrue(bucketList.containsKey(keys[3]));

                            complete(asyncTask);
                        } catch (final IOException details) {
                            aContext.fail(details);
                        }
                    });
                } else {
                    final String keyValues = StringUtils.toString(keys, '|');
                    final String status = list.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.LIST, keyValues, statusCode, status));
                }
            }, exception -> {
                aContext.fail(exception.getMessage());
            });
        }
    }

    /**
     * Tests LISTing with a prefix.
     *
     * @param aContext A testing environment
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testListWithPrefixExceptionHandlerV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keys = { TestConstants.PATH_TO_ONE + myTestID, TestConstants.PATH_TO_TWO + myTestID,
            TestConstants.PATH_FROM_ONE + myTestID, TestConstants.PATH_FROM_TWO + myTestID };

        if (createResources(keys, aContext, asyncTask)) {
            myV2SigClient.list(myTestBucket, TestConstants.PATH_FROM, list -> {
                final int statusCode = list.statusCode();

                if (statusCode == HTTP.OK) {
                    list.bodyHandler(body -> {
                        try {
                            final BucketList bucketList = new BucketList(body);

                            aContext.assertEquals(bucketList.size(), 2);
                            aContext.assertTrue(bucketList.containsKey(keys[2]));
                            aContext.assertTrue(bucketList.containsKey(keys[3]));

                            complete(asyncTask);
                        } catch (final IOException details) {
                            aContext.fail(details);
                        }
                    });
                } else {
                    final String keyValues = StringUtils.toString(keys, '|');
                    final String status = list.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.LIST, keyValues, statusCode, status));
                }
            }, exception -> {
                aContext.fail(exception.getMessage());
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
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.get(myTestBucket, s3Key, get -> {
                final int statusCode = get.statusCode();

                if (statusCode != HTTP.OK) {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.GET, s3Key, statusCode, get
                            .statusMessage()));
                } else {
                    get.bodyHandler(buffer -> {
                        if (buffer.length() != myResource.length) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.VS3_004, buffer.length()));
                        } else {
                            asyncTask.complete();
                        }
                    });
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
    public void testGetV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myV2SigClient.get(myTestBucket, s3Key, get -> {
                final int statusCode = get.statusCode();

                if (statusCode != HTTP.OK) {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.GET, s3Key, statusCode, get
                            .statusMessage()));
                } else {
                    get.bodyHandler(body -> {
                        if (body.length() != myResource.length) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.VS3_004, body.length()));
                        } else {
                            asyncTask.complete();
                        }
                    });
                }
            });
        }
    }

    /**
     * Test for {@link S3Client#get(String, String, Handler)}
     *
     * @param aContext A test context
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testGetExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.get(myTestBucket, s3Key, get -> {
                final int statusCode = get.statusCode();

                if (statusCode != HTTP.OK) {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.GET, s3Key, statusCode, get
                            .statusMessage()));
                } else {
                    get.bodyHandler(body -> {
                        if (body.length() != myResource.length) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.VS3_004, body.length()));
                        } else {
                            asyncTask.complete();
                        }
                    });
                }
            }, exception -> {
                aContext.fail(exception.getMessage());
            });
        }
    }

    /**
     * Test for {@link S3Client#get(String, String, Handler)}
     *
     * @param aContext A test context
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testGetExceptionHandlerV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myV2SigClient.get(myTestBucket, s3Key, get -> {
                final int statusCode = get.statusCode();

                if (statusCode != HTTP.OK) {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.GET, s3Key, statusCode, get
                            .statusMessage()));
                } else {
                    get.bodyHandler(body -> {
                        if (body.length() != myResource.length) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.VS3_004, body.length()));
                        } else {
                            asyncTask.complete();
                        }
                    });
                }
            }, exception -> {
                aContext.fail(exception.getMessage());
            });
        }
    }

    /**
     * Testing PUTing data to an S3 object
     *
     * @param aContext A testing environment
     */
    @Test
    public void testPut(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), put -> {
            final int statusCode = put.statusCode();

            if (statusCode != HTTP.OK) {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                        .statusMessage()));
            } else {
                asyncTask.complete();
            }
        });
    }

    /**
     * Testing PUTing data to an S3 object
     *
     * @param aContext A testing environment
     */
    @Test
    public void testPutV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        myV2SigClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), put -> {
            final int statusCode = put.statusCode();

            if (statusCode != HTTP.OK) {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                        .statusMessage()));
            } else {
                asyncTask.complete();
            }
        });
    }

    /**
     * Testing PUTing data to an S3 object
     *
     * @param aContext A testing environment
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testPutExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), put -> {
            final int statusCode = put.statusCode();

            if (statusCode != HTTP.OK) {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                        .statusMessage()));
            } else {
                asyncTask.complete();
            }
        }, exception -> {
            aContext.fail(exception.getMessage());
        });
    }

    /**
     * Testing PUTing data to an S3 object
     *
     * @param aContext A testing environment
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testPutExceptionHandlerV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        myV2SigClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), put -> {
            final int statusCode = put.statusCode();

            if (statusCode != HTTP.OK) {
                aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                        .statusMessage()));
            } else {
                asyncTask.complete();
            }
        }, exception -> {
            aContext.fail(exception.getMessage());
        });
    }

    /**
     * Test PUTing with user metadata.
     *
     * @param aContext A testing context
     */
    @Test
    public void testPutUserMetadata(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.ONE, TestConstants.TWO);

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), metadata, put -> {
            final int statusCode = put.statusCode();

            if (statusCode != HTTP.OK) {
                put.bodyHandler(body -> {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                            .statusMessage() + System.lineSeparator() + body.toString()));
                });
            } else {
                asyncTask.complete();
            }
        });
    }

    /**
     * Test PUTing with user metadata.
     *
     * @param aContext A testing context
     */
    @Test
    public void testPutUserMetadataV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.ONE, TestConstants.TWO);

        myV2SigClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), metadata, put -> {
            final int statusCode = put.statusCode();

            if (statusCode != HTTP.OK) {
                put.bodyHandler(body -> {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                            .statusMessage() + System.lineSeparator() + body.toString()));
                });
            } else {
                asyncTask.complete();
            }
        });
    }

    /**
     * Test PUTing with user metadata.
     *
     * @param aContext A testing context
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testPutUserMetadataExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.ONE, TestConstants.TWO);

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), metadata, put -> {
            final int statusCode = put.statusCode();

            if (statusCode != HTTP.OK) {
                put.bodyHandler(body -> {
                    // We can get a more detailed error message
                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                            .statusMessage() + System.lineSeparator() + body.toString()));
                });
            } else {
                asyncTask.complete();
            }
        }, exception -> {
            aContext.fail(exception.getMessage());
        });
    }

    /**
     * Test PUTing with user metadata.
     *
     * @param aContext A testing context
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testPutUserMetadataExceptionHandlerV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.ONE, TestConstants.TWO);

        myV2SigClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), metadata, put -> {
            final int statusCode = put.statusCode();

            if (statusCode != HTTP.OK) {
                put.bodyHandler(body -> {
                    aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                            .statusMessage() + System.lineSeparator() + body.toString()));
                });
            } else {
                asyncTask.complete();
            }
        }, exception -> {
            aContext.fail(exception.getMessage());
        });
    }

    /**
     * Tests PUTing an AsyncFile to S3 bucket.
     *
     * @param aContext A testing environment
     */
    @Test
    public void testPutAsyncFile(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        myContext.vertx().fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), open -> {
            if (open.succeeded()) {
                myClient.put(myTestBucket, s3Key, open.result(), put -> {
                    final int statusCode = put.statusCode();

                    if (statusCode != HTTP.OK) {
                        put.bodyHandler(body -> {
                            aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                                    .statusMessage() + System.lineSeparator() + body.toString()));
                        });
                    } else {
                        asyncTask.complete();
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
    public void testPutAsyncFileV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        myContext.vertx().fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), open -> {
            if (open.succeeded()) {
                myV2SigClient.put(myTestBucket, s3Key, open.result(), put -> {
                    final int statusCode = put.statusCode();

                    if (statusCode != HTTP.OK) {
                        put.bodyHandler(body -> {
                            aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                                    .statusMessage() + System.lineSeparator() + body.toString()));
                        });
                    } else {
                        asyncTask.complete();
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
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testPutAsyncFileExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        myContext.vertx().fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), open -> {
            if (open.succeeded()) {
                myClient.put(myTestBucket, s3Key, open.result(), put -> {
                    final int statusCode = put.statusCode();

                    if (statusCode != HTTP.OK) {
                        put.bodyHandler(body -> {
                            aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                                    .statusMessage() + System.lineSeparator() + body.toString()));
                        });
                    } else {
                        asyncTask.complete();
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
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testPutAsyncFileExceptionHandlerV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        myContext.vertx().fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), open -> {
            if (open.succeeded()) {
                myV2SigClient.put(myTestBucket, s3Key, open.result(), put -> {
                    final int statusCode = put.statusCode();

                    if (statusCode != HTTP.OK) {
                        put.bodyHandler(body -> {
                            aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                                    .statusMessage() + System.lineSeparator() + body.toString()));
                        });
                    } else {
                        asyncTask.complete();
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
    public void testPutAsyncFileUserMetadata(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.THREE, TestConstants.FOUR);

        myContext.vertx().fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), open -> {
            if (open.succeeded()) {
                myClient.put(myTestBucket, s3Key, open.result(), metadata, put -> {
                    final int statusCode = put.statusCode();

                    if (statusCode != HTTP.OK) {
                        put.bodyHandler(body -> {
                            aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                                    .statusMessage() + System.lineSeparator() + body.toString()));
                        });
                    } else {
                        asyncTask.complete();
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
    public void testPutAsyncFileUserMetadataV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.THREE, TestConstants.FOUR);

        myContext.vertx().fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), open -> {
            if (open.succeeded()) {
                myV2SigClient.put(myTestBucket, s3Key, open.result(), metadata, put -> {
                    final int statusCode = put.statusCode();

                    if (statusCode != HTTP.OK) {
                        put.bodyHandler(body -> {
                            aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                                    .statusMessage() + System.lineSeparator() + body.toString()));
                        });
                    } else {
                        asyncTask.complete();
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
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testPutAsyncFileUserMetadataExceptionHandlerV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.THREE, TestConstants.FOUR);

        myContext.vertx().fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), open -> {
            if (open.succeeded()) {
                myV2SigClient.put(myTestBucket, s3Key, open.result(), metadata, put -> {
                    final int statusCode = put.statusCode();

                    if (statusCode != HTTP.OK) {
                        put.bodyHandler(body -> {
                            aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                                    .statusMessage() + System.lineSeparator() + body.toString()));
                        });
                    } else {
                        asyncTask.complete();
                    }
                }, exception -> {
                    aContext.fail(exception.getMessage());
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
    public void testDelete(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.delete(myTestBucket, s3Key, delete -> {
                final int statusCode = delete.statusCode();

                if (statusCode != HTTP.NO_CONTENT) {
                    delete.bodyHandler(body -> {
                        aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.DELETE, s3Key, statusCode, delete
                                .statusMessage() + System.lineSeparator() + body.toString()));
                    });
                } else {
                    asyncTask.complete();
                }
            });
        }
    }

    /**
     * Test for {@link S3Client#delete(String, String, Handler)}
     *
     * @param aContext A test context
     */
    @Test
    public void testDeleteV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myV2SigClient.delete(myTestBucket, s3Key, delete -> {
                final int statusCode = delete.statusCode();

                if (statusCode != HTTP.NO_CONTENT) {
                    delete.bodyHandler(body -> {
                        aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.DELETE, s3Key, statusCode, delete
                                .statusMessage() + System.lineSeparator() + body.toString()));
                    });
                } else {
                    asyncTask.complete();
                }
            });
        }
    }

    /**
     * Tests PUTing an AsyncFile to S3 bucket.
     *
     * @param aContext A testing environment
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testPutAsyncFileUserMetadataExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(TestConstants.THREE, TestConstants.FOUR);

        myContext.vertx().fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), open -> {
            if (open.succeeded()) {
                myClient.put(myTestBucket, s3Key, open.result(), metadata, put -> {
                    final int statusCode = put.statusCode();

                    if (statusCode != HTTP.OK) {
                        put.bodyHandler(body -> {
                            aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.PUT, s3Key, statusCode, put
                                    .statusMessage() + System.lineSeparator() + body.toString()));
                        });
                    } else {
                        asyncTask.complete();
                    }
                }, exception -> {
                    aContext.fail(exception.getMessage());
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
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testDeleteExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.delete(myTestBucket, s3Key, delete -> {
                final int statusCode = delete.statusCode();

                if (statusCode != HTTP.NO_CONTENT) {
                    delete.bodyHandler(body -> {
                        aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.DELETE, s3Key, statusCode, delete
                                .statusMessage() + System.lineSeparator() + body.toString()));
                    });
                } else {
                    asyncTask.complete();
                }
            }, exception -> {
                aContext.fail(exception.getMessage());
            });
        }
    }

    /**
     * Test for {@link S3Client#createDeleteRequest(String, String, Handler)}
     *
     * @param aContext A test context
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testDeleteExceptionHandlerV2(final TestContext aContext) {
        LOGGER.info(MessageCodes.VS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TestConstants.TEST_KEY_PREFIX + myTestID + TestConstants.TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myV2SigClient.delete(myTestBucket, s3Key, delete -> {
                final int statusCode = delete.statusCode();

                if (statusCode != HTTP.NO_CONTENT) {
                    delete.bodyHandler(body -> {
                        aContext.fail(LOGGER.getMessage(MessageCodes.VS3_001, HTTP.DELETE, s3Key, statusCode, delete
                                .statusMessage() + System.lineSeparator() + body.toString()));
                    });
                } else {
                    asyncTask.complete();
                }
            }, exception -> {
                aContext.fail(exception.getMessage());
            });
        }
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(S3ClientIT.class, BUNDLE_NAME);
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
