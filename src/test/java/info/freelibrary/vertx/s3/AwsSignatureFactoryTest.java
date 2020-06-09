
package info.freelibrary.vertx.s3;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Test;

import info.freelibrary.vertx.s3.AwsSignatureFactory.Version;

/**
 * A factory for creating AWS authentication signatures.
 */
public final class AwsSignatureFactoryTest {

    private static final String S3_ACCESS_KEY = "asdf";

    private static final String S3_SECRET_KEY = "fdsa";

    private static final URI DEFAULT_ENDPOINT = URI.create("s3.amazon.com");

    /**
     * Gets default signature.
     */
    @Test
    public void testGetDefaultSignature() {
        final AwsSignature signature = AwsSignatureFactory.getFactory().setHost(DEFAULT_ENDPOINT).setCredentials(
                new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY)).getSignature();

        assertEquals(AwsV4Signature.class.getName(), signature.getClass().getName());
    }

    /**
     * Gets v.4 signature.
     */
    @Test
    public void testGetV4Signature() {
        final AwsSignature signature = AwsSignatureFactory.getFactory(Version.V4).setHost(DEFAULT_ENDPOINT)
                .setCredentials(S3_ACCESS_KEY, S3_SECRET_KEY).getSignature();

        assertEquals(AwsV4Signature.class.getName(), signature.getClass().getName());
    }

}
