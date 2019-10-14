
package info.freelibrary.vertx.s3;

import static info.freelibrary.vertx.s3.Constants.BUNDLE_NAME;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.OpenOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Integration tests for {@link S3Client}.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientIT extends AbstractS3IT {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientIT.class, BUNDLE_NAME);

    private static final String TEST_PROFILE = "vertx-s3";

    private static final String TEST_KEY_PREFIX = "green-";

    private static final String TEST_KEY_SUFFIX = ".gif";

    private static final String PATH_TO_ONE = "path/to/one-";

    private static final String PATH_TO_TWO = "path/to/two-";

    private static final String PATH_FROM_ONE = "path/from/one-";

    private static final String PATH_FROM_TWO = "path/from/two-";

    private static final String PATH_FROM = "path/from";

    private static final String ONE = "one";

    private static final String TWO = "two";

    private static final String THREE = "three";

    private static final String FOUR = "four";

    @Rule
    public TestName myName = new TestName();

    /** The S3 client being used in the tests */
    private S3Client myClient;

    /** A test ID */
    private String myTestID;

    @Override
    @Before
    public void setUp(final TestContext aContext) {
        super.setUp(aContext);

        myTestID = UUID.randomUUID().toString();
        myClient = new S3Client(myVertx, myAccessKey, mySecretKey, myRegion.getServiceEndpoint("s3"));
    }

    @Override
    @After
    public void tearDown(final TestContext aContext) {
        super.tearDown(aContext);

        myClient.close();
    }

    /**
     * Test for a client constructor that's pulling information from an AWS credentials file.
     */
    @Test
    public void testProfileFromSystemCredentials() {
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Profile profile = new Profile(TEST_PROFILE);
        final AWSCredentials credentials = profile.getCredentials();

        assertEquals(myAccessKey, credentials.getAWSAccessKeyId());
        assertEquals(mySecretKey, credentials.getAWSSecretKey());
    }

    /**
     * Test for
     * {@link info.freelibrary.vertx.s3.S3Client#head(java.lang.String, java.lang.String, io.vertx.core.Handler)}
     */
    @Test
    public void testHead(final TestContext aContext) {
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.head(myTestBucket, s3Key, response -> {
                final int code = response.statusCode();

                if (code != 200) {
                    final String status = response.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.HEAD, s3Key, code, status));
                } else {
                    final String contentLength = response.getHeader(HTTP.CONTENT_LENGTH);

                    aContext.assertNotNull(contentLength);
                    aContext.assertTrue(Integer.parseInt(contentLength) > 0);

                    if (!asyncTask.isCompleted()) {
                        asyncTask.complete();
                    }
                }
            });
        }
    }

    /**
     * Test for {@link S3Client#head(String, String, Handler, Handler)}
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testHeadExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.head(myTestBucket, s3Key, response -> {
                final int code = response.statusCode();

                if (code != 200) {
                    final String status = response.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.HEAD, s3Key, code, status));
                } else {
                    final String contentLength = response.getHeader(HTTP.CONTENT_LENGTH);

                    aContext.assertNotNull(contentLength);
                    aContext.assertTrue(Integer.parseInt(contentLength) > 0);

                    if (!asyncTask.isCompleted()) {
                        asyncTask.complete();
                    }
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
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keys = { PATH_TO_ONE + myTestID, PATH_TO_TWO + myTestID, PATH_FROM_ONE + myTestID,
            PATH_FROM_TWO + myTestID };

        if (createResources(keys, aContext, asyncTask)) {
            myClient.list(myTestBucket, response -> {
                final int code = response.statusCode();

                if (code == 200) {
                    response.bodyHandler(buffer -> {
                        final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

                        saxParserFactory.setNamespaceAware(true);

                        try {
                            final SAXParser saxParser = saxParserFactory.newSAXParser();
                            final XMLReader xmlReader = saxParser.getXMLReader();
                            final ObjectListHandler s3ListHandler = new ObjectListHandler();
                            final List<String> keyList;

                            xmlReader.setContentHandler(s3ListHandler);
                            xmlReader.parse(new InputSource(new StringReader(buffer.toString())));
                            keyList = s3ListHandler.getKeys();

                            for (final String key : keys) {
                                aContext.assertTrue(keyList.contains(key), LOGGER.getMessage(MessageCodes.SS3_005,
                                        key));
                            }

                            aContext.assertEquals(keyList.size(), keys.length);

                            if (!asyncTask.isCompleted()) {
                                asyncTask.complete();
                            }
                        } catch (final ParserConfigurationException | SAXException | IOException details) {
                            aContext.fail(details);
                        }
                    });
                } else {
                    final String keyValues = StringUtils.toString(keys, '|');
                    final String status = response.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.LIST, keyValues, code, status));
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
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keys = { PATH_TO_ONE + myTestID, PATH_TO_TWO + myTestID, PATH_FROM_ONE + myTestID,
            PATH_FROM_TWO + myTestID };

        if (createResources(keys, aContext, asyncTask)) {
            myClient.list(myTestBucket, response -> {
                final int code = response.statusCode();

                if (code == 200) {
                    response.bodyHandler(buffer -> {
                        final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

                        saxParserFactory.setNamespaceAware(true);

                        try {
                            final SAXParser saxParser = saxParserFactory.newSAXParser();
                            final XMLReader xmlReader = saxParser.getXMLReader();
                            final ObjectListHandler s3ListHandler = new ObjectListHandler();
                            final List<String> keyList;

                            xmlReader.setContentHandler(s3ListHandler);
                            xmlReader.parse(new InputSource(new StringReader(buffer.toString())));
                            keyList = s3ListHandler.getKeys();

                            for (final String key : keys) {
                                aContext.assertTrue(keyList.contains(key), LOGGER.getMessage(MessageCodes.SS3_005,
                                        key));
                            }

                            aContext.assertEquals(keyList.size(), keys.length);

                            if (!asyncTask.isCompleted()) {
                                asyncTask.complete();
                            }
                        } catch (final ParserConfigurationException | SAXException | IOException details) {
                            aContext.fail(details);
                        }
                    });
                } else {
                    final String keyValues = StringUtils.toString(keys, '|');
                    final String status = response.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.LIST, keyValues, code, status));
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
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keys = { PATH_TO_ONE + myTestID, PATH_TO_TWO + myTestID, PATH_FROM_ONE + myTestID,
            PATH_FROM_TWO + myTestID };

        if (createResources(keys, aContext, asyncTask)) {
            myClient.list(myTestBucket, PATH_FROM, response -> {
                final int code = response.statusCode();

                if (code == 200) {
                    response.bodyHandler(buffer -> {
                        final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

                        saxParserFactory.setNamespaceAware(true);

                        try {
                            final SAXParser saxParser = saxParserFactory.newSAXParser();
                            final XMLReader xmlReader = saxParser.getXMLReader();
                            final ObjectListHandler s3ListHandler = new ObjectListHandler();
                            final List<String> keyList;

                            xmlReader.setContentHandler(s3ListHandler);
                            xmlReader.parse(new InputSource(new StringReader(buffer.toString())));

                            keyList = s3ListHandler.getKeys();

                            aContext.assertEquals(keyList.size(), 2);
                            aContext.assertTrue(keyList.contains(keys[2]));
                            aContext.assertTrue(keyList.contains(keys[3]));

                            if (!asyncTask.isCompleted()) {
                                asyncTask.complete();
                            }
                        } catch (final ParserConfigurationException | SAXException | IOException details) {
                            aContext.fail(details);
                        }
                    });
                } else {
                    final String keyValues = StringUtils.toString(keys, '|');
                    final String status = response.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.LIST, keyValues, code, status));
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
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String[] keys = { PATH_TO_ONE + myTestID, PATH_TO_TWO + myTestID, PATH_FROM_ONE + myTestID,
            PATH_FROM_TWO + myTestID };

        if (createResources(keys, aContext, asyncTask)) {
            myClient.list(myTestBucket, PATH_FROM, response -> {
                final int code = response.statusCode();

                if (code == 200) {
                    response.bodyHandler(buffer -> {
                        final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

                        saxParserFactory.setNamespaceAware(true);

                        try {
                            final SAXParser saxParser = saxParserFactory.newSAXParser();
                            final XMLReader xmlReader = saxParser.getXMLReader();
                            final ObjectListHandler s3ListHandler = new ObjectListHandler();
                            final List<String> keyList;

                            xmlReader.setContentHandler(s3ListHandler);
                            xmlReader.parse(new InputSource(new StringReader(buffer.toString())));

                            keyList = s3ListHandler.getKeys();

                            aContext.assertEquals(keyList.size(), 2);
                            aContext.assertTrue(keyList.contains(keys[2]));
                            aContext.assertTrue(keyList.contains(keys[3]));

                            if (!asyncTask.isCompleted()) {
                                asyncTask.complete();
                            }
                        } catch (final ParserConfigurationException | SAXException | IOException details) {
                            aContext.fail(details);
                        }
                    });
                } else {
                    final String keyValues = StringUtils.toString(keys, '|');
                    final String status = response.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.LIST, keyValues, code, status));
                }
            }, exception -> {
                aContext.fail(exception.getMessage());
            });
        }
    }

    /**
     * Test for
     * {@link info.freelibrary.vertx.s3.S3Client#get(java.lang.String, java.lang.String, io.vertx.core.Handler)}
     */
    @Test
    public void testGet(final TestContext aContext) {
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.get(myTestBucket, s3Key, response -> {
                final int code = response.statusCode();

                if (code != 200) {
                    final String status = response.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.GET, s3Key, code, status));
                } else {
                    response.bodyHandler(buffer -> {
                        if (buffer.length() != myResource.length) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.SS3_004, buffer.length()));
                        } else {
                            asyncTask.complete();
                        }
                    });
                }
            });
        }
    }

    /**
     * Test for
     * {@link info.freelibrary.vertx.s3.S3Client#get(java.lang.String, java.lang.String, io.vertx.core.Handler)}
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testGetExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.get(myTestBucket, s3Key, response -> {
                final int code = response.statusCode();

                if (code != 200) {
                    final String status = response.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.GET, s3Key, code, status));
                } else {
                    response.bodyHandler(buffer -> {
                        if (buffer.length() != myResource.length) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.SS3_004, buffer.length()));
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
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), response -> {
            final int code = response.statusCode();

            if (code != 200) {
                final String status = response.statusMessage();

                aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.PUT, s3Key, code, status));
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
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), response -> {
            final int code = response.statusCode();

            if (code != 200) {
                final String status = response.statusMessage();

                aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.PUT, s3Key, code, status));
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
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(ONE, TWO);

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), metadata, response -> {
            final int code = response.statusCode();

            if (code != 200) {
                response.bodyHandler(body -> {
                    final String status = response.statusMessage() + System.lineSeparator() + body.toString();

                    aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.PUT, s3Key, code, status));
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
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(ONE, TWO);

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), metadata, response -> {
            final int code = response.statusCode();

            if (code != 200) {
                response.bodyHandler(body -> {
                    final String status = response.statusMessage() + System.lineSeparator() + body.toString();

                    aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.PUT, s3Key, code, status));
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
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;

        myVertx.fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), openResult -> {
            if (openResult.succeeded()) {
                myClient.put(myTestBucket, s3Key, openResult.result(), response -> {
                    final int code = response.statusCode();

                    if (code != 200) {
                        response.bodyHandler(body -> {
                            final String status = response.statusMessage() + System.lineSeparator() + body.toString();

                            aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.PUT, s3Key, code, status));
                        });
                    } else {
                        asyncTask.complete();
                    }
                });
            } else {
                aContext.fail(openResult.cause());
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
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;

        myVertx.fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), openResult -> {
            if (openResult.succeeded()) {
                myClient.put(myTestBucket, s3Key, openResult.result(), response -> {
                    final int code = response.statusCode();

                    if (code != 200) {
                        response.bodyHandler(body -> {
                            final String status = response.statusMessage() + System.lineSeparator() + body.toString();

                            aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.PUT, s3Key, code, status));
                        });
                    } else {
                        asyncTask.complete();
                    }
                });
            } else {
                aContext.fail(openResult.cause());
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
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(THREE, FOUR);

        myVertx.fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), openResult -> {
            if (openResult.succeeded()) {
                myClient.put(myTestBucket, s3Key, openResult.result(), metadata, response -> {
                    final int code = response.statusCode();

                    if (code != 200) {
                        response.bodyHandler(body -> {
                            final String status = response.statusMessage() + System.lineSeparator() + body.toString();

                            aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.PUT, s3Key, code, status));
                        });
                    } else {
                        asyncTask.complete();
                    }
                });
            } else {
                aContext.fail(openResult.cause());
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
    public void testPutAsyncFileUserMetadataExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;
        final UserMetadata metadata = new UserMetadata(THREE, FOUR);

        myVertx.fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), openResult -> {
            if (openResult.succeeded()) {
                myClient.put(myTestBucket, s3Key, openResult.result(), metadata, response -> {
                    final int code = response.statusCode();

                    if (code != 200) {
                        response.bodyHandler(body -> {
                            final String status = response.statusMessage() + System.lineSeparator() + body.toString();

                            aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.PUT, s3Key, code, status));
                        });
                    } else {
                        asyncTask.complete();
                    }
                }, exception -> {
                    aContext.fail(exception.getMessage());
                });
            } else {
                aContext.fail(openResult.cause());
            }
        });
    }

    /**
     * Test for
     * {@link info.freelibrary.vertx.s3.S3Client#delete(java.lang.String, java.lang.String, io.vertx.core.Handler)}
     */
    @Test
    public void testDelete(final TestContext aContext) {
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.delete(myTestBucket, s3Key, response -> {
                final int code = response.statusCode();

                if (code != 204) {
                    response.bodyHandler(body -> {
                        final String status = response.statusMessage() + System.lineSeparator() + body.toString();

                        aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.DELETE, s3Key, code, status));
                    });
                } else {
                    asyncTask.complete();
                }
            });
        }
    }

    /**
     * Test for
     * {@link info.freelibrary.vertx.s3.S3Client#delete(java.lang.String, java.lang.String, io.vertx.core.Handler)}
     */
    @SuppressWarnings("checkstyle:Indentation")
    @Test
    public void testDeleteExceptionHandler(final TestContext aContext) {
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            myClient.delete(myTestBucket, s3Key, response -> {
                final int code = response.statusCode();

                if (code != 204) {
                    response.bodyHandler(body -> {
                        final String status = response.statusMessage() + System.lineSeparator() + body.toString();

                        aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.DELETE, s3Key, code, status));
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
     * Test for {@link S3Client#createPutRequest(String, String, Handler)}
     */
    @Test
    public void testCreatePutRequest(final TestContext aContext) {
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;

        final S3ClientRequest request = myClient.createPutRequest(myTestBucket, s3Key, response -> {
            final int code = response.statusCode();

            if (code != 200) {
                response.bodyHandler(body -> {
                    final String status = response.statusMessage() + System.lineSeparator() + body.toString();

                    aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.PUT, s3Key, code, status));
                });
            } else {
                asyncTask.complete();
            }
        });

        request.end(Buffer.buffer(myResource));
    }

    /**
     * Test for {@link S3Client#createGetRequest(String, String, Handler)}
     */
    @Test
    public void testCreateGetRequest(final TestContext aContext) {
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            final S3ClientRequest request = myClient.createGetRequest(myTestBucket, s3Key, response -> {
                final int code = response.statusCode();

                if (code != HTTP.OK) {
                    final String status = response.statusMessage();

                    aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.GET, s3Key, code, status));
                } else {
                    response.bodyHandler(buffer -> {
                        final byte[] bytes = buffer.getBytes();

                        if (bytes.length != myResource.length) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.SS3_004, bytes.length));
                        } else {
                            asyncTask.complete();
                        }
                    });
                }
            });

            request.end();
        }
    }

    /**
     * Test for {@link S3Client#createDeleteRequest(String, String, Handler)}
     */
    @Test
    public void testCreateDeleteRequest(final TestContext aContext) {
        LOGGER.info(MessageCodes.SS3_006, myName.getMethodName());

        final Async asyncTask = aContext.async();
        final String s3Key = TEST_KEY_PREFIX + myTestID + TEST_KEY_SUFFIX;

        if (createResource(s3Key, aContext, asyncTask)) {
            final S3ClientRequest request = myClient.createDeleteRequest(myTestBucket, s3Key, response -> {
                final int code = response.statusCode();

                if (code != 204) {
                    response.bodyHandler(body -> {
                        final String status = response.statusMessage() + System.lineSeparator() + body.toString();

                        aContext.fail(LOGGER.getMessage(MessageCodes.SS3_001, HTTP.DELETE, s3Key, code, status));
                    });
                } else {
                    asyncTask.complete();
                }
            });

            request.end();
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
