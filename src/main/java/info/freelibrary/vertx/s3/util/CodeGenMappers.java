
package info.freelibrary.vertx.s3.util;

import java.util.Optional;

import info.freelibrary.vertx.s3.AwsCredentials;
import info.freelibrary.vertx.s3.AwsProfile;
import info.freelibrary.vertx.s3.S3ClientOptions;

import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

/**
 * A CodeGen mapping utility that helps map specific objects to JSON (and back).
 */
public final class CodeGenMappers {

    private static final String CREDENTIALS = "credentials";

    private static final String PROFILE = "profile";

    private static final String AWS_ACCESS_KEY = "aws_access_key";

    private static final String AWS_SECRET_KEY = "aws_secret_key";

    private static final String AWS_SESSION_TOKEN = "aws_session_token";

    private CodeGenMappers() {
    }

    /**
     * Serializes S3 client options into a JsonObject.
     *
     * @param aConfig A S3 client configuration
     * @return A JSON serialization of the S3 client options
     */
    public static JsonObject serializeS3ClientOptions(final S3ClientOptions aConfig) {
        final JsonObject serialization = aConfig.toJson(); // HttpClientOptions' config
        final Optional<AwsCredentials> credentials = aConfig.getCredentials();
        final Optional<AwsProfile> profile = aConfig.getProfile();

        if (credentials.isPresent()) {
            final AwsCredentials awsCredentials = credentials.get();
            final String awsSessionToken = awsCredentials.getSessionToken();
            final String awsAccessKey = awsCredentials.getAccessKey();
            final String awsSecretKey = awsCredentials.getSecretKey();
            final JsonObject json = new JsonObject();

            if (awsSessionToken != null) {
                json.put(AWS_SESSION_TOKEN, awsSessionToken);
            }

            if (awsAccessKey != null) {
                json.put(AWS_ACCESS_KEY, awsAccessKey);
            }

            if (awsSecretKey != null) {
                json.put(AWS_SECRET_KEY, awsSecretKey);
            }

            if (!json.isEmpty()) {
                serialization.put(CREDENTIALS, json);
            }
        }

        if (profile.isPresent()) {
            serialization.put(PROFILE, profile.get());
        }

        return serialization;
    }

    /**
     * Deserializes an S3ClientOptions object from the supplied JSON serialization.
     *
     * @param aConfig A JSON serialization of the S3 client configuration
     * @return An S3ClientOptions object
     */
    public static S3ClientOptions deserializeS3ClientOptions(final JsonObject aConfig) {
        final S3ClientOptions s3ClientConfig = new S3ClientOptions(new HttpClientOptions(aConfig));

        if (aConfig.containsKey(AWS_ACCESS_KEY) && aConfig.containsKey(AWS_SECRET_KEY)) {
            final String awsAccessKey = aConfig.getString(AWS_ACCESS_KEY);
            final String awsSecretKey = aConfig.getString(AWS_SECRET_KEY);
            final AwsCredentials credentials = new AwsCredentials(awsAccessKey, awsSecretKey);

            if (aConfig.containsKey(AWS_SESSION_TOKEN)) {
                credentials.setSessionToken(awsSecretKey);
            }

            s3ClientConfig.setCredentials(credentials);
        }

        if (aConfig.containsKey(PROFILE)) {
            s3ClientConfig.setProfile(aConfig.getString(PROFILE));
        }

        return s3ClientConfig;
    }
}
