
package info.freelibrary.vertx.s3.service;

import info.freelibrary.vertx.s3.AwsCredentials;
import info.freelibrary.vertx.s3.AwsProfile;
import info.freelibrary.vertx.s3.S3ClientOptions;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * A S3 client service implementation that can be used as a proxy.
 */
public class S3ClientServiceProxyImpl extends S3ClientServiceImpl implements S3ClientService {

    private final MessageConsumer<JsonObject> myConsumer;

    private final ServiceBinder myServiceBinder;

    /**
     * Creates a new S3 client service proxy.
     *
     * @param aVertx A Vert.x instance
     * @param aAddress A Vert.x event bus address
     */
    public S3ClientServiceProxyImpl(final Vertx aVertx, final String aAddress) {
        this(aVertx, (AwsProfile) null, aAddress);
    }

    /**
     * Creates a new S3 client service proxy using the supplied AWS credentials profile.
     *
     * @param aVertx A Vert.x instance
     * @param aProfile An AWS credentials profile
     * @param aAddress A Vert.x event bus address
     */
    public S3ClientServiceProxyImpl(final Vertx aVertx, final AwsProfile aProfile, final String aAddress) {
        super(aVertx, aProfile);

        myServiceBinder = new ServiceBinder(aVertx);
        myConsumer = myServiceBinder.setAddress(aAddress).register(S3ClientService.class, this);

        myConsumer.handler(handler -> {
            System.out.println("====== here ======");
            handler.reply("asdf");
        });
    }

    /**
     * Creates a new S3 client service proxy using the supplied AWS credentials profile.
     *
     * @param aVertx A Vert.x instance
     * @param aCredentials AWS credentials
     * @param aAddress A Vert.x event bus address
     */
    public S3ClientServiceProxyImpl(final Vertx aVertx, final AwsCredentials aCredentials, final String aAddress) {
        super(aVertx, aCredentials);

        myServiceBinder = new ServiceBinder(aVertx);
        myConsumer = myServiceBinder.setAddress(aAddress).register(S3ClientService.class, this);
    }

    /**
     * Creates a new S3 client service proxy using the supplied AWS credentials profile.
     *
     * @param aVertx A Vert.x instance
     * @param aCredentials AWS credentials
     * @param aConfig An S3 client configuration
     * @param aAddress A Vert.x event bus address
     */
    public S3ClientServiceProxyImpl(final Vertx aVertx, final AwsCredentials aCredentials,
            final S3ClientOptions aConfig, final String aAddress) {
        super(aVertx, aCredentials, aConfig);

        myServiceBinder = new ServiceBinder(aVertx);
        myConsumer = myServiceBinder.setAddress(aAddress).register(S3ClientService.class, this);
    }

    @Override
    @ProxyClose
    public void close() {
        super.close();
        myServiceBinder.unregister(myConsumer);
    }
}
