
package info.freelibrary.vertx.s3;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.UUID;

import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;

/**
 * Tests of S3Client that don't need an actual S3 connection.
 */
public class S3ClientTest {

    private static final Vertx VERTX = Vertx.vertx();

    private final HttpClientOptions myClientOptions = new HttpClientOptions();

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
        new S3Client(VERTX, new HttpClientOptions()).close();
    }

    /**
     * Tests constructor that takes an S3 endpoint.
     */
    @Test
    public final void testS3ClientVertxString() {
        new S3Client(VERTX, S3Client.DEFAULT_ENDPOINT).close();
    }

    /**
     * Tests constructor that takes S3 access and secret keys.
     */
    @Test
    public final void testS3ClientVertxStringString() {
        new S3Client(VERTX, getUUID(), getUUID()).close();
    }

    /**
     * Tests constructor that takes an S3 access key, secret key, and {@link HttpClientOptions}.
     */
    @Test
    public final void testS3ClientVertxStringStringHttpClientOptions() {
        new S3Client(VERTX, getUUID(), getUUID(), myClientOptions).close();
    }

    /**
     * Tests constructor that takes an S3 access key, secret key, and S3 endpoint.
     */
    @Test
    public final void testS3ClientVertxStringStringString() throws MalformedURLException {
        new S3Client(VERTX, getUUID(), getUUID(), S3Client.DEFAULT_ENDPOINT).close();
    }

    /**
     * Tests constructor that takes Vert.x instance, access key, secret key, session key, and
     * {@link HttpClientOptions}.
     */
    @Test
    public final void testS3ClientVertxStringStringStringHttpClientOptions() {
        new S3Client(VERTX, getUUID(), getUUID(), getUUID(), myClientOptions).close();
    }

    /**
     * Tests constructor that takes Vert.x instance, access key, secret key, session key, and S3 endpoint.
     */
    @Test
    public final void testS3ClientVertxStringStringStringString() throws MalformedURLException {
        new S3Client(VERTX, getUUID(), getUUID(), getUUID(), S3Client.DEFAULT_ENDPOINT).close();
    }

    /**
     * Tests constructor that takes {@link AwsCredentials} and an {@link HttpClient}.
     */
    @Test
    public final void testS3ClientAwsCredentialsHttpClient() {
        new S3Client(new AwsCredentials(getUUID(), getUUID()), VERTX.createHttpClient()).close();
    }

    /**
     * Tests setting the use of a version two signature.
     */
    @Test
    public final void testUseV2SignatureTrue() {
        assertTrue(new S3Client(VERTX).useV2Signature(true).usesV2Signature());
    }

    /**
     * Tests that the default signature that's used is a version four signature.
     */
    @Test
    public final void testUseV2SignatureDefaultFalse() {
        assertFalse(new S3Client(VERTX).usesV2Signature());
    }

    /**
     * Tests setting the version two signature use to false.
     */
    @Test
    public final void testUseV2SignatureFalse() {
        assertFalse(new S3Client(VERTX).useV2Signature(false).usesV2Signature());
    }

    /**
     * Gets a string to use for access key, secret key, and session key.
     *
     * @return A UUID
     */
    private String getUUID() {
        return UUID.randomUUID().toString();
    }
}
