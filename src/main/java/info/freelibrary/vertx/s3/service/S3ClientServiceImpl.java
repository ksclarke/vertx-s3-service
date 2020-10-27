
package info.freelibrary.vertx.s3.service;

import info.freelibrary.vertx.s3.Profile;
import info.freelibrary.vertx.s3.S3Client;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * An S3 client service implementation.
 */
public class S3ClientServiceImpl implements S3ClientService {

    protected final S3Client myS3Client;

    /**
     * Creates a new S3 client service.
     *
     * @param aVertx A Vert.x instance
     */
    public S3ClientServiceImpl(final Vertx aVertx) {
        this(aVertx, null);
    }

    /**
     * Creates a new S3 client service using the supplied AWS credentials profile.
     *
     * @param aVertx A Vert.x instance
     * @param aProfile An AWS credentials profile
     */
    public S3ClientServiceImpl(final Vertx aVertx, final Profile aProfile) {
        myS3Client = new S3Client(aVertx, aProfile);
    }

    @Override
    public void put(final String aBucket, final String aKey, final JsonObject aJsonObject,
            final Handler<AsyncResult<Void>> aResult) {
        System.out.println("PUT!");
    }

    @Override
    public void get(final String aBucket, final String aKey, final Handler<AsyncResult<JsonObject>> aResult) {
        System.out.println("GET!");
    }

    @Override
    public void close() {
        myS3Client.close();
    }

}
