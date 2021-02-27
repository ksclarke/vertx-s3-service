
package info.freelibrary.vertx.s3.service;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.vertx.s3.AbstractS3FT;
import info.freelibrary.vertx.s3.AwsCredentials;
import info.freelibrary.vertx.s3.S3ClientOptions;
import info.freelibrary.vertx.s3.TestConstants;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests of the S3 client service.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientServiceFT extends AbstractS3FT {

    /**
     * Rule that creates the test context.
     */
    @Rule
    public RunTestOnContext myTestContext = new RunTestOnContext();

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
     * Tests creating a service from supplied AWS credentials.
     *
     * @param aContext A test context
     */
    @Test
    @Ignore
    public final void testCreateFromCreds(final TestContext aContext) {
        final AwsCredentials creds = new AwsCredentials(myAccessKey, mySecretKey);
        final S3ClientOptions config = new S3ClientOptions().setEndpoint(myEndpoint);
        final S3ClientService service = S3ClientService.createCustom(myTestContext.vertx(), creds, config);
        final Async asyncTask = aContext.async();

        service.putJSON(myBucket, myKey, new JsonObject().put(TestConstants.ID, myKey), put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myS3Client.doesObjectExist(myBucket, myKey));
                complete(asyncTask);
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Tests creating a service from AWS credentials.
     *
     * @param aContext A test context
     */
    @Test
    @Ignore
    public final void testCreateProxyFromCreds(final TestContext aContext) {
        final AwsCredentials creds = new AwsCredentials(myAccessKey, mySecretKey);
        final S3ClientOptions config = new S3ClientOptions().setEndpoint(myEndpoint);
        final S3ClientService service = S3ClientService.createCustomProxy(myTestContext.vertx(), creds, config, "s3");
        final Async asyncTask = aContext.async();

        service.putJSON(myBucket, myKey, new JsonObject().put(TestConstants.ID, myKey), put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myS3Client.doesObjectExist(myBucket, myKey));
                complete(asyncTask);
            } else {
                aContext.fail(put.cause());
            }
        });
    }

    /**
     * Tests putting a JSON document to the S3 service.
     */
    @Test
    @Ignore
    public final void testPutJSON() {

    }

}
