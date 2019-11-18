
package info.freelibrary.vertx.s3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AwsCredentialsTest {

    private static final String S3_ACCESS_KEY = "my-access-key";

    private static final String S3_SECRET_KEY = "my-secret-key";

    private static final String S3_SESSION_TOKEN = "my-session-token";

    private static final String NEW_SESSION_TOKEN = "new-session-token";

    @Test
    public final void testBasicConstructor() {
        final AwsCredentials creds = new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY);

        assertEquals(S3_ACCESS_KEY, creds.getAccessKey());
        assertEquals(S3_SECRET_KEY, creds.getSecretKey());
    }

    @Test
    public final void testSessionTokenConstructor() {
        final AwsCredentials creds = new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY, S3_SESSION_TOKEN);

        assertEquals(S3_ACCESS_KEY, creds.getAccessKey());
        assertEquals(S3_SECRET_KEY, creds.getSecretKey());
        assertEquals(S3_SESSION_TOKEN, creds.getSessionToken());
    }

    @Test
    public final void testIsValidBasic() {
        assertTrue(new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY).isValid());
    }

    @Test
    public final void testIsValidSessionToken() {
        assertTrue(new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY, S3_SESSION_TOKEN).isValid());
    }

    @Test
    public final void testIsValidNot() {
        assertFalse(new AwsCredentials(S3_ACCESS_KEY, null).isValid());
    }

    @Test
    public final void testHasSessionToken() {
        assertTrue(new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY, S3_SESSION_TOKEN).hasSessionToken());
    }

    @Test
    public final void testHasSessionTokenNot() {
        assertFalse(new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY).hasSessionToken());
    }

    @Test
    public final void testSetSessionToken() {
        final AwsCredentials creds = new AwsCredentials(S3_ACCESS_KEY, S3_SECRET_KEY, S3_SESSION_TOKEN);

        assertEquals(NEW_SESSION_TOKEN, creds.setSessionToken(NEW_SESSION_TOKEN).getSessionToken());
    }
}
