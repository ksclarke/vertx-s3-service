
package info.freelibrary.vertx.s3;

import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;

/**
 * Tests of S3Client that don't need an actual S3 connection.
 */
public class S3ClientTest {

    private static final Vertx VERTX = Vertx.vertx();

    /**
     * Tests the simplest constructor.
     */
    @Test
    public final void testS3ClientVertx() {
        new S3Client(VERTX).close();
    }

    /**
     * Tests constructor that takes {@link HttpClientOptions}.
     */
    @Test
    public final void testS3ClientVertxHttpClientOptions() {
        new S3Client(VERTX, new S3ClientOptions()).close();
    }

    /**
     * Tests constructor that takes an S3 endpoint.
     */
    @Test
    public final void testS3ClientVertxString() {
        new S3Client(VERTX, new S3ClientOptions().setEndpoint(S3Client.DEFAULT_ENDPOINT)).close();
    }
}
