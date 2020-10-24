
package info.freelibrary.vertx.s3;

/**
 * An AWS credentials object.
 */
public class AwsCredentials extends uk.co.lucasweb.aws.v4.signer.credentials.AwsCredentials {

    private String mySessionToken;

    /**
     * Creates a new AWS credentials object.
     *
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AWS secret key
     */
    public AwsCredentials(final String aAccessKey, final String aSecretKey) {
        super(aAccessKey, aSecretKey);
    }

    /**
     * Creates a new AWS credentials object.
     *
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AWS secret key
     * @param aSessionToken An AWS session token
     */
    public AwsCredentials(final String aAccessKey, final String aSecretKey, final String aSessionToken) {
        super(aAccessKey, aSecretKey);
        mySessionToken = aSessionToken;
    }

    @Override
    public String getAccessKey() {
        return super.getAccessKey();
    }

    @Override
    public String getSecretKey() {
        return super.getSecretKey();
    }

    /**
     * Indicates whether the credentials are using a session token.
     *
     * @return True if credentials are using a session token; else, false
     */
    public boolean hasSessionToken() {
        return mySessionToken != null;
    }

    /**
     * Tests whether there is an access and secret key set.
     *
     * @return Whether there is an access and secret key set
     */
    public boolean isValid() {
        return getAccessKey() != null && getSecretKey() != null;
    }

    /**
     * Gets the session token.
     *
     * @return The session token
     */
    public String getSessionToken() {
        return mySessionToken;
    }

    /**
     * Sets a new session token.
     *
     * @param aSessionToken A new session token
     * @return The AWS credentials
     */
    public AwsCredentials setSessionToken(final String aSessionToken) {
        mySessionToken = aSessionToken;
        return this;
    }

}
