
package info.freelibrary.vertx.s3.service;

import info.freelibrary.vertx.s3.S3ClientOptions;
import info.freelibrary.vertx.s3.S3DataObject;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
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
     * Puts S3DataObject to the S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aKey A key for the JSON object
     * @param aObjectData A object with the data to be uploaded
     * @return The result of the PUT
     */
    Future<Void> put(String aBucket, String aKey, S3DataObject aObjectData);

    /**
     * Gets S3DataObject from the S3 client service.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @return The S3DataObject
     */
    Future<S3DataObject> get(String aBucket, String aKey);

    /**
     * Closes the S3 client service.
     *
     * @return A result for closing the service
     */
    @ProxyClose
    Future<Void> close();

}
