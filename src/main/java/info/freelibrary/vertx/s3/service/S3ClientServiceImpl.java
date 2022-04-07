
package info.freelibrary.vertx.s3.service;

import java.util.Objects;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import info.freelibrary.vertx.s3.HttpHeaders;
import info.freelibrary.vertx.s3.S3Client;
import info.freelibrary.vertx.s3.S3ClientOptions;
import info.freelibrary.vertx.s3.S3Object;
import info.freelibrary.vertx.s3.util.MessageCodes;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
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
    public Future<HttpHeaders> put(final String aBucket, final S3Object aS3Object) {
        final Promise<HttpHeaders> promise = Promise.promise();
        final String objectKey = aS3Object.getKey();

        if (objectKey != null) {
            LOGGER.debug(MessageCodes.VSS_018, objectKey, aBucket);

            if (aS3Object.getSource() == S3Object.Type.BUFFER) {
                aS3Object.asBuffer(myS3Client.getVertx().fileSystem()).onFailure(promise::fail).onSuccess(buffer -> {
                    aS3Object.getMetadata().ifPresentOrElse(metadata -> {
                        myS3Client.put(aBucket, aS3Object.getKey(), buffer, metadata) //
                                .onFailure(promise::fail).onSuccess(promise::complete);
                    }, () -> {
                        myS3Client.put(aBucket, aS3Object.getKey(), buffer) //
                                .onFailure(promise::fail).onSuccess(promise::complete);
                    });
                });
            } else {
                aS3Object.asFile(myS3Client.getVertx().fileSystem()).onFailure(promise::fail).onSuccess(file -> {
                    aS3Object.getMetadata().ifPresentOrElse(metadata -> {
                        myS3Client.put(aBucket, aS3Object.getKey(), file, metadata) //
                                .onSuccess(promise::complete).onFailure(promise::fail);
                    }, () -> {
                        myS3Client.put(aBucket, aS3Object.getKey(), file) //
                                .onSuccess(promise::complete).onFailure(promise::fail);
                    });
                });
            }
        } else {
            promise.fail(new MissingKeyException(LOGGER.getMessage(MessageCodes.VSS_027, aBucket)));
        }

        return promise.future();
    }

    @Override
    public Future<S3Object> get(final String aBucket, final String aKey) {
        final Promise<S3Object> promise = Promise.promise();

        LOGGER.debug(MessageCodes.VSS_019, aKey, aBucket);

        myS3Client.get(aBucket, aKey).onComplete(get -> {
            if (get.succeeded()) {
                get.result().body(body -> {
                    promise.complete(new S3Object(aKey, body.result()));
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
