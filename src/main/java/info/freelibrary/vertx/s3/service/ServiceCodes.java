
package info.freelibrary.vertx.s3.service;

/**
 * Constant service error codes.
 */
public final class ServiceCodes {

    /**
     * An unexpected service error.
     */
    public static final int UNEXPECTED_ERROR = 1;

    /**
     * The code of a missing key exception.
     */
    public static final int MISSING_KEY_ERROR = 3;

    /**
     * Create a new constants class for service error codes.
     */
    private ServiceCodes() {
        // This is intentionally left empty
    }

}
