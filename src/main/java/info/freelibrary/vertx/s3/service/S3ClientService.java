
package info.freelibrary.vertx.s3.service;

import info.freelibrary.vertx.s3.S3ClientOptions;
import info.freelibrary.vertx.s3.S3ObjectData;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * An S3 client service.
 */
@ProxyGen
@VertxGen
public interface S3ClientService {

    /**
     * Creates a new S3 client service using the supplied Vert.x instance.
     *
     * @param aVertx A Vert.x instance
     * @return The S3 client service
     */
    static S3ClientService create(final Vertx aVertx) {
        return new S3ClientServiceImpl(aVertx);
    }

    /**
     * Creates a new S3 client service using the supplied Vert.x instance and S3 endpoint.
     *
     * @param aVertx A Vert.x instance
     * @param aConfig An S3 client config
     * @return The S3 client service
     */
    static S3ClientService createWithOptions(final Vertx aVertx, final S3ClientOptions aConfig) {
        return new S3ClientServiceImpl(aVertx, aConfig);
    }

    /**
     * Creates a new S3 client service proxy using the supplied Vert.x instance.
     *
     * @param aVertx A Vert.x instance
     * @param aAddress An address on the event bus
     * @return The S3 client service
     */
    static S3ClientService createProxy(final Vertx aVertx, final String aAddress) {
        return new S3ClientServiceProxyImpl(aVertx, aAddress);
    }

    /**
     * Creates a new S3 client service proxy using the supplied Vert.x instance and S3 client options.
     *
     * @param aVertx A Vert.x instance
     * @param aConfig An S3 client config
     * @param aAddress An address on the event bus
     * @return The S3 client service
     */
    static S3ClientService createProxyWithOptions(final Vertx aVertx, final S3ClientOptions aConfig,
        final String aAddress) {
        return new S3ClientServiceProxyImpl(aVertx, aConfig, aAddress);
    }

    /**
     * Puts a JsonObject to the S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aKey A key for the JSON object
     * @param aObjectData A object with the data to be uploaded
     * @param aResult The result of the PUT
     * @return The service
     */
    @Fluent
    S3ClientService put(String aBucket, String aKey, S3ObjectData aObjectData, Handler<AsyncResult<Void>> aResult);

    /**
     * Gets a JsonObject from the S3 client service.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 object key
     * @param aResult A handler for the result
     * @return The service
     */
    @Fluent
    S3ClientService get(String aBucket, String aKey, Handler<AsyncResult<S3ObjectData>> aResult);

    /**
     * Closes the S3 client service.
     */
    @ProxyClose
    void close();

}
