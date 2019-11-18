
package info.freelibrary.vertx.s3;

import static info.freelibrary.vertx.s3.Constants.PATH_SEP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class S3ClientRequestTest {

    private static final String METHOD = "DELETE";

    private String myAccessKey;

    private String mySecretKey;

    private String mySessionToken;

    private String myKey;

    private String myBucket;

    private Vertx myVertx;

    @Before
    public void setUp() {
        myAccessKey = UUID.randomUUID().toString();
        mySecretKey = UUID.randomUUID().toString();
        mySessionToken = UUID.randomUUID().toString();
        myKey = UUID.randomUUID().toString();
        myBucket = UUID.randomUUID().toString();
        myVertx = Vertx.vertx();
    }

    @Test
    public final void testConstructorAnon(final TestContext aContext) {
        final HttpClientRequest httpRequest = myVertx.createHttpClient().delete(myBucket + PATH_SEP + myKey);
        final S3ClientRequest request = new S3ClientRequest(METHOD, myBucket, myKey, httpRequest);

        assertTrue(request.getCredentials().isEmpty());
    }

    @Test
    public final void testConstructorWithCreds() {
        final HttpClientRequest httpRequest = myVertx.createHttpClient().delete(myBucket + PATH_SEP + myKey);
        final S3ClientRequest request = new S3ClientRequest(METHOD, myBucket, myKey, httpRequest, myAccessKey,
                mySecretKey, mySessionToken);

        assertTrue(request.getCredentials().isPresent());
    }

    @Test
    public final void testConstructorWithCredsEmpty() {
        final HttpClientRequest httpRequest = myVertx.createHttpClient().delete(myBucket + PATH_SEP + myKey);
        final S3ClientRequest request = new S3ClientRequest(METHOD, myBucket, myKey, httpRequest, myAccessKey, null,
                mySessionToken);

        assertTrue(request.getCredentials().isEmpty());
    }

    @Test
    public final void testGetMethod() {
        final HttpClientRequest httpRequest = myVertx.createHttpClient().delete(myBucket + PATH_SEP + myKey);
        final S3ClientRequest request = new S3ClientRequest(METHOD, myBucket, myKey, httpRequest, myAccessKey, null,
                mySessionToken);

        assertEquals(METHOD, request.getMethod());
    }

}
