
package info.freelibrary.vertx.s3;

import java.nio.file.Paths;
import java.util.Map;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.utility.DockerImageName;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import io.vertx.ext.unit.Async;

/**
 * An abstract base class for functional tests.
 */
public abstract class AbstractS3FT {

    /* An AWS credentials provider */
    protected static AWSCredentialsProvider myCredentialsProvider;

    /* An endpoint configuration for an S3 client */
    protected static EndpointConfiguration myEndpointConfig;

    /* S3 access key for our LocalStack S3 */
    protected static String myAccessKey;

    /* S3 secret key for our LocalStack S3 */
    protected static String mySecretKey;

    /* An S3 client used to confirm tests have worked */
    protected static AmazonS3 myS3Client;

    /* An S3 endpoint */
    protected static S3Endpoint myEndpoint;

    /* A network alias for the LocalStack S3 service */
    private static final String S3_ALIAS = "s3.localstack";

    /* The user the tests are being run as */
    private static final String TEST_USER = System.getProperty("test.user");

    /* The local tag of our test container */
    private static final String TAG = toTag(System.getProperty(TestConstants.TAG_VERSION));

    /* The local Maven repository cache */
    private static final String LOCAL_M2_REPO = ".m2";

    /* The S3 LocalStack and test containers use the same network */
    private static final Network NETWORK = Network.newNetwork();

    /* The S3 LocalStack container */
    @SuppressWarnings("unused")
    private static final LocalStackContainer S3_CONTAINER = getS3Container();

    /* The test container */
    @SuppressWarnings("unused")
    private static final GenericContainer<?> TEST_CONTAINER = getContainer();

    /**
     * Gets a local S3-compatible container.
     *
     * @return A local S3-compatible container
     */
    private static LocalStackContainer getS3Container() {
        final ClientConfiguration s3ClientConfig = new ClientConfiguration().withProtocol(Protocol.HTTP);
        final DockerImageName localstackImage = DockerImageName.parse("localstack/localstack");
        final LocalStackContainer s3Container = new LocalStackContainer(localstackImage);
        final AmazonS3ClientBuilder builder;
        final AWSCredentials credentials;

        s3Container.withServices(Service.S3).withNetwork(NETWORK).withNetworkAliases(S3_ALIAS).start();
        myEndpointConfig = s3Container.getEndpointConfiguration(Service.S3);
        myCredentialsProvider = s3Container.getDefaultCredentialsProvider();
        builder = AmazonS3ClientBuilder.standard().withCredentials(myCredentialsProvider);
        builder.withClientConfiguration(s3ClientConfig).setEndpointConfiguration(myEndpointConfig);
        myEndpoint = new S3Endpoint(myEndpointConfig.getServiceEndpoint());
        credentials = myCredentialsProvider.getCredentials();
        myAccessKey = credentials.getAWSAccessKeyId();
        mySecretKey = credentials.getAWSSecretKey();
        myS3Client = builder.build();

        return s3Container;
    }

    /**
     * Gets a local test container.
     *
     * @return A local test container
     */
    protected static GenericContainer<?> getContainer() {
        final String localM2RepoCache = Paths.get(System.getProperty("user.home"), LOCAL_M2_REPO).toString();
        final String containerM2RepoCache = Paths.get("/home", TEST_USER, LOCAL_M2_REPO).toString();
        final GenericContainer<?> container = new GenericContainer<>(TAG);
        final AWSCredentials credentials = myCredentialsProvider.getCredentials();
        final Map<String, String> envMap =
                Map.of(AwsCredentialsProviderChain.ACCESS_KEY_ENV_VAR, credentials.getAWSAccessKeyId(),
                        AwsCredentialsProviderChain.SECRET_KEY_ENV_VAR, credentials.getAWSSecretKey(),
                        AwsCredentialsProviderChain.AWS_DEFAULT_REGION, myEndpointConfig.getSigningRegion());

        // Map our local Maven repository cache to our container's so we don't have to re-download everything
        if (!localM2RepoCache.startsWith("/home/travis")) { // This doesn't work on Travis, though
            container.withFileSystemBind(localM2RepoCache, containerM2RepoCache, BindMode.READ_ONLY);
        }

        container.withEnv(envMap).withNetwork(NETWORK).start();

        // Remember to shutdown the NETWORK when we're done with it
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                container.getNetwork().close();
            }
        });

        return container;
    }

    /**
     * Completes an asynchronous task if it hasn't already been completed.
     *
     * @param aAsyncTask
     */
    protected void complete(final Async aAsyncTask) {
        if (!aAsyncTask.isCompleted()) {
            aAsyncTask.complete();
        }
    }

    /**
     * Replaces a SNAPSHOT version with 'latest' for the Docker image tag.
     *
     * @param aVersion An artifact version
     * @return A Docker image tag
     */
    public static String toTag(final String aVersion) {
        final String version = aVersion != null ? aVersion : "0.0.0-SNAPSHOT";
        return System.getProperty(TestConstants.IMAGE_NAME, "vertx-s3-service") + ":" +
                (version.contains("-SNAPSHOT") ? "latest" : version);
    }
}
