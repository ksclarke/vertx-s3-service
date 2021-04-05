
package info.freelibrary.vertx.s3;

import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.vertx.s3.util.MessageCodes;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import uk.co.lucasweb.aws.v4.signer.SigningException;

/**
 * These tests run in the container where we've set up AWS credentials to be tested.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientProfilesTest {

    private static final Vertx VERTX = Vertx.vertx();

    private static final String PROFILE = "vertx-s3";

    private static final String MISSING = "missing";

    /**
     * Tests the simple profile constructor.
     *
     * @param aContext A test context
     */
    @Test
    public final void testS3ClientVertxProfile(final TestContext aContext) {
        new S3Client(VERTX, new S3ClientOptions().setProfile(PROFILE)).close();
    }

    /**
     * Test the profile + S3 endpoint constructor.
     *
     * @param aContext A test context
     */
    @Test
    public final void testS3ClientVertxProfilePlusEndpoint(final TestContext aContext) {
        final S3ClientOptions config = new S3ClientOptions(S3Endpoint.US_EAST_1).setProfile(new AwsProfile(PROFILE));
        new S3Client(VERTX, config).close();
    }

    /**
     * Tests that a non-existent profile throws a signing exception.
     *
     * @param aContext A test context
     */
    @Test(expected = SigningException.class)
    public final void testS3ClientVertxProfileMissingProfile(final TestContext aContext) {
        new S3Client(VERTX, new S3ClientOptions().setProfile(MISSING)).close();
        aContext.fail(MessageCodes.VSS_014);
    }

    /**
     * Tests that a non-existent profile throws a signing exception.
     *
     * @param aContext A test context
     */
    @Test(expected = SigningException.class)
    public final void testS3ClientVertxProfileHttpClientOptionsMissingProfile(final TestContext aContext) {
        new S3Client(VERTX, new S3ClientOptions().setProfile(new AwsProfile(MISSING))).close();
        aContext.fail(MessageCodes.VSS_014);
    }

    /**
     * Tests that a non-existent profile throws a signing exception.
     *
     * @param aContext A test context
     */
    @Test(expected = SigningException.class)
    public final void testS3ClientVertxProfilePlusEndpointMissingProfile(final TestContext aContext) {
        new S3Client(VERTX, new S3ClientOptions(S3Endpoint.US_EAST_1).setProfile(new AwsProfile(MISSING))).close();
        aContext.fail(MessageCodes.VSS_014);
    }
}
