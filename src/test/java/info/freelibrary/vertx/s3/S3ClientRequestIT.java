
package info.freelibrary.vertx.s3;

import static info.freelibrary.vertx.s3.Constants.PATH_SEP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    private static final HttpMethod METHOD = HttpMethod.DELETE;

    private String myAccessKey;

    private String mySecretKey;

    private String myKey;

    private String myBucket;

    private Vertx myVertx;

    /**
     * Sets up the testing environment.
     */
    @Before
    public void setUp() {
        myAccessKey = UUID.randomUUID().toString();
        mySecretKey = UUID.randomUUID().toString();
        myKey = UUID.randomUUID().toString();
        myBucket = UUID.randomUUID().toString();
        myVertx = Vertx.vertx();
    }

    /**
     * Tests the anonymous constructor.
     *
     * @param aContext A test context
     */
    @Test
    public final void testConstructorAnon(final TestContext aContext) {
        final String requestURI = myBucket + PATH_SEP + myKey;
        final Future<HttpClientRequest> futureRequest = myVertx.createHttpClient().request(METHOD, requestURI);
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
        final String requestURI = myBucket + PATH_SEP + myKey;
        final AwsCredentials awsCredentials = new AwsCredentials(myAccessKey, mySecretKey);
        final Future<HttpClientRequest> futureRequest = myVertx.createHttpClient().request(METHOD, requestURI);
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
        final String requestURI = myBucket + PATH_SEP + myKey;
        final Future<HttpClientRequest> futureRequest = myVertx.createHttpClient().request(METHOD, requestURI);
        final S3ClientRequest request = new S3ClientRequest(futureRequest.result());

        assertEquals(METHOD, request.getMethod());
    }

}
