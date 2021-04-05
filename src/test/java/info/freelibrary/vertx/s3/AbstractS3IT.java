
package info.freelibrary.vertx.s3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import info.freelibrary.util.IOUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.StringUtils;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Support for S3 test interactions.
 */
@RunWith(VertxUnitRunner.class)
public abstract class AbstractS3IT {

    /** The test file used in the tests */
    protected static final File TEST_FILE = new File("src/test/resources/green.gif");

    /** A sample AWS Access Key */
    protected static final String YOUR_ACCESS_KEY = "YOUR_ACCESS_KEY";

    /** A sample AWS Secret Key */
    protected static final String YOUR_SECRET_KEY = "YOUR_SECRET_KEY";

    /** S3 bucket used in the tests */
    protected static String myTestBucket;

    /** AWS region used in the tests */
    protected static Region myRegion;

    /** Byte array for resource contents */
    protected static byte[] myResource;

    /** The S3 client used to setup some of the tests */
    protected AmazonS3 myS3Client;

    /**
     * Static test setup.
     *
     * @param aContext A test context
     */
    @BeforeClass
    public static void setUpBeforeClass(final TestContext aContext) {
        final String endpoint = StringUtils.trimToNull(System.getProperty(TestConstants.S3_REGION));

        try {
            myTestBucket = StringUtils.trimToNull(System.getProperty(TestConstants.S3_BUCKET));
            myResource = IOUtils.readBytes(new FileInputStream(TEST_FILE));

            if (endpoint != null) {
                myRegion = RegionUtils.getRegion(endpoint);
            } else {
                // Use "us-east-1" as default region if needed
                myRegion = RegionUtils.getRegion("us-east-1");
            }
        } catch (final IOException details) {
            aContext.fail(details.getMessage());
        }
    }

    /**
     * Set up the testing environment.
     *
     * @param aContext A testing context
     */
    @Before
    public void setUp(final TestContext aContext) {
        final AwsCredentials creds = new AwsCredentialsProviderChain(TestConstants.TEST_PROFILE).getCredentials();
        final String accessKey = creds.getAccessKey();
        final String secretKey = creds.getSecretKey();
        final BasicAWSCredentials basicCredentials = new BasicAWSCredentials(accessKey, secretKey);
        final AWSCredentialsProvider provider = new AWSStaticCredentialsProvider(basicCredentials);
        final AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withCredentials(provider);
        final String endpoint = myRegion.getServiceEndpoint("s3");
        final String regionName = myRegion.getName();

        builder.setEndpointConfiguration(new EndpointConfiguration(endpoint, regionName));

        myS3Client = builder.build();
    }

    /**
     * Test cleanup deletes everything in the bucket so obviously only use on S3 test buckets.
     *
     * @param aContext A test context
     */
    @After
    public void tearDown(final TestContext aContext) {
        if (myS3Client != null) {
            final ObjectListing listing = myS3Client.listObjects(myTestBucket);
            final Iterator<S3ObjectSummary> iterator = listing.getObjectSummaries().iterator();

            while (iterator.hasNext()) {
                final String key = iterator.next().getKey();

                try {
                    myS3Client.deleteObject(myTestBucket, key);
                } catch (final AmazonClientException details) {
                    aContext.fail(details);
                }
            }
        }
    }

    /**
     * Completes an uncompleted {@link Async} task.
     *
     * @param aAsyncTask An asynchronous task to be completed.
     */
    protected void complete(final Async aAsyncTask) {
        if (!aAsyncTask.isCompleted()) {
            aAsyncTask.complete();
        }
    }

    /**
     * Return the logger that's been created in the subclass.
     *
     * @return A logger for logging events
     */
    protected abstract Logger getLogger();

}
