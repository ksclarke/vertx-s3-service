
package info.freelibrary.vertx.s3.service;

import java.util.Objects;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.vertx.s3.S3Client;
import info.freelibrary.vertx.s3.S3ClientOptions;
import info.freelibrary.vertx.s3.S3DataObject;
import info.freelibrary.vertx.s3.util.MessageCodes;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * An S3 client service implementation.
 */
public class S3ClientServiceImpl implements S3ClientService {

    /**
     * The client service implementation logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientServiceImpl.class, MessageCodes.BUNDLE);

    /**
     * An inner S3 client.
     */
    protected final S3Client myS3Client;

    /**
     * The message consumer for the proxy implementation.
     */
    private final MessageConsumer<JsonObject> myConsumer;

    /**
     * A Vert.x service binder.
     */
    private final ServiceBinder myServiceBinder;

    /**
     * Creates a new S3 client service.
     *
     * @param aVertx A Vert.x instance
     * @param aAddress An event bus address for the service
     */
    public S3ClientServiceImpl(final Vertx aVertx, final String aAddress) {
        Objects.requireNonNull(aVertx);
        Objects.requireNonNull(aAddress);

        myS3Client = new S3Client(aVertx);
        myServiceBinder = new ServiceBinder(aVertx);
        myConsumer = myServiceBinder.setAddress(aAddress).register(S3ClientService.class, this);
    }

    /**
     * Creates a new S3 client service using the supplied AWS credentials and S3 endpoint.
     *
     * @param aVertx A Vert.x instance
     * @param aConfig A S3 client configuration
     * @param aAddress An event bus address for the service
     */
    public S3ClientServiceImpl(final Vertx aVertx, final String aAddress, final S3ClientOptions aConfig) {
        Objects.requireNonNull(aAddress);
        Objects.requireNonNull(aConfig);
        Objects.requireNonNull(aVertx);

        myS3Client = new S3Client(aVertx, aConfig);
        myServiceBinder = new ServiceBinder(aVertx);
        myConsumer = myServiceBinder.setAddress(aAddress).register(S3ClientService.class, this);
    }

    @Override
    public Future<Void> put(final String aBucket, final String aKey, final S3DataObject aObject) {
        final FileSystem fileSystem = myS3Client.getVertx().fileSystem();
        final Promise<Void> promise = Promise.promise();

        LOGGER.debug(MessageCodes.VSS_018, aKey, aBucket, aObject);

        if (aObject.source() == S3DataObject.Type.BUFFER) {
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

        return promise.future();
    }

    @Override
    public Future<S3DataObject> get(final String aBucket, final String aKey) {
        final Promise<S3DataObject> promise = Promise.promise();

        LOGGER.debug(MessageCodes.VSS_019, aKey, aBucket);

        myS3Client.get(aBucket, aKey).onComplete(get -> {
            if (get.succeeded()) {
                get.result().body(body -> {
                    promise.complete(new S3DataObject(body.result()));
                });
            } else {
                promise.fail(get.cause());
            }
        });

        return promise.future();
    }

    @Override
    public Future<Void> close() {
        if (myServiceBinder != null) {
            myServiceBinder.unregister(myConsumer);
        }

        return myS3Client.close();
    }

}
