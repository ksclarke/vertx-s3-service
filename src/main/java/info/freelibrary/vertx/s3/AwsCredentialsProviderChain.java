
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
import info.freelibrary.util.warnings.PMD;
import info.freelibrary.vertx.s3.util.MessageCodes;
import uk.co.lucasweb.aws.v4.signer.SigningException;
import uk.co.lucasweb.aws.v4.signer.functional.Streams;

/**
 * An AWS credentials provider chain.
 */
public class AwsCredentialsProviderChain {

    /**
     * The ENV property for default AWS region.
     */
    static final String AWS_DEFAULT_REGION = "AWS_DEFAULT_REGION";

    /**
     * The ENV property for the AWS access key.
     */
    static final String ACCESS_KEY_ENV_VAR = "AWS_ACCESS_KEY";

    /**
     * The ENV property for the AWS secret key.
     */
    static final String SECRET_KEY_ENV_VAR = "AWS_SECRET_KEY";

    /**
     * The system property for the AWS access key.
     */
    static final String ACCESS_KEY_SYSTEM_PROPERTY = "aws.accessKeyId";

    /**
     * The system property for the AWS secret key.
     */
    static final String SECRET_KEY_SYSTEM_PROPERTY = "aws.secretKey";

    /**
     * The file property for the AWS access key.
     */
    static final String ACCESS_KEY_FILE_PROPERTY = "aws_access_key_id";

    /**
     * The file property for the AWS secret key.
     */
    static final String SECRET_KEY_FILE_PROPERTY = "aws_secret_access_key";

    /**
     * The file property for the AWS session token.
     */
    static final String SESSION_KEY_FILE_PROPERTY = "aws_session_token";

    /**
     * A logger for the AWS credentials provider chain.
     */
    private static final Logger LOGGER =
        LoggerFactory.getLogger(AwsCredentialsProviderChain.class, MessageCodes.BUNDLE);

    /**
     * A profile name recognition pattern.
     */
    private static final String PROFILE_PATTERN = "^\\[{}\\]$";

    /**
     * A delimiter for property name and value.
     */
    private static final String PROPERTY_SPLITTER = "=";

    /**
     * A list of AWS credentials providers.
     */
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
        return myProviders.stream() //
            .flatMap(provider -> Streams.streamopt(provider.getCredentials())) //
            .findFirst().orElseThrow(() -> new SigningException(LOGGER.getMessage(MessageCodes.VSS_010)));
    }

    /**
     * Gets an environment provider.
     *
     * @return An AWS credentials provider
     */
    public AwsCredentialsProvider getEnvironmentalProvider() {
        return environmentProvider();
    }

    /**
     * Gets an environment provider.
     *
     * @return An AWS credentials provider
     */
    private AwsCredentialsProvider environmentProvider() {
        return () -> getAwsCredentials(System.getenv(ACCESS_KEY_ENV_VAR), System.getenv(SECRET_KEY_ENV_VAR));
    }

    /**
     * Gets a system properties provider.
     *
     * @return An AWS credentials provider
     */
    public AwsCredentialsProvider getSystemPropertiesProvider() {
        return systemPropertiesProvider();
    }

    /**
     * Gets a system properties provider.
     *
     * @return An AWS credentials provider
     */
    private AwsCredentialsProvider systemPropertiesProvider() {
        return () -> getAwsCredentials(System.getProperty(ACCESS_KEY_SYSTEM_PROPERTY),
            System.getProperty(SECRET_KEY_SYSTEM_PROPERTY));
    }

    /**
     * Gets a file system configuration provider.
     *
     * @param aProfile A credentials profile
     * @return An AWS credentials provider
     */
    public AwsCredentialsProvider getFileSystemProvider(final String aProfile) {
        return fileSystemProvider(aProfile);
    }

    /**
     * Gets a file system configuration provider.
     *
     * @param aProfile A credentials profile
     * @return An AWS credentials provider
     */
    @SuppressWarnings({ PMD.CYCLOMATIC_COMPLEXITY })
    private AwsCredentialsProvider fileSystemProvider(final String aProfile) { // NOPMD
        final Path path = Paths.get(System.getProperty("user.home"), ".aws/credentials");
        final StringBuilder sessionKey = new StringBuilder();
        final StringBuilder accessKey = new StringBuilder();
        final StringBuilder secretKey = new StringBuilder();

        try {
            boolean inProfile = false;

            if (Files.exists(path)) {
                for (final String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                    final String value = line.trim();

                    if (value.matches(StringUtils.format(PROFILE_PATTERN, aProfile))) {
                        inProfile = true;
                    } else if (value.matches(StringUtils.format(PROFILE_PATTERN, ".*"))) {
                        inProfile = false;
                    } else if (inProfile) {
                        final String key = value.substring(value.indexOf(PROPERTY_SPLITTER) + 1).trim();

                        if (value.startsWith(ACCESS_KEY_FILE_PROPERTY)) {
                            accessKey.replace(0, accessKey.length(), key);
                        } else if (value.startsWith(SECRET_KEY_FILE_PROPERTY)) {
                            secretKey.replace(0, secretKey.length(), key);
                        } else if (value.startsWith(SESSION_KEY_FILE_PROPERTY)) {
                            sessionKey.replace(0, sessionKey.length(), key);
                        }
                    }
                }
            } else {
                LOGGER.info(MessageCodes.VSS_011, path);
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
        }
        if (optAccessKey.isPresent() && optSecretKey.isPresent()) {
            return Optional.of(new AwsCredentials(optAccessKey.get(), optSecretKey.get()));
        }
        return Optional.empty();
    }

}
