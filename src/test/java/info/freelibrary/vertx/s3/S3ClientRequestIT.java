
package info.freelibrary.vertx.s3;

import static info.freelibrary.util.Constants.SLASH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests the S3ClientRequest.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientRequestIT extends AbstractS3IT {

    /**
     * A test rule to run the tests on the Vert.x context.
     */
    @Rule
    public final RunTestOnContext myContext = new RunTestOnContext();

    /**
     * A bucket and S3 object key URI path.
     */
    private String myURI;

    /**
     * Sets up the testing environment.
     */
    @Before
    public void setUp() {
        myURI = String.join(SLASH, UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    /**
     * Tests the anonymous constructor.
     *
     * @param aContext A test context
     */
    @Test
    public final void testConstructorAnon(final TestContext aContext) {
        final String host = S3Endpoint.US_EAST_1.getHost();
        final Async asyncTask = aContext.async();

        myContext.vertx().createHttpClient().request(HttpMethod.DELETE, host, myURI).onComplete(request -> {
            if (request.succeeded()) {
                assertTrue(new S3ClientRequest(request.result()).getCredentials().isEmpty());
                complete(asyncTask);
            } else {
                aContext.fail(request.cause());
            }
        });
    }

    /**
     * Tests the credentials constructor.
     *
     * @param aContext A test context
     */
    @Test
    public final void testConstructorWithCreds(final TestContext aContext) {
        final AwsCredentials creds = new AwsCredentials(TestUtils.AWS_ACCESS_KEY, TestUtils.AWS_SECRET_KEY);
        final String host = S3Endpoint.US_EAST_1.getHost();
        final Async asyncTask = aContext.async();

        myContext.vertx().createHttpClient().request(HttpMethod.DELETE, host, myURI).onComplete(request -> {
            if (request.succeeded()) {
                assertTrue(new S3ClientRequest(request.result(), creds).getCredentials().isPresent());
                complete(asyncTask);
            } else {
                aContext.fail(request.cause());
            }
        });
    }

    /**
     * Tests the S3ClientRequestIT's method method.
     *
     * @param aContext A test context
     */
    @Test
    public final void testGetMethod(final TestContext aContext) {
        final String host = S3Endpoint.US_EAST_1.getHost();
        final Async asyncTask = aContext.async();

        myContext.vertx().createHttpClient().request(HttpMethod.DELETE, host, myURI).onComplete(request -> {
            if (request.succeeded()) {
                assertEquals(HttpMethod.DELETE, new S3ClientRequest(request.result()).getMethod());
                complete(asyncTask);
            } else {
                aContext.fail(request.cause());
            }
        });
    }
}
