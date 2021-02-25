
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

import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class S3ClientServiceProxyImplFT extends AbstractS3FT {

    private static final String ADDRESS = "s3.service";

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

    @Test
    public final void testClose() {

    }

    @Test
    public final void testS3ClientServiceProxyImplVertxString(final TestContext aContext) {
        final S3ClientService service = new S3ClientServiceProxyImpl(myTestContext.vertx(), ADDRESS);
        final Async asyncTask = aContext.async();

        service.putJSON(myBucket, myKey, new JsonObject().put("asdf", "aaaa"), put -> {
            if (put.succeeded()) {
                try (S3Object s3Obj = myS3Client.getObject(myBucket, myKey)) {
                    aContext.assertEquals(myBucket, s3Obj.getBucketName());
                    aContext.assertEquals(myKey, s3Obj.getKey());
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

    @Test
    public final void testS3ClientServiceProxyImplVertxString2(final TestContext aContext) {
        final S3ClientService service = new S3ClientServiceProxyImpl(myTestContext.vertx(), ADDRESS);
        final Async asyncTask = aContext.async();

        final EventBus eventBus = myTestContext.vertx().eventBus();

        final Future<Message<JsonObject>> future =
                eventBus.request("s3-service",
                        new JsonObject().put("bucket", myBucket).put("key", myKey).put("Document",
                                new JsonObject().put("zz", "11")),
                        new DeliveryOptions().addHeader("action", "putJSON"));

        future.onComplete(put -> {
            asyncTask.complete();
        });
    }

    @Test
    public final void testS3ClientServiceProxyImplVertxAwsProfileString() {

    }

    @Test
    public final void testS3ClientServiceProxyImplVertxAwsCredentialsString() {

    }

    @Test
    public final void testS3ClientServiceProxyImplVertxAwsCredentialsS3ClientOptionsString() {

    }

}
