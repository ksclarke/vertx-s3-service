
package info.freelibrary.vertx.s3;

import static info.freelibrary.vertx.s3.LocalStackEndpoint.PORT_PROPERTY;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import info.freelibrary.util.Constants;
import info.freelibrary.util.StringUtils;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.junit.RunTestOnContext;

/**
 * An abstract base class for functional tests.
 */
public abstract class AbstractS3FT {

    /**
     * The default LocalStack port in string form.
     */
    private static final String DEFAULT_PORT = "4566";

    /**
     * A test rule to run the tests on the Vert.x context.
     */
    @Rule
    public final RunTestOnContext myContext = new RunTestOnContext();

    /**
     * The test's S3 bucket.
     */
    protected String myBucket;

    /**
     * The test's S3 object key.
     */
    protected String myKey;

    /**
     * An AWS S3 client.
     */
    protected AmazonS3 myAwsS3Client;

    /**
     * Sets up the testing environment.
     */
    @Before
    public void setUp() {
        final String port = System.getProperty(LocalStackEndpoint.PORT_PROPERTY, DEFAULT_PORT);
        final String host = StringUtils.format(LocalStackEndpoint.ENDPOINT_PATTERN, Constants.INADDR_ANY, port);
        final AWSCredentials credentials = new BasicAWSCredentials(TestUtils.AWS_ACCESS_KEY, TestUtils.AWS_SECRET_KEY);
        final AWSCredentialsProvider credsProvider = new AWSStaticCredentialsProvider(credentials);
        final EndpointConfiguration endpoint = new EndpointConfiguration(host, LocalStackEndpoint.REGION);
        final AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();

        // The official S3 client that we use for testing ours
        myAwsS3Client = builder.withEndpointConfiguration(endpoint).withCredentials(credsProvider).build();

        // The bucket and S3 object key setup
        myBucket = UUID.randomUUID().toString();
        myKey = UUID.randomUUID().toString();
        myAwsS3Client.createBucket(myBucket);
    }

    /**
     * Tears down the testing environment.
     */
    @After
    public void tearDown() {
        TestUtils.deleteBucket(myAwsS3Client, myBucket);
    }

    /**
     * Completes an asynchronous task if it hasn't already been completed.
     *
     * @param aAsyncTask A task to complete
     */
    protected void complete(final Async aAsyncTask) {
        if (!aAsyncTask.isCompleted()) {
            aAsyncTask.complete();
        }
    }

    /**
     * Gets the S3 client configuration.
     *
     * @return The S3 client's options
     */
    protected S3ClientOptions getConfig() {
        final int port = Integer.parseInt(System.getProperty(PORT_PROPERTY, DEFAULT_PORT));
        final Endpoint myEndpoint = new LocalStackEndpoint(port);
        final AwsCredentials myCredentials = new AwsCredentials(TestUtils.AWS_ACCESS_KEY, TestUtils.AWS_SECRET_KEY);

        return new S3ClientOptions(myEndpoint).setCredentials(myCredentials);
    }
}
