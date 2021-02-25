
package info.freelibrary.vertx.s3.service;

import info.freelibrary.vertx.s3.AwsCredentials;
import info.freelibrary.vertx.s3.AwsProfile;
import info.freelibrary.vertx.s3.S3ClientOptions;

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
        return new S3ClientServiceImpl(aVertx, new AwsProfile(aProfileName));
    }

    /**
     * Creates a new S3 client service using the supplied Vert.x instance and AWS credentials profile.
     *
     * @param aVertx A Vert.x instance
     * @param aCredentials AWS credentials
     * @return The S3 client service
     */
    static S3ClientService createFromCreds(final Vertx aVertx, final AwsCredentials aCredentials) {
        return new S3ClientServiceImpl(aVertx, aCredentials);
    }

    /**
     * Creates a new S3 client service using the supplied Vert.x instance, AWS credentials, and S3 endpoint.
     *
     * @param aVertx A Vert.x instance
     * @param aCredentials AWS credentials
     * @param aConfig An S3 client config
     * @return The S3 client service
     */
    static S3ClientService createCustom(final Vertx aVertx, final AwsCredentials aCredentials,
            final S3ClientOptions aConfig) {
        return new S3ClientServiceImpl(aVertx, aCredentials, aConfig);
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
        return new S3ClientServiceProxyImpl(aVertx, new AwsProfile(aProfileName), aAddress);
    }

    /**
     * Creates a new S3 client service proxy using the supplied Vert.x instance and AWS credentials.
     *
     * @param aVertx A Vert.x instance
     * @param aCredentials AWS credentials
     * @param aAddress An address on the event bus
     * @return The S3 client service
     */
    static S3ClientService createProxyFromCreds(final Vertx aVertx, final AwsCredentials aCredentials,
            final String aAddress) {
        return new S3ClientServiceProxyImpl(aVertx, aCredentials, aAddress);
    }

    /**
     * Creates a new S3 client service proxy using the supplied Vert.x instance, AWS credentials, S3 endpoint, and an
     * address on the event bus.
     *
     * @param aVertx A Vert.x instance
     * @param aCredentials AWS credentials
     * @param aConfig An S3 client config
     * @param aAddress An address on the event bus
     * @return The S3 client service
     */
    static S3ClientService createCustomProxy(final Vertx aVertx, final AwsCredentials aCredentials,
            final S3ClientOptions aConfig, final String aAddress) {
        return new S3ClientServiceProxyImpl(aVertx, aCredentials, aConfig, aAddress);
    }

    /**
     * Puts a JsonObject to the S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aKey A key for the JSON object
     * @param aJsonObject A JSON object
     * @param aResult The result of the PUT
     */
    void putJSON(String aBucket, String aKey, JsonObject aJsonObject, Handler<AsyncResult<Void>> aResult);

    /**
     * Gets a JsonObject from the S3 client service.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 object key
     * @param aResult The result of the GET
     */
    void getJSON(String aBucket, String aKey, Handler<AsyncResult<JsonObject>> aResult);

    /**
     * Puts a file to the S3 bucket.
     *
     * @param aBucket
     * @param aKey
     * @param aFilePath
     * @param aResult
     */
    void putFile(String aBucket, String aKey, String aFilePath, Handler<AsyncResult<Void>> aResult);

    /**
     * Gets a file from the S3 service.
     *
     * @param aBucket An S3 bucket
     * @param aKey An object key from the S3 bucket
     * @param aResult A result
     */
    void getFile(String aBucket, String aKey, Handler<AsyncResult<String>> aResult);

    /**
     * Closes the S3 client service.
     */
    @ProxyClose
    void close();

}
