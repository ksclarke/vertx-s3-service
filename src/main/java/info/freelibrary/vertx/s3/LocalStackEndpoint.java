
package info.freelibrary.vertx.s3;

import java.net.MalformedURLException;
import java.net.URL;

import info.freelibrary.util.Constants;
import info.freelibrary.util.MalformedUrlException;
import info.freelibrary.util.StringUtils;
import info.freelibrary.util.warnings.PMD;

/**
 * An S3 endpoint for a LocalStack implementation.
 */
public class LocalStackEndpoint implements Endpoint {

    /**
     * The system property for the endpoint port.
     */
    public static final String PORT_PROPERTY = "localstack.port";

    /**
     * The default region used by LocalStack.
     */
    public static final String REGION = "us-east-1";

    /**
     * An endpoint pattern.
     */
    public static final String ENDPOINT_PATTERN = "http://{}:{}/";

    /**
     * The LocalStack endpoint.
     */
    private final URL myEndpoint;

    /**
     * Creates a new LocalStack endpoint for testing.
     *
     * @param aPort An S3 port
     * @throws Runtime MalformedUrlException if the supplied port is invalid
     */
    @SuppressWarnings({ PMD.PRESERVE_STACK_TRACE, "PMD.PreserveStackTrace" })
    public LocalStackEndpoint(final int aPort) {
        try {
            myEndpoint = new URL(StringUtils.format(ENDPOINT_PATTERN, Constants.INADDR_ANY, aPort));
        } catch (final MalformedURLException details) {
            throw new MalformedUrlException(details.getMessage());
        }
    }

    /**
     * Creates a new LocalStack endpoint for testing.
     *
     * @param aHost An S3 host
     * @param aPort An S3 port
     * @throws Runtime MalformedUrlException if the supplied port or host is invalid
     */
    @SuppressWarnings({ PMD.PRESERVE_STACK_TRACE, "PMD.PreserveStackTrace" })
    public LocalStackEndpoint(final String aHost, final int aPort) {
        try {
            myEndpoint = new URL(StringUtils.format(ENDPOINT_PATTERN, aHost, aPort));
        } catch (final MalformedURLException details) {
            throw new MalformedUrlException(details.getMessage());
        }
    }

    /**
     * Creates a new LocalStack endpoint for testing.
     *
     * @param aEndpoint A URL endpoint in string form
     * @throws Runtime MalformedUriException if the supplied endpoint is not a valid URL
     */
    @SuppressWarnings({ PMD.PRESERVE_STACK_TRACE, "PMD.PreserveStackTrace" })
    public LocalStackEndpoint(final String aEndpoint) {
        try {
            myEndpoint = new URL(aEndpoint);
        } catch (final MalformedURLException details) {
            throw new MalformedUrlException(details.getMessage());
        }
    }

    @Override
    public String toString() {
        return myEndpoint.toString();
    }

    @Override
    public String getDualStack() {
        return myEndpoint.toString();
    }

    @Override
    public String getRegion() {
        return REGION;
    }

    @Override
    public String getHost() {
        return myEndpoint.getHost();
    }

    @Override
    public String getLabel() {
        return myEndpoint.toString();
    }

    @Override
    public int getPort() {
        return myEndpoint.getPort();
    }

}
