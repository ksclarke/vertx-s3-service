
package info.freelibrary.vertx.s3;

import info.freelibrary.util.StringUtils;

/**
 * An S3 endpoint for a LocalStack implementation.
 */
public class LocalStackEndpoint implements Endpoint {

    private static final String ENDPOINT_PATTERN = "http://{}:{}/";

    private final String myEndpointURI;

    /**
     * Creates a new LocalStack endpoint for testing.
     *
     * @param aHost An S3 host
     * @param aPort An S3 port
     */
    public LocalStackEndpoint(final String aHost, final int aPort) {
        myEndpointURI = StringUtils.format(ENDPOINT_PATTERN, aHost, aPort);
    }

    /**
     * Creates a new LocalStack endpoint for testing.
     *
     * @param aEndpoint A URL endpoint in string form
     */
    public LocalStackEndpoint(final String aEndpoint) {
        myEndpointURI = aEndpoint;
    }

    @Override
    public String toString() {
        return myEndpointURI;
    }

    @Override
    public String getDualStack() {
        return myEndpointURI;
    }

    @Override
    public String getRegion() {
        return myEndpointURI;
    }

    @Override
    public String getLabel() {
        return myEndpointURI;
    }

}
