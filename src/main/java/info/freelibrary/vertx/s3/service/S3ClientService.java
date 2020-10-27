
package info.freelibrary.vertx.s3.service;

import info.freelibrary.vertx.s3.Profile;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

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
     * Creates a new S3 client service using the supplied Vert.x instance and AWS credentials profile.
     *
     * @param aVertx A Vert.x instance
     * @param aProfileName An AWS credentials profile name
     * @return The S3 client service
     */
    static S3ClientService createFromProfile(final Vertx aVertx, final String aProfileName) {
        return new S3ClientServiceImpl(aVertx, new Profile(aProfileName));
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
     * Creates a new S3 client service proxy using the supplied Vert.x instance and AWS credentials profile.
     *
     * @param aVertx A Vert.x instance
     * @param aProfileName An AWS credentials profile name
     * @param aAddress An address on the event bus
     * @return The S3 client service
     */
    static S3ClientService createProxyFromProfile(final Vertx aVertx, final String aProfileName,
            final String aAddress) {
        return new S3ClientServiceProxyImpl(aVertx, new Profile(aProfileName), aAddress);
    }

    /**
     * Puts a JsonObject to the S3 client service.
     *
     * @param aBucket An S3 bucket
     * @param aKey A key for the JSON object
     * @param aJsonObject A JSON object to put
     * @param aResult The result of the PUT
     */
    void put(String aBucket, String aKey, JsonObject aJsonObject, Handler<AsyncResult<Void>> aResult);

    /**
     * Gets a JsonObject from the S3 client service.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 object key
     * @param aResult The result of the GET
     */
    void get(String aBucket, String aKey, Handler<AsyncResult<JsonObject>> aResult);

    /**
     * Closes the S3 client service.
     */
    @ProxyClose
    void close();

}
