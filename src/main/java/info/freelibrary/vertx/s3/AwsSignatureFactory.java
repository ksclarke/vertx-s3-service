
package info.freelibrary.vertx.s3;

import java.net.URI;
import java.util.Optional;

import info.freelibrary.util.I18nRuntimeException;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * An AWS signature factory from which S3 authentication signatures can be created.
 */
public final class AwsSignatureFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsSignatureFactory.class, Constants.BUNDLE_NAME);

    /**
     * The different signature options supported by this factory.
     */
    public enum Version {
        V2, V4
    }

    private final Version myVersion;

    private URI myHost;

    private Optional<AwsCredentials> myCredentials;

    private AwsSignatureFactory(final Version aVersion) {
        myVersion = aVersion;
    }

    /**
     * Gets a new signature factory using the most recent signature type.
     *
     * @return A new signature factory that uses the most recent signature type
     */
    public static AwsSignatureFactory getFactory() {
        return new AwsSignatureFactory(Version.V4);
    }

    /**
     * Gets a new signature factory using the supplied signature type.
     *
     * @param aVersion A type of AWS signature
     * @return A new signature factory that uses the supplied signature type
     */
    public static AwsSignatureFactory getFactory(final Version aVersion) {
        return new AwsSignatureFactory(aVersion);
    }

    /**
     * Sets the host for the V4 signature.
     *
     * @param aHost An S3 host
     * @return This signature factory
     */
    public AwsSignatureFactory setHost(final URI aHost) {
        myHost = aHost;
        return this;
    }

    /**
     * Gets the host used in the V4 signature.
     *
     * @return The S3 host
     */
    public URI getHost() {
        return myHost;
    }

    /**
     * Gets the credentials used by the AWS signature.
     *
     * @return The credentials used by the AWS signature
     */
    public Optional<AwsCredentials> getCredentials() {
        return myCredentials;
    }

    /**
     * Sets the credentials used by the AWS signature.
     *
     * @param aCredentials Used by the AWS signature
     * @return The signature factory
     * @throws ConfigurationException If authentication credentials are missing or invalid
     */
    public AwsSignatureFactory setCredentials(final AwsCredentials aCredentials) throws ConfigurationException {
        if (aCredentials == null || !aCredentials.isValid()) {
            throw new ConfigurationException();
        }

        myCredentials = Optional.of(aCredentials);
        return this;
    }

    /**
     * Sets the credentials used by the AWS signature.
     *
     * @param aAccessKey The AWS access key
     * @param aSecretKey The AWS secret key
     * @return The signature factory
     * @throws ConfigurationException If authentication credentials are missing or invalid
     */
    public AwsSignatureFactory setCredentials(final String aAccessKey, final String aSecretKey)
            throws ConfigurationException {
        if (aAccessKey != null && aSecretKey != null) {
            myCredentials = Optional.of(new AwsCredentials(aAccessKey, aSecretKey));
        } else {
            myCredentials = Optional.empty();
        }

        return this;
    }

    /**
     * Sets the credentials used by the AWS signature.
     *
     * @param aAccessKey The AWS access key
     * @param aSecretKey The AWS secret key
     * @param aSessionToken The AWS session token
     * @return The signature factory
     */
    public AwsSignatureFactory setCredentials(final String aAccessKey, final String aSecretKey,
            final String aSessionToken) {
        if (aAccessKey != null && aSecretKey != null && aSessionToken != null) {
            myCredentials = Optional.of(new AwsCredentials(aAccessKey, aSecretKey, aSessionToken));
        } else {
            myCredentials = Optional.empty();
        }

        return this;
    }

    /**
     * Gets an AWS authentication signature.
     *
     * @return The AWS authentication signature
     */
    public AwsSignature getSignature() {
        final AwsSignature signature;

        if (myVersion.equals(Version.V2)) {
            signature = new AwsV2Signature(myCredentials.get());
        } else if (myVersion.equals(Version.V4)) {
            signature = new AwsV4Signature(myHost, myCredentials.get());
        } else {
            throw new I18nRuntimeException(Constants.BUNDLE_NAME, MessageCodes.VS3_005);
        }

        return signature;
    }

}
