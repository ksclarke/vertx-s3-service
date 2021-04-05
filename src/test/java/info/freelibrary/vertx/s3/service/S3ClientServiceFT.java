
package info.freelibrary.vertx.s3.service;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.vertx.s3.AbstractS3FT;
import info.freelibrary.vertx.s3.AwsCredentials;
import info.freelibrary.vertx.s3.S3ClientOptions;
import info.freelibrary.vertx.s3.S3ObjectData;
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
     * Tests creating a service from supplied AWS credentials.
     *
     * @param aContext A test context
     */
    @Test
    public final void testCreateWithOptions(final TestContext aContext) {
        final AwsCredentials creds = new AwsCredentials(myAccessKey, mySecretKey);
        final S3ClientOptions config = new S3ClientOptions(myEndpoint).setCredentials(creds);
        final S3ClientService service = S3ClientService.createWithOptions(myContext.vertx(), config);
        final JsonObject json = new JsonObject().put(TestConstants.ID, myKey);
        final Async asyncTask = aContext.async();

        service.put(myBucket, myKey, new S3ObjectData(json.toBuffer()), put -> {
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
    public final void testCreateProxyWithOptions(final TestContext aContext) {
        final AwsCredentials credentials = new AwsCredentials(myAccessKey, mySecretKey);
        final S3ClientOptions config = new S3ClientOptions(myEndpoint).setCredentials(credentials);
        final S3ClientService service = S3ClientService.createProxyWithOptions(myContext.vertx(), config, "s3");
        final JsonObject json = new JsonObject().put(TestConstants.ID, myKey);
        final Async asyncTask = aContext.async();

        service.put(myBucket, myKey, new S3ObjectData(json.toBuffer()), put -> {
            if (put.succeeded()) {
                aContext.assertTrue(myS3Client.doesObjectExist(myBucket, myKey));
                complete(asyncTask);
            } else {
                aContext.fail(put.cause());
            }
        });
    }

}
