
package info.freelibrary.vertx.s3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.util.HTTP;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests the S3ClientRequest.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientRequestIT {

    private String myAccessKey;

    private String mySecretKey;

    private String myURI;

    private Vertx myVertx;

    /**
     * Sets up the testing environment.
     */
    @Before
    public void setUp() {
        myURI = String.join(HTTP.Syntax.SLASH, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        myAccessKey = UUID.randomUUID().toString();
        mySecretKey = UUID.randomUUID().toString();
        myVertx = Vertx.vertx();
    }

    /**
     * Tests the anonymous constructor.
     *
     * @param aContext A test context
     */
    @Test
    public final void testConstructorAnon(final TestContext aContext) {
        final Future<HttpClientRequest> futureRequest = myVertx.createHttpClient().request(HttpMethod.DELETE, myURI);
        final S3ClientRequest request = new S3ClientRequest(futureRequest.result());

        assertTrue(request.getCredentials().isEmpty());
    }

    /**
     * Tests the credentials constructor.
     *
     * @param aContext A test context
     */
    @Test
    public final void testConstructorWithCreds(final TestContext aContext) {
        final AwsCredentials awsCredentials = new AwsCredentials(myAccessKey, mySecretKey);
        final Future<HttpClientRequest> futureRequest = myVertx.createHttpClient().request(HttpMethod.DELETE, myURI);
        final S3ClientRequest request = new S3ClientRequest(futureRequest.result(), awsCredentials);

        assertTrue(request.getCredentials().isPresent());
    }

    /**
     * Tests the S3ClientRequestIT's method method.
     *
     * @param aContext A test context
     */
    @Test
    public final void testGetMethod(final TestContext aContext) {
        final Future<HttpClientRequest> futureRequest = myVertx.createHttpClient().request(HttpMethod.DELETE, myURI);
        final S3ClientRequest request = new S3ClientRequest(futureRequest.result());

        assertEquals(HttpMethod.DELETE, request.getMethod());
    }

}
