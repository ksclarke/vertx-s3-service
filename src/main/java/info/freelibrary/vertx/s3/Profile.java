
package info.freelibrary.vertx.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

/**
 * A AWS profile defined in the system credentials file.
 */
public class Profile {

    private final String myProfileName;

    /**
     * Creates a profile from the supplied profile name.
     *
     * @param aProfileName A name of a profile from the system credentials file
     */
    public Profile(final String aProfileName) {
        myProfileName = aProfileName;
    }

    /**
     * Get the credentials for the supplied profile.
     *
     * @return The AWS credentials for this profile
     */
    public AWSCredentials getCredentials() {
        return new ProfileCredentialsProvider(myProfileName).getCredentials();
    }

}
