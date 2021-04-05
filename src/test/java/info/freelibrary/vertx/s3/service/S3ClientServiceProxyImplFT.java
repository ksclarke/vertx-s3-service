
package info.freelibrary.vertx.s3.service;

import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.amazonaws.services.s3.model.S3Object;

import info.freelibrary.vertx.s3.AbstractS3FT;
import info.freelibrary.vertx.s3.AwsCredentials;
import info.freelibrary.vertx.s3.S3ClientOptions;
import info.freelibrary.vertx.s3.S3ObjectData;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests usage of the S3 client service proxy.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientServiceProxyImplFT extends AbstractS3FT {

    private static final String ADDRESS = "s3.service";

    /**
     * Rule that creates the test context.
     */
    @Rule
    public RunTestOnContext myContext = new RunTestOnContext();

    private String myBucket;

    private String myKey;

    /**
     * Sets up the testing environment.
     */
    @Before
    public void setUpTests() {
        myKey = UUID.randomUUID().toString();
        myBucket = UUID.randomUUID().toString();
        myS3Client.createBucket(myBucket);
    }

    /**
     * Tears down the testing environment.
     */
    @After
    public void tearDownTests() {
        myS3Client.deleteObject(myBucket, myKey);
        myS3Client.deleteBucket(myBucket);
    }

    /**
     * Tests closing the service proxy.
     */
    @Test
    public final void testClose() {

    }

    /**
     * Tests constructing a S3 client service proxy from the supplied address.
     *
     * @param aContext A test context
     */
    @Test
    public final void testS3ClientServiceProxyImplVertxString(final TestContext aContext) {
        final AwsCredentials awsCreds = new AwsCredentials(myAccessKey, mySecretKey);
        final S3ClientOptions config = new S3ClientOptions(myEndpoint).setCredentials(awsCreds);
        final S3ClientService service = new S3ClientServiceProxyImpl(myContext.vertx(), config, ADDRESS);
        final JsonObject json = new JsonObject().put("asdf", "aaaa");
        final Async asyncTask = aContext.async();

        service.put(myBucket, myKey, new S3ObjectData(json.toBuffer()), put -> {
            if (put.succeeded()) {
                try (S3Object s3Obj = myS3Client.getObject(myBucket, myKey)) {
                    aContext.assertEquals(myBucket, s3Obj.getBucketName());
                    aContext.assertEquals(myKey, s3Obj.getKey());
                    s3Obj.getObjectContent().abort();
                    s3Obj.close();
                } catch (final IOException details) {
                    aContext.fail(details);
                } finally {
                    complete(asyncTask);
                }
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Tests constructing an S3 client service proxy with a profile and an address.
     */
    @Test
    public final void testS3ClientServiceProxyImplVertxAwsProfileString() {

    }

    /**
     * Tests constructing an S3 client service proxy with credentials and an address.
     */
    @Test
    public final void testS3ClientServiceProxyImplVertxAwsCredentialsString() {

    }

    /**
     * Tests constructing an S3 client service proxy with credentials, client options, and an address.
     */
    @Test
    public final void testS3ClientServiceProxyImplVertxAwsCredentialsS3ClientOptionsString() {

    }

}
