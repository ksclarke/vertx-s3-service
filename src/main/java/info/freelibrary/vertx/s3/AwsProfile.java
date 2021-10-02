
package info.freelibrary.vertx.s3;

/**
 * A AWS profile defined in the system credentials file.
 */
public class AwsProfile {

    /**
     * The profile name.
     */
    private final String myProfileName;

    /**
     * Creates a profile from the supplied profile name.
     *
     * @param aProfileName A name of a profile from the system credentials file
     */
    public AwsProfile(final String aProfileName) {
        myProfileName = aProfileName;
    }

    /**
     * Get the credentials for the supplied profile.
     *
     * @return The AWS credentials for this profile
     */
    public AwsCredentials getCredentials() {
        return new AwsCredentialsProviderChain(myProfileName).getCredentials();
    }
}
