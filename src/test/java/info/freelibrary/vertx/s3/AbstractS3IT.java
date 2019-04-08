
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

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Support for S3 test interactions.
 */
@RunWith(VertxUnitRunner.class)
public abstract class AbstractS3IT {

    /** The test file used in the tests */
    protected static final File TEST_FILE = new File("src/test/resources/green.gif");

    /** AWS access key */
    protected static String myAccessKey;

    /** AWS secret key */
    protected static String mySecretKey;

    /** S3 bucket used in the tests */
    protected static String myTestBucket;

    /** AWS region used in the tests */
    protected static Region myRegion;

    /** Byte array for resource contents */
    protected static byte[] myResource;

    /** The S3 client used to setup some of the tests */
    protected AmazonS3 myS3Client;

    /** The connection to the Vertx framework */
    protected Vertx myVertx;

    /**
     * Static test setup.
     *
     * @param aContext A test context
     */
    @BeforeClass
    public static void setUpBeforeClass(final TestContext aContext) {
        final String endpoint = StringUtils.trimToNull(System.getProperty("vertx.s3.region"));

        myTestBucket = System.getProperty("vertx.s3.bucket", "vertx-pairtree-tests");
        myAccessKey = System.getProperty("vertx.s3.access_key", "YOUR_ACCESS_KEY");
        mySecretKey = System.getProperty("vertx.s3.secret_key", "YOUR_SECRET_KEY");

        try {
            myResource = IOUtils.readBytes(new FileInputStream(TEST_FILE));

            // We use "us-east-1" as the default region
            if (endpoint != null) {
                myRegion = RegionUtils.getRegion(endpoint);
            } else {
                myRegion = RegionUtils.getRegion("us-east-1");
            }
        } catch (final IOException details) {
            aContext.fail(details.getMessage());
        }
    }

    @Before
    public void setUp(final TestContext aContext) {
        if (mySecretKey.equals("YOUR_SECRET_KEY") || myAccessKey.equals("YOUR_ACCESS_KEY")) {
            aContext.fail(getLogger().getMessage(MessageCodes.SS3_002));
        }

        // Initialize the S3 client we use for test set up and tear down
        final AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withCredentials(
                new AWSStaticCredentialsProvider(new BasicAWSCredentials(myAccessKey, mySecretKey)));
        final String endpoint = myRegion.getServiceEndpoint("s3");
        final String regionName = myRegion.getName();

        builder.setEndpointConfiguration(new EndpointConfiguration(endpoint, regionName));

        myS3Client = builder.build();
        myVertx = Vertx.vertx();
    }

    /**
     * Test cleanup deletes everything in the bucket so obviously only use on S3 test buckets.
     *
     * @param aContext A test context
     */
    @After
    public void tearDown(final TestContext aContext) {
        // Clean up our test resources in the S3 bucket
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

    /**
     * Return the logger that's been created in the subclass.
     *
     * @return A logger for logging events
     */
    protected abstract Logger getLogger();

}
