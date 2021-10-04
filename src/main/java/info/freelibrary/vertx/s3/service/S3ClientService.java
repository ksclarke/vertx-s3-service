
package info.freelibrary.vertx.s3.service;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.MessageCodes;
import info.freelibrary.util.Stopwatch;
import info.freelibrary.vertx.s3.S3ClientOptions;
import info.freelibrary.vertx.s3.S3DataObject;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceProxyBuilder;

/**
 * An S3 client service.
 */
@ProxyGen
@VertxGen
public interface S3ClientService {

    /**
     * A default event bus service address.
     */
    String DEFAULT_ADDRESS = S3ClientService.class.getName();

    /**
     * The environmental property used to override the default initialization timeout.
     */
    String INIT_TIMEOUT_PROPERTY = "S3_CLIENT_INIT_TIMEOUT";

    /**
     * The maximum amount of time in milliseconds that we wait for a service to be initialized.
     */
    String DEFAULT_INIT_TIMEOUT = "10";

    /**
     * Creates a new S3 client service proxy using the supplied Vert.x instance. If an event bus address is supplied,
     * that is used; else, the default address is used.
     *
     * @param aVertx A Vert.x instance
     * @param aAddress An address on the event bus
     * @return The S3 client service
     * @throws S3ServiceException If there is trouble getting the proxy
     */
    static Future<S3ClientService> create(final Vertx aVertx, final String... aAddress) {
        return createWithOptions(aVertx, (S3ClientOptions) null, aAddress);
    }

    /**
     * Creates a new S3 client service proxy using the supplied Vert.x instance and S3 client options. If an event bus
     * address is supplied, that is used; else, the default address is used.
     *
     * @param aVertx A Vert.x instance
     * @param aConfig An S3 client configuration
     * @param aAddress An address on the event bus
     * @return The S3 client service
     */
    static Future<S3ClientService> createWithOptions(final Vertx aVertx, final S3ClientOptions aConfig,
        final String... aAddress) {

        final long timeout = Long.parseLong(System.getenv().getOrDefault(INIT_TIMEOUT_PROPERTY, DEFAULT_INIT_TIMEOUT));
        final Logger logger = LoggerFactory.getLogger(S3ClientService.class, MessageCodes.BUNDLE);
        final Promise<S3ClientService> promise = Promise.promise();
        final Stopwatch stopwatch = new Stopwatch().start();

        // Check for an initialization lock (if the lock exists, the service has been initialized)
        aVertx.sharedData().getLockWithTimeout(S3ClientService.class.getName(), timeout, getLock -> {
            final String address = aAddress.length > 0 ? aAddress[0] : DEFAULT_ADDRESS;
            final S3ClientServiceImpl service;

            // If getting the lock succeeds, it's the first time we'll initialize an S3 client service
            if (getLock.succeeded()) {
                stopwatch.stop();

                // Register the S3 client service on the event bus so that we can create proxies for it
                if (aConfig == null) {
                    service = new S3ClientServiceImpl(aVertx, address);
                } else {
                    service = new S3ClientServiceImpl(aVertx, address, aConfig);
                }

                logger.info("Service '{}' registered at '{}' in {} ms", service.getClass().getSimpleName(), aAddress,
                    stopwatch.getMilliseconds());
            } else {
                final Long elapsedTime = Long.parseLong(stopwatch.stop().getMilliseconds());

                // If we cannot get a lock the service has already been initialized; record how long we waited
                if (elapsedTime > timeout) {
                    logger.debug(
                        "{}'s initialization attempt couldn't get a lock in {} ms -- service should already exist",
                        S3ClientService.class.getSimpleName(), elapsedTime);
                }
            }

            promise.complete(new ServiceProxyBuilder(aVertx).setAddress(address).build(S3ClientService.class));
        });

        return promise.future();
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
