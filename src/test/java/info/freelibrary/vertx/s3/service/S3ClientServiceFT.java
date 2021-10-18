
package info.freelibrary.vertx.s3.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.amazonaws.SdkClientException;

import info.freelibrary.util.I18nRuntimeException;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import info.freelibrary.vertx.s3.AbstractS3FT;
import info.freelibrary.vertx.s3.S3Object;
import info.freelibrary.vertx.s3.TestConstants;
import info.freelibrary.vertx.s3.TestUtils;
import info.freelibrary.vertx.s3.util.MessageCodes;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests of the S3 client service.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientServiceFT extends AbstractS3FT {

    /**
     * The test logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientServiceFT.class, MessageCodes.BUNDLE);

    /**
     * The test file for the tests in this class.
     */
    private static final String TEST_FILE = "src/test/resources/green.gif";

    /**
     * The expected resource size.
     */
    private long myExpectedSize;

    /**
     * Sets up the testing environment.
     */
    @Override
    @Before
    public void setUp() {
        super.setUp();

        try {
            myExpectedSize = Files.size(Path.of(TEST_FILE));
        } catch (final IOException details) {
            throw new I18nRuntimeException(details);
        }
    }

    /**
     * Tests the <code>getBuffer()</code> function of an S3 service proxy.
     *
     * @param aContext A test context
     */
    @Test
    public final void testGetProxyWithOptsGetBuffer(final TestContext aContext) {
        final Async asyncTask = aContext.async();
        final Future<Void> put = storeTestFile();

        put.onFailure(aContext::fail);
        put.onSuccess(result -> {
            S3ClientService.getProxyWithOpts(myContext.vertx(), getConfig()).onSuccess(service -> {
                service.get(myBucket, myKey).onFailure(aContext::fail).onSuccess(s3Obj -> {
                    aContext.assertEquals(myExpectedSize, s3Obj.getSize());
                    complete(asyncTask);
                });
            }).onFailure(aContext::fail);
        });
    }

    /**
     * Tests creating a service from AWS credentials and PUTing a JsonObject.
     *
     * @param aContext A test context
     */
    @Test
    public final void testGetProxyWithOptsPutBuffer(final TestContext aContext) {
        final Async asyncTask = aContext.async();
        final Vertx vertx = myContext.vertx();

        S3ClientService.getProxyWithOpts(vertx, getConfig()).onFailure(aContext::fail).onSuccess(service -> {
            final Buffer buffer = new JsonObject().put(TestConstants.ID, myKey).toBuffer();

            service.put(myBucket, new S3Object(myKey, buffer)).onFailure(aContext::fail).onSuccess(result -> {
                aContext.assertTrue(myAwsS3Client.doesObjectExist(myBucket, myKey));

                // Write a temporary file from the S3 object and check that its size is the same as ours
                writeFile(myAwsS3Client.getObject(myBucket, myKey).getObjectContent()).onSuccess(path -> {
                    aContext.assertEquals(45L, vertx.fileSystem().propsBlocking(path).size());
                    complete(asyncTask);
                }).onFailure(aContext::fail);
            });
        });
    }

    /**
     * Tests creating a service from AWS credentials and PUTing a file.
     *
     * @param aContext A test context
     */
    @Test
    public final void testGetProxyWithOptsPutFile(final TestContext aContext) throws IOException {
        final Async asyncTask = aContext.async();
        final Vertx vertx = myContext.vertx();

        S3ClientService.getProxyWithOpts(vertx, getConfig()).onFailure(aContext::fail).onSuccess(service -> {
            service.put(myBucket, new S3Object(myKey, TEST_FILE)).onFailure(aContext::fail).onSuccess(result -> {
                aContext.assertTrue(myAwsS3Client.doesObjectExist(myBucket, myKey));

                // Write a temporary file from the S3 object and check that its size is the same as ours
                writeFile(myAwsS3Client.getObject(myBucket, myKey).getObjectContent()).onSuccess(path -> {
                    aContext.assertEquals(myExpectedSize, vertx.fileSystem().propsBlocking(path).size());
                    complete(asyncTask);
                }).onFailure(aContext::fail);
            });
        });
    }

    /**
     * Puts the test file into the test bucket.
     *
     * @return A future with the result of the PUT
     */
    private Future<Void> storeTestFile() {
        final Promise<Void> promise = Promise.promise();

        try {
            myAwsS3Client.putObject(myBucket, myKey, new File(TEST_FILE));

            if (myAwsS3Client.doesObjectExist(myBucket, myKey)) {
                promise.complete();
            } else {
                promise.fail(LOGGER.getMessage(MessageCodes.VSS_029, myKey, myBucket));
            }
        } catch (final SdkClientException details) {
            promise.fail(details);
        }

        return promise.future();
    }

    /**
     * Write a file to a system path and return that path.
     *
     * @param aInputStream An InputStream with data to write to the file
     * @return A file system path for the newly created file
     */
    private Future<String> writeFile(final InputStream aInputStream) {
        final Promise<String> promise = Promise.promise();

        try {
            final FileSystem fileSystem = myContext.vertx().fileSystem();
            final Path path = TestUtils.getTestFile(".gif");

            Files.copy(aInputStream, path, StandardCopyOption.REPLACE_EXISTING);

            fileSystem.exists(path.toString()).onComplete(exists -> {
                if (exists.succeeded()) {
                    promise.complete(path.toString());
                } else {
                    promise.fail(exists.cause());
                }
            });
        } catch (final IOException details) {
            promise.fail(details);
        }

        return promise.future();
    }
}
