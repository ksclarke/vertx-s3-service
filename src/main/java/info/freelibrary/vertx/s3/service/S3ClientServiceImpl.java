
package info.freelibrary.vertx.s3.service;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import info.freelibrary.vertx.s3.S3Client;
import info.freelibrary.vertx.s3.S3ClientOptions;
import info.freelibrary.vertx.s3.S3ObjectData;
import info.freelibrary.vertx.s3.util.MessageCodes;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

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
        myS3Client = new S3Client(aVertx);
    }

    /**
     * Creates a new S3 client service using the supplied AWS credentials and S3 endpoint.
     *
     * @param aVertx A Vert.x instance
     * @param aConfig A S3 client configuration
     */
    public S3ClientServiceImpl(final Vertx aVertx, final S3ClientOptions aConfig) {
        myS3Client = new S3Client(aVertx, aConfig);
    }

    @Override
    public S3ClientService put(final String aBucket, final String aKey, final S3ObjectData aObject,
        final Handler<AsyncResult<Void>> aResult) {
        final FileSystem fileSystem = myS3Client.getVertx().fileSystem();
        final Promise<Void> promise = Promise.promise();

        // Set the supplied handler as the promise handler
        promise.future().onComplete(aResult);
        LOGGER.debug(MessageCodes.VSS_018, aKey, aBucket, aObject);

        if (aObject.source() == S3ObjectData.Type.BUFFER) {
            aObject.asBuffer(fileSystem).onComplete(asBuffer -> {
                if (asBuffer.succeeded()) {
                    myS3Client.put(aBucket, aKey, asBuffer.result(), put -> {
                        if (put.succeeded()) {
                            promise.complete();
                        } else {
                            promise.fail(put.cause());
                        }
                    });
                } else {
                    promise.fail(asBuffer.cause());
                }
            });
        } else {
            aObject.asFile(fileSystem).onComplete(asFile -> {
                if (asFile.succeeded()) {
                    myS3Client.put(aBucket, aKey, asFile.result(), put -> {
                        if (put.succeeded()) {
                            promise.complete();
                        } else {
                            promise.fail(put.cause());
                        }
                    });
                } else {
                    promise.fail(asFile.cause());
                }
            });
        }

        return this;
    }

    @Override
    public S3ClientService get(final String aBucket, final String aKey,
        final Handler<AsyncResult<S3ObjectData>> aResult) {
        final Promise<S3ObjectData> promise = Promise.promise();

        // Set the supplied handler as the promise handler
        promise.future().onComplete(aResult);
        LOGGER.debug(MessageCodes.VSS_019, aKey, aBucket);

        myS3Client.get(aBucket, aKey, get -> {
            if (get.succeeded()) {
                get.result().body(body -> {
                    promise.complete(new S3ObjectData(body.result()));
                });
            } else {
                promise.fail(get.cause());
            }
        });

        return this;
    }

    @Override
    public void close() {
        myS3Client.close();
    }

}
