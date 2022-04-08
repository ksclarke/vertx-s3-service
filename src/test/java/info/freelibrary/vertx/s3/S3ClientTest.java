
package info.freelibrary.vertx.s3;

import java.util.UUID;

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
        final String username = UUID.randomUUID().toString();
        final String password = UUID.randomUUID().toString();

        new S3Client(VERTX, new S3ClientOptions().setCredentials(username, password)).close();
    }
}
