
package info.freelibrary.vertx.s3;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests of examples that are used in the project's documentation.
 */
@RunWith(VertxUnitRunner.class)
public class ExamplesIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExamplesIT.class, Constants.BUNDLE_NAME);

    /**
     * A simple (somewhat contrived) GET example.
     *
     * @param aContext A test context
     */
    @Test
    @SuppressWarnings("checkstyle:indentation")
    public void testExample1(final TestContext aContext) {
        final Async asyncTask = aContext.async(); // Remove this from the docs
        final Vertx vertx = Vertx.vertx();
        final S3Client s3Client = new S3Client(vertx, new Profile("vertx-s3"));
        final String fileName = "ucla-library-logo.png";
        final Promise<File> promise = Promise.promise();

        // Do something with the result of our S3 download
        promise.future().onComplete(handler -> {
            if (handler.succeeded()) {
                LOGGER.info("Successfully downloaded: {}", handler.result());
            } else {
                LOGGER.error("Download failed: {}", handler.cause().getMessage());
            }

            asyncTask.complete(); // Remove this; it's just needed here because we're in a test
        });

        // Do our S3 download
        s3Client.get("presentation-materials", fileName, get -> {
            if (get.succeeded()) {
                final HttpClientResponse response = get.result();
                final int statusCode = response.statusCode();

                if (statusCode == HTTP.OK) {
                    response.bodyHandler(body -> {
                        final Path path = Paths.get(System.getProperty("java.io.tmpdir"), fileName);

                        // Write our S3 file to our local file system
                        vertx.fileSystem().writeFile(path.toString(), body, write -> {
                            if (write.succeeded()) {
                                promise.complete(path.toFile());
                            } else {
                                promise.fail(write.cause());
                            }
                        });
                    });
                } else {
                    promise.fail("Unexpected status code: " + statusCode + " [" + response.statusMessage() + "]");
                }
            } else {
                promise.fail(get.cause());
            }
        }, error -> {
            promise.fail(error);
        });
    }

}
