
package info.freelibrary.vertx.s3;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests of AWS credentials.
 */
public class AwsCredentialsTest {

    private static final String S3_ACCESS_KEY = "my-access-key";

    private static final String S3_SECRET_KEY = "my-secret-key";

    private static final String S3_SESSION_TOKEN = "my-session-token";

    private static final String NEW_SESSION_TOKEN = "new-session-token";

    /**
     * Tests constructing a basic AWS credentials object.
     */
    @Test
    public final void testBasicConstructor() {
        final AwsCredentials creds = new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY);

        assertEquals(S3_ACCESS_KEY, creds.getAccessKey());
        assertEquals(S3_SECRET_KEY, creds.getSecretKey());
    }

    /**
     * Tests constructing a basic AWS credentials object that includes a session token.
     */
    @Test
    public final void testSessionTokenConstructor() {
        final AwsCredentials creds = new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY, S3_SESSION_TOKEN);

        assertEquals(S3_ACCESS_KEY, creds.getAccessKey());
        assertEquals(S3_SECRET_KEY, creds.getSecretKey());
        assertEquals(S3_SESSION_TOKEN, creds.getSessionToken());
    }

    /**
     * Tests the validity of an AWS credentials object.
     */
    @Test
    public final void testIsValidBasic() {
        assertTrue(new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY).isValid());
    }

    /**
     * Tests the validity of an AWS credentials object that includes a session token.
     */
    @Test
    public final void testIsValidSessionToken() {
        assertTrue(new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY, S3_SESSION_TOKEN).isValid());
    }

    /**
     * Tests that an invalid AWS credentials object fails the validity test.
     */
    @Test
    public final void testIsValidNot() {
        assertFalse(new AwsCredentials(S3_ACCESS_KEY, null).isValid());
    }

    /**
     * Tests checking whether an AWS credentials object contains a session token.
     */
    @Test
    public final void testHasSessionToken() {
        assertTrue(new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY, S3_SESSION_TOKEN).hasSessionToken());
    }

    /**
     * Tests that an AWS credentials object doesn't say it has a session token when it doesn't.
     */
    @Test
    public final void testHasSessionTokenNot() {
        assertFalse(new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY).hasSessionToken());
    }

    /**
     * Tests setting a session token on an AWS credentials object.
     */
    @Test
    public final void testSetSessionToken() {
        final AwsCredentials creds = new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY, S3_SESSION_TOKEN);

        assertEquals(NEW_SESSION_TOKEN, creds.setSessionToken(NEW_SESSION_TOKEN).getSessionToken());
    }
}
