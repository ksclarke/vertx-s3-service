
package info.freelibrary.vertx.s3.service;

import info.freelibrary.vertx.s3.S3ClientOptions;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * A S3 client service implementation that can be used as a proxy.
 */
public class S3ClientServiceProxyImpl extends S3ClientServiceImpl implements S3ClientService {

    /**
     * The message consumer for the proxy implementation.
     */
    private final MessageConsumer<JsonObject> myConsumer;

    /**
     * The service binder for the proxy implementation.
     */
    private final ServiceBinder myServiceBinder;

    /**
     * Creates a new S3 client service proxy.
     *
     * @param aVertx A Vert.x instance
     * @param aAddress A Vert.x event bus address
     */
    public S3ClientServiceProxyImpl(final Vertx aVertx, final String aAddress) {
        super(aVertx);

        myServiceBinder = new ServiceBinder(aVertx);
        myConsumer = myServiceBinder.setAddress(aAddress).register(S3ClientService.class, this);
    }

    /**
     * Creates a new S3 client service proxy using the supplied AWS credentials profile.
     *
     * @param aVertx A Vert.x instance
     * @param aConfig An S3 client configuration
     * @param aAddress A Vert.x event bus address
     */
    public S3ClientServiceProxyImpl(final Vertx aVertx, final S3ClientOptions aConfig, final String aAddress) {
        super(aVertx, aConfig);

        myServiceBinder = new ServiceBinder(aVertx);
        myConsumer = myServiceBinder.setAddress(aAddress).register(S3ClientService.class, this);

        myConsumer.handler(handler -> {
            System.out.println(handler.address());
        });
    }

    @Override
    @ProxyClose
    public Future<Void> close() {
        final Future<Void> future = super.close();
        myServiceBinder.unregister(myConsumer);
        return future;
    }
}
