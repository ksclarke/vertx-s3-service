
package info.freelibrary.vertx.s3.service;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.vertx.s3.AbstractS3FT;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests usage of the S3 client service proxy.
 */
@RunWith(VertxUnitRunner.class)
@Ignore
public class S3ClientServiceProxyImplFT extends AbstractS3FT {

    private static final String ADDRESS = "s3.service";

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
    // @Test
    // public final void testS3ClientServiceProxyImplVertxString(final TestContext aContext) {
    // final S3ClientService service = new S3ClientServiceImpl(myContext.vertx(), getConfig(), ADDRESS);
    // final JsonObject json = new JsonObject().put("asdf", "aaaa");
    // final Async asyncTask = aContext.async();
    //
    // service.put(myBucket, myKey, new S3DataObject(json.toBuffer())).onComplete(put -> {
    // if (put.succeeded()) {
    // try (S3Object s3Obj = myAwsS3Client.getObject(myBucket, myKey)) {
    // aContext.assertEquals(myBucket, s3Obj.getBucketName());
    // aContext.assertEquals(myKey, s3Obj.getKey());
    // s3Obj.getObjectContent().abort();
    // s3Obj.close();
    // } catch (final IOException details) {
    // aContext.fail(details);
    // } finally {
    // complete(asyncTask);
    // }
    // } else {
    // aContext.fail(put.cause());
    // }
    // });
    // }

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
