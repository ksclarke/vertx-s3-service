
package info.freelibrary.vertx.s3;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * A class to run the examples that we use in documentation (just to make sure they actually work).
 */
public final class ExamplesMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExamplesMain.class, Constants.BUNDLE_NAME);

    // This doesn't need to be public
    private ExamplesMain() {
    }

    /**
     * The example runner main method.
     *
     * @param args Any arguments passed to this class
     */
    @SuppressWarnings("checkstyle:uncommentedmain")
    public static void main(final String[] args) {
        example1();
    }

    /**
     * The first example does a simple GET.
     */
    @SuppressWarnings("checkstyle:indentation")
    public static void example1() {
        final Vertx vertx = Vertx.vertx();
        final S3Client s3Client = new S3Client(vertx, new Profile("vertx-s3"));
        final String fileName = "ucla-library-logo.png";
        final Future<File> future = Future.future();

        // Do something with the result of our S3 download
        future.setHandler(download -> {
            if (download.succeeded()) {
                LOGGER.info("Successfully downloaded: {}", download.result());
            } else {
                LOGGER.error("Download failed: {}", download.cause().getMessage());
            }
        });

        // Do our S3 download
        s3Client.get("presentation-materials", fileName, get -> {
            final int statusCode = get.statusCode();

            if (statusCode == HTTP.OK) {
                get.bodyHandler(body -> {
                    final Path path = Paths.get(System.getProperty("java.io.tmpdir"), fileName);

                    // Write our S3 file to our local file system
                    vertx.fileSystem().writeFile(path.toString(), body, write -> {
                        if (write.succeeded()) {
                            future.complete(path.toFile());
                        } else {
                            future.fail(write.cause());
                        }
                    });
                });
            } else {
                future.fail(LOGGER.getMessage("Unexpected status code: {} [{}]", statusCode, get.statusMessage()));
            }
        }, error -> {
            future.fail(error);
        });
    }
}
