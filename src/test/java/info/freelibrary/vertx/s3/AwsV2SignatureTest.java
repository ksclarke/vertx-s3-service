
package info.freelibrary.vertx.s3;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;

/**
 * Tests of the AwsV2Signature.
 */
public class AwsV2SignatureTest {

    private static final String TEST_CONTENT_TYPE = "text/csv";

    private String myBucket;

    private String myKey;

    private String myAccessKey;

    private String mySecretKey;

    private String mySessionToken;

    @Before
    public void setUp() {
        myBucket = UUID.randomUUID().toString();
        myKey = UUID.randomUUID().toString();
        myAccessKey = UUID.randomUUID().toString();
        mySecretKey = UUID.randomUUID().toString();
        mySessionToken = UUID.randomUUID().toString();
    }

    @Test
    public final void testGetAuthorizationSessionToken() {
        final AwsCredentials credentials = new AwsCredentials(myAccessKey, mySecretKey, mySessionToken);
        final AwsV2Signature signature = new AwsV2Signature(credentials);
        final MultiMap headers = MultiMap.caseInsensitiveMultiMap();

        signature.getAuthorization(headers, "GET", myBucket, myKey, new byte[] {});
        assertTrue(headers.contains("X-Amz-Security-Token"));
    }

    @Test
    public final void testGetAuthorization() {
        final AwsCredentials credentials = new AwsCredentials(myAccessKey, mySecretKey, mySessionToken);
        final AwsV2Signature signature1 = new AwsV2Signature(credentials);
        final AwsV2Signature signature2 = new AwsV2Signature(credentials);
        final String headerName = HttpHeaders.CONTENT_TYPE.toString();
        final MultiMap headers1 = MultiMap.caseInsensitiveMultiMap().add(headerName, TEST_CONTENT_TYPE);
        final MultiMap headers2 = MultiMap.caseInsensitiveMultiMap();
        final String method = "LIST";
        final String auth1 = signature1.getAuthorization(headers1, method, myBucket, myKey, new byte[] {});
        final String auth2 = signature2.getAuthorization(headers2, method, myBucket, myKey, new byte[] {});

        assertNotEquals(auth1, auth2);
    }
}
