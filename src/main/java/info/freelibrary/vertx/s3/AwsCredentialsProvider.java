
package info.freelibrary.vertx.s3;

import java.util.Optional;

@FunctionalInterface
public interface AwsCredentialsProvider {

    Optional<AwsCredentials> getCredentials();
}
