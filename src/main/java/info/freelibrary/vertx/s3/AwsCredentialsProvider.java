
package info.freelibrary.vertx.s3;

import java.util.Optional;

/**
 * A simple interface defining the AWS credentials provider API.
 */
@FunctionalInterface
public interface AwsCredentialsProvider {

    /**
     * Gets credentials from the current AWS credentials provider.
     *
     * @return AWS credentials, if they are found
     */
    Optional<AwsCredentials> getCredentials();
}
