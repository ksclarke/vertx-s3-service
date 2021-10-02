
package info.freelibrary.vertx.s3.service;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.S3Object;

import info.freelibrary.vertx.s3.AbstractS3FT;
import info.freelibrary.vertx.s3.S3DataObject;
import info.freelibrary.vertx.s3.TestConstants;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests of the <code>S3ClientService</code>.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientServiceImplFT extends AbstractS3FT {

    /**
     * Tests the default constructor.
     *
     * @param aContext A testing context
     */
    @Test
    public void testS3ClientServiceImplVertx(final TestContext aContext) {
        final S3ClientService service = new S3ClientServiceImpl(myContext.vertx(), getConfig());
        final JsonObject testObject = new JsonObject().put(myKey, myBucket);
        final Async asyncTask = aContext.async();

        service.put(myBucket, myKey, new S3DataObject(testObject.toBuffer())).onComplete(put -> {
            if (put.succeeded()) {
                try (S3Object s3Obj = myAwsS3Client.getObject(myBucket, myKey)) {
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
     * Tests putting a resource to S3.
     *
     * @param aContext A testing context
     */
    @Test
    public void testPutJSON(final TestContext aContext) {
        final S3ClientService service = new S3ClientServiceImpl(myContext.vertx(), getConfig());
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
    }

    /**
     * Tests getting a resource from S3.
     *
     * @param aContext A testing context
     */
    @Test
    public void testGetJSON(final TestContext aContext) throws SdkClientException, AmazonServiceException {
        final S3ClientService service = new S3ClientServiceImpl(myContext.vertx(), getConfig());
        final String content = new JsonObject().put(TestConstants.ID, myKey).encodePrettily();
        final FileSystem fileSystem = myContext.vertx().fileSystem();
        final Async asyncTask = aContext.async();

        // PUT the object that we test into our test S3 bucket
        myAwsS3Client.putObject(myBucket, myKey, content);
        aContext.assertTrue(myAwsS3Client.doesObjectExist(myBucket, myKey));

        // Get the object from the S3 bucket and check its value
        service.get(myBucket, myKey).onComplete(get -> {
            if (get.succeeded()) {
                get.result().asBuffer(fileSystem).onComplete(asBuffer -> {
                    if (asBuffer.succeeded()) {
                        aContext.assertEquals(myKey, new JsonObject(asBuffer.result()).getString(TestConstants.ID));
                        complete(asyncTask);
                    } else {
                        aContext.fail(asBuffer.cause());
                    }
                });
            } else {
                aContext.fail(get.cause());
            }
        });
    }

}
