
package info.freelibrary.vertx.s3;

import java.net.URI;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

/**
 * Configuration options for the S3 client.
 */
@DataObject
public class S3ClientOptions extends HttpClientOptions {

    /**
     * Creates a new S3 client configuration using the default AWS S3 endpoint.
     */
    public S3ClientOptions() {
        setEndpoint(S3Endpoint.US_EAST_1);
    }

    /**
     * Creates a new S3 client configuration using the supplied S3 endpoint.
     *
     * @param aEndpoint An S3 endpoint the client should use
     */
    public S3ClientOptions(final Endpoint aEndpoint) {
        setEndpoint(aEndpoint);
    }

    /**
     * Creates a new S3 client configuration from the supplied JSON serialization.
     *
     * @param aJsonObject A JSON serialization of the S3 client configuration
     */
    public S3ClientOptions(final JsonObject aJsonObject) {
        super(aJsonObject);
    }

    /**
     * Sets the endpoint for the S3 client.
     *
     * @param aEndpoint An S3 endpoint
     * @return The S3 client options
     */
    public S3ClientOptions setEndpoint(final Endpoint aEndpoint) {
        final URI endpointURI = URI.create(aEndpoint.toString());
        final String protocol = endpointURI.getScheme();
        final String host = endpointURI.getHost();
        final int port = endpointURI.getPort();

        setDefaultHost(host);

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
