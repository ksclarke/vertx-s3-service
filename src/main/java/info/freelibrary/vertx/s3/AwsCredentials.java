
package info.freelibrary.vertx.s3;

// import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * An AWS credentials object.
 */
// @DataObject
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

    /**
     * Creates a new AWS credentials object from the supplied JSON configuration.
     *
     * @param aJsonConfig A JSON document that contains AccessKey, SecretKey, and SessionToken properties
     */
    public AwsCredentials(final JsonObject aJsonConfig) {
        super(aJsonConfig.getString(AwsCredentialsProviderChain.ACCESS_KEY_FILE_PROPERTY),
            aJsonConfig.getString(AwsCredentialsProviderChain.SECRET_KEY_FILE_PROPERTY));
        mySessionToken = aJsonConfig.getString(AwsCredentialsProviderChain.SESSION_KEY_FILE_PROPERTY);
    }

    /**
     * Gets AWS credentials in the form of a JSON object.
     *
     * @return A JSON object representation of the AWS credentials
     */
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();

        json.put(AwsCredentialsProviderChain.ACCESS_KEY_FILE_PROPERTY, getAccessKey());
        json.put(AwsCredentialsProviderChain.SECRET_KEY_FILE_PROPERTY, getSecretKey());

        if (mySessionToken != null) {
            json.put(AwsCredentialsProviderChain.SESSION_KEY_FILE_PROPERTY, mySessionToken);
        }

        return json;
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
