
package info.freelibrary.vertx.s3.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;

/**
 * An S3 service exception.
 */
public class S3ServiceException extends ServiceException {

    /**
     * The <code>serialVersionUID</code> for an S3 service exception.
     */
    private static final long serialVersionUID = -141348299829570221L;

    /**
     * Creates a new service exception from the supplied code and exception message.
     *
     * @param aCode An exception code
     * @param aMessage An exception message
     */
    public S3ServiceException(final int aCode, final String aMessage) {
        super(aCode, aMessage);
    }

    /**
     * Creates a new service exception from the supplied code and exception message.
     *
     * @param aCode An exception code
     * @param aMessage An exception message
     * @param aDebugInfo Additional debugging information
     */
    public S3ServiceException(final int aCode, final String aMessage, final JsonObject aDebugInfo) {
        super(aCode, aMessage, aDebugInfo);
    }

    /**
     * Wraps an S3 service exception in a failed future.
     *
     * @param aCode A failure code
     * @param aMessage An exception message
     * @return A failed future
     */
    @SuppressWarnings("unchecked")
    public static Future<S3ServiceException> fail(final int aCode, final String aMessage) {
        return Future.failedFuture(new S3ServiceException(aCode, aMessage));
    }

    /**
     * Wraps an S3 service exception in a failed future.
     *
     * @param aCode A failure code
     * @param aMessage An exception message
     * @return A failed future
     */
    @SuppressWarnings("unchecked")
    public static Future<S3ServiceException> fail(final int aCode, final String aMessage, final JsonObject aDebugInfo) {
        return Future.failedFuture(new S3ServiceException(aCode, aMessage, aDebugInfo));
    }

}
