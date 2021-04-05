
package info.freelibrary.vertx.s3;

import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import info.freelibrary.vertx.s3.util.MessageCodes;

import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests of examples that are used in the project's documentation.
 */
@RunWith(VertxUnitRunner.class)
@SuppressWarnings("MultipleStringLiterals") // There are code snippets from the documentation
public class ExamplesIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExamplesIT.class, MessageCodes.BUNDLE);

    /**
     * Rule that creates the test context.
     */
    @Rule
    public RunTestOnContext myContext = new RunTestOnContext();

    /**
     * Test the first example from the documentation.
     *
     * @param aContext A test context
     */
    @Test
    public void testExample0(final TestContext aContext) {
        final Async asyncTask = aContext.async(); // Remove this from the documentation
        final S3Client s3Client = new S3Client(myContext.vertx(), new S3ClientOptions().setProfile("vertx-s3"));
        final FileSystem fileSystem = myContext.vertx().fileSystem();
        final OpenOptions opts = new OpenOptions();

        fileSystem.createTempFile("ucla-library-logo", ".png").compose(path -> fileSystem.open(path, opts))
            .compose(file -> s3Client.get("presentation-materials", "ucla-library-logo.png")
                .compose(response -> response.pipeTo(file)))
            .onComplete(download -> {
                if (download.succeeded()) {
                    System.out.println("DONE1!");
                    asyncTask.complete();
                } else {
                    aContext.fail(download.cause());
                }
            });
    }

    /**
     * Test the second example from the documentation.
     *
     * @param aContext A test context
     */
    @Test
    public void testExample1(final TestContext aContext) {
        final Async asyncTask = aContext.async(); // Remove this from the documentation
        final S3Client s3Client = new S3Client(vertx(), new S3ClientOptions("vertx-s3"));
        final String fileName = "ucla-library-logo.png";

        s3Client.get("presentation-materials", fileName, get -> {
            if (get.succeeded()) {
                final String path = Paths.get("/tmp", fileName).toString();
                final OpenOptions opts = new OpenOptions();

                vertx().fileSystem().open(path, opts).compose(file -> get.result().pipeTo(file).onSuccess(result -> {
                    LOGGER.info("Successfully downloaded S3 object to: {}", path);
                    asyncTask.complete(); // remove for examples documentation
                }).onFailure(error -> {
                    LOGGER.error(error, error.getMessage());
                    aContext.fail(error); // remove for examples documentation
                }));
            } else {
                LOGGER.error(get.cause(), get.cause().getMessage());
                aContext.fail(get.cause()); // remove for examples documentation
            }
        });
    }

    /**
     * Test the third example from the documentation.
     *
     * @param aContext A test context
     */
    @Test
    public void testExampleWithFutures(final TestContext aContext) {
        final Async asyncTask = aContext.async(); // Remove this from the documentation
        final S3Client s3Client = new S3Client(vertx(), new S3ClientOptions("vertx-s3"));
        final String fileName = "ucla-library-logo.png";
        final String path = Paths.get(System.getProperty("java.io.tmpdir"), fileName).toString();

        // Downloading an S3 object using only futures...
        s3Client.get("presentation-materials", fileName).compose(response -> vertx().fileSystem()
            .open(path, new OpenOptions()).compose(file -> response.pipeTo(file).onSuccess(result -> {
                LOGGER.info("Successfully downloaded S3 object to: {}", path);
                asyncTask.complete(); // remove for examples documentation
            }).onFailure(error -> {
                LOGGER.error(error, error.getMessage());
                aContext.fail(error); // remove for examples documentation
            })));
    }

    /**
     * A convenience method to make the test code look more like it would in a non-test environment.
     *
     * @return A Vert.x instance
     */
    private Vertx vertx() {
        return myContext.vertx();
    }
}
