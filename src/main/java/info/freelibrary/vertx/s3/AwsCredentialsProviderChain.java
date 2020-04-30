
package info.freelibrary.vertx.s3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import uk.co.lucasweb.aws.v4.signer.SigningException;
import uk.co.lucasweb.aws.v4.signer.functional.Streams;

/**
 * An AWS credentials provider chain.
 */
public class AwsCredentialsProviderChain {

    static final String AWS_DEFAULT_REGION = "AWS_DEFAULT_REGION";

    static final String ACCESS_KEY_ENV_VAR = "AWS_ACCESS_KEY";

    static final String SECRET_KEY_ENV_VAR = "AWS_SECRET_KEY";

    static final String ACCESS_KEY_SYSTEM_PROPERTY = "aws.accessKeyId";

    static final String SECRET_KEY_SYSTEM_PROPERTY = "aws.secretKey";

    static final String ACCESS_KEY_FILE_PROPERTY = "aws_access_key_id";

    static final String SECRET_KEY_FILE_PROPERTY = "aws_secret_access_key";

    static final String SESSION_KEY_FILE_PROPERTY = "aws_session_token";

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsCredentialsProviderChain.class,
            Constants.BUNDLE_NAME);

    private static final String PROFILE_PATTERN = "^\\[{}\\]$";

    private static final String PROPERTY_SPLITTER = "=";

    private final List<AwsCredentialsProvider> myProviders;

    /**
     * Creates a new AWS credentials provider chain.
     */
    public AwsCredentialsProviderChain() {
        this(null);
    }

    /**
     * Creates a new AWS credentials provider chain from the supplied profile.
     *
     * @param aProfile An AWS credentials profile
     */
    public AwsCredentialsProviderChain(final String aProfile) {
        myProviders = new ArrayList<>();

        if (aProfile == null) {
            myProviders.add(systemPropertiesProvider());
            myProviders.add(environmentProvider());
            myProviders.add(fileSystemProvider("default"));
        } else {
            myProviders.add(fileSystemProvider(aProfile));
        }
    }

    /**
     * Gets the appropriate credentials from the provider chain.
     *
     * @return AWS credentials
     */
    public AwsCredentials getCredentials() {
        return myProviders.stream().flatMap(provider -> Streams.streamopt(provider.getCredentials())).findFirst()
                .orElseThrow(() -> new SigningException(LOGGER.getMessage(MessageCodes.VS3_010)));
    }

    /**
     * Gets an environment provider.
     *
     * @return An AWS credentials provider
     */
    AwsCredentialsProvider environmentProvider() {
        return () -> getAwsCredentials(System.getenv(ACCESS_KEY_ENV_VAR), System.getenv(SECRET_KEY_ENV_VAR));
    }

    /**
     * Gets a system properties provider.
     *
     * @return An AWS credentials provider
     */
    AwsCredentialsProvider systemPropertiesProvider() {
        return () -> getAwsCredentials(System.getProperty(ACCESS_KEY_SYSTEM_PROPERTY), System.getProperty(
                SECRET_KEY_SYSTEM_PROPERTY));
    }

    /**
     * Gets a file system configuration provider.
     *
     * @param aProfile A credentials profile
     * @return An AWS credentials provider
     */
    AwsCredentialsProvider fileSystemProvider(final String aProfile) {
        final Path path = Paths.get(System.getProperty("user.home"), ".aws/credentials");
        final StringBuilder sessionKey = new StringBuilder();
        final StringBuilder accessKey = new StringBuilder();
        final StringBuilder secretKey = new StringBuilder();

        try {
            boolean inProfile = false;

            if (Files.exists(path)) {
                for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                    line = line.trim();

                    if (line.matches(StringUtils.format(PROFILE_PATTERN, aProfile))) {
                        LOGGER.debug(MessageCodes.VS3_013, aProfile);
                        inProfile = true;
                    } else if (line.matches(StringUtils.format(PROFILE_PATTERN, ".*"))) {
                        inProfile = false;
                    } else if (inProfile) {
                        if (line.startsWith(ACCESS_KEY_FILE_PROPERTY)) {
                            line = line.substring(line.indexOf(PROPERTY_SPLITTER) + 1).trim();
                            accessKey.replace(0, accessKey.length(), line);
                        } else if (line.startsWith(SECRET_KEY_FILE_PROPERTY)) {
                            line = line.substring(line.indexOf(PROPERTY_SPLITTER) + 1).trim();
                            secretKey.replace(0, secretKey.length(), line);
                        } else if (line.startsWith(SESSION_KEY_FILE_PROPERTY)) {
                            line = line.substring(line.indexOf(PROPERTY_SPLITTER) + 1).trim();
                            sessionKey.replace(0, sessionKey.length(), line);
                        }
                    }
                }
            } else {
                LOGGER.info(MessageCodes.VS3_011, path);
            }
        } catch (final IOException details) {
            LOGGER.error(details, details.getMessage());
        }

        return () -> getAwsCredentials(accessKey.toString(), secretKey.toString(), sessionKey.toString());
    }

    /**
     * Gets the AWS credentials.
     *
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AWS secret key
     * @return An optional AWS credentials
     */
    private Optional<AwsCredentials> getAwsCredentials(final String aAccessKey, final String aSecretKey) {
        return getAwsCredentials(aAccessKey, aSecretKey, null);
    }

    /**
     * Gets the AWS credentials.
     *
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AWS secret key
     * @param aSessionKey A session key
     * @return An optional AWS credentials
     */
    private Optional<AwsCredentials> getAwsCredentials(final String aAccessKey, final String aSecretKey,
            final String aSessionKey) {
        final Optional<String> optAccessKey = Optional.ofNullable(StringUtils.trimToNull(aAccessKey));
        final Optional<String> optSecretKey = Optional.ofNullable(StringUtils.trimToNull(aSecretKey));
        final Optional<String> optSessionKey = Optional.ofNullable(StringUtils.trimToNull(aSessionKey));

        if (optAccessKey.isPresent() && optSecretKey.isPresent() && optSessionKey.isPresent()) {
            return Optional.of(new AwsCredentials(optAccessKey.get(), optSecretKey.get(), optSessionKey.get()));
        } else if (optAccessKey.isPresent() && optSecretKey.isPresent()) {
            return Optional.of(new AwsCredentials(optAccessKey.get(), optSecretKey.get()));
        } else {
            return Optional.empty();
        }
    }

}
