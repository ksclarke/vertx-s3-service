
package info.freelibrary.vertx.s3;

import java.util.Iterator;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;

/**
 * Utilities available for tests to use.
 */
@SuppressWarnings("MultipleStringLiterals") // The 'test' AWS key literals
public final class TestUtils {

    /**
     * An AWS access key for testing.
     */
    public static final String AWS_ACCESS_KEY = "test";

    /**
     * An AWS secret key for testing.
     */
    public static final String AWS_SECRET_KEY = "test";

    /**
     * Creates a new test utilities class.
     */
    private TestUtils() {
        // This intentionally left empty
    }

    /**
     * Deletes an S3 bucket and all its contents. This is used for test cleanup.
     *
     * @param aS3Client An AWS S3 client
     * @param aBucketName A bucket name
     */
    public static void deleteBucket(final AmazonS3 aS3Client, final String aBucketName) {
        ObjectListing objectListing = aS3Client.listObjects(aBucketName);
        VersionListing versionListing;

        while (true) {
            final Iterator<S3ObjectSummary> iterator = objectListing.getObjectSummaries().iterator();

            while (iterator.hasNext()) {
                aS3Client.deleteObject(aBucketName, iterator.next().getKey());
            }

            if (!objectListing.isTruncated()) {
                break;
            }

            objectListing = aS3Client.listNextBatchOfObjects(objectListing);
        }

        versionListing = aS3Client.listVersions(new ListVersionsRequest().withBucketName(aBucketName));

        while (true) {
            final Iterator<S3VersionSummary> iterator = versionListing.getVersionSummaries().iterator();

            while (iterator.hasNext()) {
                final S3VersionSummary summary = iterator.next();
                aS3Client.deleteVersion(aBucketName, summary.getKey(), summary.getVersionId());
            }

            if (!versionListing.isTruncated()) {
                break;
            }

            versionListing = aS3Client.listNextBatchOfVersions(versionListing);
        }

        aS3Client.deleteBucket(aBucketName);
    }
}
