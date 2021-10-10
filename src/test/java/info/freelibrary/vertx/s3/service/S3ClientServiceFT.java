
package info.freelibrary.vertx.s3.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.vertx.s3.AbstractS3FT;
import info.freelibrary.vertx.s3.S3Object;
import info.freelibrary.vertx.s3.TestConstants;
import info.freelibrary.vertx.s3.TestUtils;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
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

    private static final String TEST_FILE = "src/test/resources/green.gif";

    /**
     * Tests creating a service from AWS credentials and PUTing a JsonObject.
     *
     * @param aContext A test context
     */
    @Test
    public final void testCreateWithOptsPutBuffer(final TestContext aContext) {
        final Async asyncTask = aContext.async();
        final Vertx vertx = myContext.vertx();

        S3ClientService.createWithOpts(vertx, getConfig()).onSuccess(service -> {
            final JsonObject jsonObj = new JsonObject().put(TestConstants.ID, myKey);

            service.put(myBucket, new S3Object(myKey, jsonObj.toBuffer())).onComplete(put -> {
                final FileSystem fileSystem = vertx.fileSystem();
                final InputStream inStream;

                if (put.succeeded()) {
                    aContext.assertTrue(myAwsS3Client.doesObjectExist(myBucket, myKey));
                    inStream = myAwsS3Client.getObject(myBucket, myKey).getObjectContent().getDelegateStream();

                    // Write a temporary file from the S3 object and check that its size is the same as ours
                    writeFile(inStream).onSuccess(path -> {
                        final long foundSize = fileSystem.fsPropsBlocking(path).totalSpace();
                        final long expectedSize = fileSystem.fsPropsBlocking(TEST_FILE).totalSpace();

                        aContext.assertEquals(expectedSize, foundSize);
                        complete(asyncTask);
                    }).onFailure(details -> aContext.fail(details));
                } else {
                    aContext.fail(put.cause());
                }
            });
        }).onFailure(details -> aContext.fail(details));
    }

    /**
     * Tests creating a service from AWS credentials and PUTing a file.
     *
     * @param aContext A test context
     */
    @Test
    public final void testCreateWithOptsPutFile(final TestContext aContext) throws IOException {
        final Async asyncTask = aContext.async();
        final Vertx vertx = myContext.vertx();

        S3ClientService.createWithOpts(vertx, getConfig()).onSuccess(service -> {
            final Future<Void> put = service.put(myBucket, new S3Object(myKey, TEST_FILE));

            put.onFailure(details -> aContext.fail(details));
            put.onSuccess(test -> {
                final FileSystem fileSystem = vertx.fileSystem();
                final InputStream inStream;

                // Check that our uploaded file exists in the S3 bucket and then get its input stream
                aContext.assertTrue(myAwsS3Client.doesObjectExist(myBucket, myKey));
                inStream = myAwsS3Client.getObject(myBucket, myKey).getObjectContent().getDelegateStream();

                // Write a temporary file from the S3 object and check that its size is the same as ours
                writeFile(inStream).onSuccess(path -> {
                    final long foundSize = fileSystem.fsPropsBlocking(path).totalSpace();
                    final long expectedSize = fileSystem.fsPropsBlocking(TEST_FILE).totalSpace();

                    aContext.assertEquals(expectedSize, foundSize);
                    complete(asyncTask);
                }).onFailure(details -> aContext.fail(details));
            });
        }).onFailure(details -> aContext.fail(details));
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
