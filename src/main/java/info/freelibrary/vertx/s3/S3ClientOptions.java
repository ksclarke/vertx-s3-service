
package info.freelibrary.vertx.s3;

import info.freelibrary.util.StringUtils;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

/**
 * Configuration options for the S3 client.
 */
@DataObject
public class S3ClientOptions extends HttpClientOptions {

    /**
     * Creates anew S3 client configuration.
     */
    public S3ClientOptions() {
    }

    /**
     * Creates a new S3ClientOption from the JSON serialization.
     *
     * @param aJsonObject
     */
    @SuppressWarnings("unused")
    public S3ClientOptions(final JsonObject aJsonObject) {
        super(aJsonObject);
    }

    /**
     * Sets the endpoint for the S3 client.
     *
     * @param aEndpoint An S3 endpoint
     * @return The S3 client options
     */
    public S3ClientOptions setEndpoint(final String aEndpoint) {
        return setEndpoint(new S3Endpoint(aEndpoint));
    }

    /**
     * Sets the endpoint for the S3 client.
     *
     * @param aEndpoint An S3 endpoint
     * @return The S3 client options
     */
    public S3ClientOptions setEndpoint(final S3Endpoint aEndpoint) {
        final String host = StringUtils.trimToNull(aEndpoint.getHost());
        final String protocol = aEndpoint.getProtocol();
        final int port = aEndpoint.getPort();

        // If there is a supplied host, set it in the client options
        if (host != null) {
            setDefaultHost(host);
        }

        // An exception has been thrown if there is no protocol
        if ("http".equals(protocol)) {
            setSsl(false);

            if (port != -1) {
                setDefaultPort(port);
            } else {
                setDefaultPort(80);
            }
        } else {
            setSsl(true);

            if (port != -1) {
                setDefaultPort(port);
            } else {
                setDefaultPort(443);
            }
        }

        return this;
    }
}
