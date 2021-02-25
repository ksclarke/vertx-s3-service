
package info.freelibrary.vertx.s3.service;

import info.freelibrary.util.HTTP;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import info.freelibrary.vertx.s3.AwsCredentials;
import info.freelibrary.vertx.s3.AwsProfile;
import info.freelibrary.vertx.s3.S3Client;
import info.freelibrary.vertx.s3.S3ClientOptions;
import info.freelibrary.vertx.s3.util.MessageCodes;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;

/**
 * An S3 client service implementation.
 */
public class S3ClientServiceImpl implements S3ClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientServiceImpl.class, MessageCodes.BUNDLE);

    protected final S3Client myS3Client;

    /**
     * Creates a new S3 client service.
     *
     * @param aVertx A Vert.x instance
     */
    public S3ClientServiceImpl(final Vertx aVertx) {
        this(aVertx, (AwsProfile) null);
    }

    /**
     * Creates a new S3 client service using the supplied AWS credentials profile.
     *
     * @param aVertx A Vert.x instance
     * @param aProfile An AWS credentials profile
     */
    public S3ClientServiceImpl(final Vertx aVertx, final AwsProfile aProfile) {
        if (aProfile == null) {
            myS3Client = new S3Client(aVertx);
        } else {
            myS3Client = new S3Client(aVertx, aProfile);
        }
    }

    /**
     * Creates a new S3 client service using the supplied AWS credentials.
     *
     * @param aVertx A Vert.x instance
     * @param aCredentials AWS credentials
     */
    public S3ClientServiceImpl(final Vertx aVertx, final AwsCredentials aCredentials) {
        myS3Client = new S3Client(aVertx, aCredentials);
    }

    /**
     * Creates a new S3 client service using the supplied AWS credentials and S3 endpoint.
     *
     * @param aVertx A Vert.x instance
     * @param aCredentials AWS credentials
     * @param aConfig A S3 client configuration
     */
    public S3ClientServiceImpl(final Vertx aVertx, final AwsCredentials aCredentials, final S3ClientOptions aConfig) {
        myS3Client = new S3Client(aVertx, aCredentials, aConfig);
    }

    @Override
    public void putJSON(final String aBucket, final String aKey, final JsonObject aJsonObject,
            final Handler<AsyncResult<Void>> aResult) {
        final Promise<Void> promise = Promise.promise();

        promise.future().onComplete(aResult);
        LOGGER.debug(MessageCodes.VSS_018, aKey, aBucket, aJsonObject);

        myS3Client.put(aBucket, aKey, aJsonObject.toBuffer(), put -> {
            if (put.succeeded()) {
                promise.complete();
            } else {
                promise.fail(put.cause());
            }
        }, exception -> {
            promise.fail(exception);
        });
    }

    @Override
    public void getJSON(final String aBucket, final String aKey, final Handler<AsyncResult<JsonObject>> aResult) {
        final Promise<JsonObject> promise = Promise.promise();

        promise.future().onComplete(aResult);
        LOGGER.debug(MessageCodes.VSS_019, aKey, aBucket);

        myS3Client.get(aBucket, aKey, get -> {
            if (get.succeeded()) {
                final HttpClientResponse response = get.result();

                if (HTTP.OK == response.statusCode()) {
                    response.body(body -> {
                        final JsonObject json = body.result().toJsonObject();

                        LOGGER.debug(MessageCodes.VSS_020, aKey, aBucket, json.encode());
                        promise.complete(json);
                    });
                } else {
                    promise.fail(new IllegalStateException(Integer.toString(response.statusCode())));
                }
            } else {
                promise.fail(get.cause());
            }
        }, exception -> {
            promise.fail(exception);
        });
    }

    @Override
    public void putFile(final String aBucket, final String aKey, final String aFilePath,
            final Handler<AsyncResult<Void>> aResult) {

    }

    @Override
    public void getFile(final String aBucket, final String aKey, final Handler<AsyncResult<String>> aResult) {

    }

    @Override
    public void close() {
        myS3Client.close();
    }

}
