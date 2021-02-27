
package info.freelibrary.vertx.s3;

/**
 * A public interface for S3 endpoints.
 */
public interface Endpoint {

    /**
     * Returns the URL (in string form) of the endpoint.
     *
     * @return The URL (in string form) of the endpoint
     */
    @Override
    String toString();

    /**
     * Gets the dual-stack version of the endpoint.
     *
     * @return The dual-stack version of the endpoint
     */
    String getDualStack();

    /**
     * Gets the region string for the endpoint.
     *
     * @return The region string for the endpoint
     */
    String getRegion();

    /**
     * Gets a human-friendly name for the endpoint.
     *
     * @return The human-friendly name for the endpoint
     */
    String getLabel();
}
