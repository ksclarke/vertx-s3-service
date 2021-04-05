
package info.freelibrary.vertx.s3;

import java.net.URI;
import java.util.Optional;

import io.vertx.core.http.HttpClientOptions;

/**
 * Configuration options for the S3 client.
 */
public class S3ClientOptions extends HttpClientOptions {

    private AwsCredentials myCredentials;

    private AwsProfile myProfile;

    /**
     * Creates a new S3 client options.
     */
    public S3ClientOptions() {
        setEndpoint(S3Endpoint.US_EAST_1);
    }

    /**
     * Creates a new S3 client options with a AWS credentials profile.
     *
     * @param aProfile An S3 credentials profile
     */
    public S3ClientOptions(final String aProfile) {
        setEndpoint(S3Endpoint.US_EAST_1);
        setProfile(aProfile);
    }

    /**
     * Creates a new S3 client options from the supplied S3 endpoint.
     *
     * @param aEndpoint An S3 endpoint
     */
    public S3ClientOptions(final Endpoint aEndpoint) {
        setEndpoint(aEndpoint);
    }

    /**
     * Creates a new S3 client options from the supplied S3 endpoint and AWS credentials profile.
     *
     * @param aProfile An S3 credentials profile
     * @param aEndpoint An S3 endpoint
     */
    public S3ClientOptions(final String aProfile, final Endpoint aEndpoint) {
        setEndpoint(aEndpoint);
        setProfile(aProfile);
    }

    /**
     * Creates a new S3 client options from the supplied HTTP client configuration.
     *
     * @param aConfig An HTTP client configuration.
     */
    public S3ClientOptions(final HttpClientOptions aConfig) {
        super(aConfig);
    }

    /**
     * Sets the AWS credentials to use.
     *
     * @param aCredentials An AWS credentials set
     * @return The S3 client options
     */
    public S3ClientOptions setCredentials(final AwsCredentials aCredentials) {
        myCredentials = aCredentials;
        return this;
    }

    /**
     * Sets the AWS credentials to use.
     *
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @return The S3 client options
     */
    public S3ClientOptions setCredentials(final String aAccessKey, final String aSecretKey) {
        myCredentials = new AwsCredentials(aAccessKey, aSecretKey);
        return this;
    }

    /**
     * Gets the AWS credentials to use.
     *
     * @return The AWS credentials
     */
    public Optional<AwsCredentials> getCredentials() {
        return Optional.ofNullable(myCredentials);
    }

    /**
     * Sets the AWS profile to use.
     *
     * @param aProfile An AWS profile
     * @return The S3 client options
     */
    public S3ClientOptions setProfile(final AwsProfile aProfile) {
        myCredentials = aProfile.getCredentials();
        myProfile = aProfile;
        return this;
    }

    /**
     * Sets the AWS profile to use from a profile name
     *
     * @param aProfile An AWS profile name
     * @return The S3 client options
     */
    public S3ClientOptions setProfile(final String aProfile) {
        final AwsProfile profile = new AwsProfile(aProfile);

        myCredentials = profile.getCredentials();
        myProfile = profile;

        return this;
    }

    /**
     * Gets the AWS profile to use.
     *
     * @return An AWS profile
     */
    public Optional<AwsProfile> getProfile() {
        return Optional.ofNullable(myProfile);
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
