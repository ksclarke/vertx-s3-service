
package info.freelibrary.vertx.s3;

/**
 * Constants defined for use by the Vert.x S3 library.
 */
public final class Constants {

    /** A bundle name for I18N messages */
    public static final String BUNDLE_NAME = "vertx-s3_messages";

    public static final String S3_ACCESS_KEY = "vertx.s3.access_key";

    public static final String S3_SECRET_KEY = "vertx.s3.secret_key";

    public static final String S3_BUCKET = "vertx.s3.bucket";

    public static final String S3_REGION = "vertx.s3.region";

    /** A system independent path separator */
    public static final char PATH_SEP = '/';

    /** An empty private constructor for this utility class */
    private Constants() {
    }

}
