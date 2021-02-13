
package info.freelibrary.vertx.s3.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import info.freelibrary.vertx.s3.AwsCredentials;
import info.freelibrary.vertx.s3.HTTP;
import info.freelibrary.vertx.s3.MessageCodes;
import info.freelibrary.vertx.s3.Profile;
import info.freelibrary.vertx.s3.S3Client;
import info.freelibrary.vertx.s3.S3Endpoint;

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
        this(aVertx, null);
    }

    /**
     * Creates a new S3 client service using the supplied AWS credentials profile.
     *
     * @param aVertx A Vert.x instance
     * @param aProfile An AWS credentials profile
     */
    public S3ClientServiceImpl(final Vertx aVertx, final Profile aProfile) {
        if (aProfile == null) {
            myS3Client = new S3Client(aVertx);
        } else {
            myS3Client = new S3Client(aVertx, aProfile);
        }
    }

    /**
     * Creates a new S3 client service using the supplied AWS credentials and S3 endpoint.
     *
     * @param aVertx A Vert.x instance
     * @param aCredentials AWS credentials
     * @param aEndpoint An S3 endpoint
     */
    public S3ClientServiceImpl(final Vertx aVertx, final AwsCredentials aCredentials, final S3Endpoint aEndpoint) {
        final String accessKey = aCredentials.getAccessKey();
        final String secretKey = aCredentials.getSecretKey();

        if (aCredentials.hasSessionToken()) {
            myS3Client = new S3Client(aVertx, accessKey, secretKey, aCredentials.getSessionToken(), aEndpoint);
        } else {
            myS3Client = new S3Client(aVertx, accessKey, secretKey, aEndpoint);
        }
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
    public void putBytes(final String aBucket, final String aKey, final List<Byte> aBytesList,
            final Handler<AsyncResult<Void>> aResult) {

    }

    @Override
    public void getBytes(final String aBucket, final String aKey, final Handler<AsyncResult<List<Byte>>> aResult) {
        final Promise<List<Byte>> promise = Promise.promise();

        promise.future().onComplete(aResult);
        LOGGER.debug(MessageCodes.VSS_019, aKey, aBucket);

        myS3Client.get(aBucket, aKey, get -> {
            if (get.succeeded()) {
                final HttpClientResponse response = get.result();

                if (HTTP.OK == response.statusCode()) {
                    response.body(body -> {
                        final byte[] bytes = body.result().getBytes();

                        LOGGER.debug(MessageCodes.VSS_020, aKey, aBucket, new String(bytes, StandardCharsets.UTF_8));
                        promise.complete();
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
