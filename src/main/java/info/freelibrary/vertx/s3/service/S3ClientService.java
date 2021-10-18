
package info.freelibrary.vertx.s3.service;

import info.freelibrary.util.Constants;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.Stopwatch;

import info.freelibrary.vertx.s3.S3ClientOptions;
import info.freelibrary.vertx.s3.S3Object;
import info.freelibrary.vertx.s3.service.MissingKeyException.MissingKeyExceptionMessageCodec;
import info.freelibrary.vertx.s3.service.S3ServiceException.S3ServiceExceptionMessageCodec;
import info.freelibrary.vertx.s3.util.MessageCodes;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
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
    @GenIgnore
    String DEFAULT_ADDRESS = S3ClientService.class.getName();

    /**
     * The environmental property used to override the default initialization timeout.
     */
    @GenIgnore
    String INIT_TIMEOUT_PROPERTY = "S3_CLIENT_INIT_TIMEOUT";

    /**
     * The maximum amount of time in milliseconds that we wait for a service to be initialized.
     */
    @GenIgnore
    String DEFAULT_INIT_TIMEOUT = "10";

    /**
     * Creates a new S3 client service proxy using the supplied Vert.x instance. If an event bus address is supplied,
     * that is used; else, the default address is used.
     *
     * @param aVertx A Vert.x instance
     * @param aAddress An address on the event bus
     * @return The S3 client service
     * @throws S3ServiceException If there is trouble creating the service
     */
    @GenIgnore
    static Future<S3ClientService> getProxy(final Vertx aVertx, final String... aAddress) {
        return getProxyWithOpts(aVertx, (S3ClientOptions) null, aAddress);
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
    @GenIgnore
    static Future<S3ClientService> getProxyWithOpts(final Vertx aVertx, final S3ClientOptions aConfig,
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
                final EventBus eventBus = aVertx.eventBus();

                // Check succeeded... stop the watch so we can log the elapsed time
                stopwatch.stop();

                // Register the S3 client service on the event bus so that we can create proxies for it
                if (aConfig == null) {
                    service = new S3ClientServiceImpl(aVertx, address);
                } else {
                    service = new S3ClientServiceImpl(aVertx, address, aConfig);
                }

                // Log that we've successfully registered the S3 client service
                logger.info(MessageCodes.VSS_023, service.getName(), address, stopwatch.getMilliseconds());

                // Register service exceptions related to the S3 client service
                eventBus.registerDefaultCodec(S3ServiceException.class, new S3ServiceExceptionMessageCodec());
                eventBus.registerDefaultCodec(MissingKeyException.class, new MissingKeyExceptionMessageCodec());

                logger.debug(MessageCodes.VSS_025, S3ServiceExceptionMessageCodec.class.getSimpleName());
                logger.debug(MessageCodes.VSS_025, MissingKeyExceptionMessageCodec.class.getSimpleName());
            } else {
                final String msecString = stopwatch.stop().getMilliseconds();
                final int index = msecString.lastIndexOf(Constants.SPACE);
                final int length = msecString.length();
                // msecString's value should always have a space, but let's be cautious about future changes
                final Long elapsedTime = Long.parseLong(msecString.substring(0, index != -1 ? index : length));

                // If we cannot get a lock the service should already be initialized; record how long we waited
                if (elapsedTime > timeout) {
                    logger.debug(MessageCodes.VSS_024, S3ClientService.class.getSimpleName(), elapsedTime);
                }
            }

            promise.complete(new ServiceProxyBuilder(aVertx).setAddress(address).build(S3ClientService.class));
        });

        return promise.future();
    }

    /**
     * Gets the name of the service.
     *
     * @return The human friendly name of the service
     */
    @GenIgnore
    default String getName() {
        return S3ClientService.class.getSimpleName();
    }

    /**
     * Puts S3DataObject to the S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aS3Object A object with the data to be uploaded
     * @return The result of the PUT
     */
    Future<Void> put(String aBucket, S3Object aS3Object);

    /**
     * Gets S3DataObject from the S3 client service.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @return The S3Object
     */
    Future<S3Object> get(String aBucket, String aKey);

    /**
     * Closes the S3 client service.
     *
     * @return A result for closing the service
     */
    @ProxyClose
    Future<Void> close();

}
