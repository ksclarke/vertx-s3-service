
package info.freelibrary.vertx.s3.service;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.vertx.s3.AbstractS3FT;
import info.freelibrary.vertx.s3.S3DataObject;
import info.freelibrary.vertx.s3.TestConstants;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests of the S3 client service.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientServiceFT extends AbstractS3FT {

    /**
     * Tests creating a service proxy.
     *
     * @param aContext A test context
     */
    @Test
    public final void testCreateProxy(final TestContext aContext) {
        final Async asyncTask = aContext.async();

        S3ClientService.create(myContext.vertx(), "s3").onComplete(result -> {
            asyncTask.complete();
        });
    }

    /**
     * Tests creating a service from AWS credentials.
     *
     * @param aContext A test context
     */
    @Test
    @Ignore
    public final void testCreateProxyWithOptions(final TestContext aContext) {
        S3ClientService.createWithOptions(myContext.vertx(), getConfig(), "s3") //
            .onSuccess(service -> {
                final JsonObject json = new JsonObject().put(TestConstants.ID, myKey);
                final Async asyncTask = aContext.async();

                service.put(myBucket, myKey, new S3DataObject(json.toBuffer())).onComplete(put -> {
                    if (put.succeeded()) {
                        aContext.assertTrue(myAwsS3Client.doesObjectExist(myBucket, myKey));
                        complete(asyncTask);
                    } else {
                        aContext.fail(put.cause());
                    }
                });
            }) //
            .onFailure(details -> aContext.fail(details));
    }

}
