package info.freelibrary.vertx.s3.service;

import info.freelibrary.util.Logger;

import info.freelibrary.vertx.s3.util.MessageCodes;

import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;

/**
 * An abstract service exception
 */
abstract class AbstractServiceException extends ServiceException {

    /**
     * The <code>serialVersionUID</code> for a service exception.
     */
    private static final long serialVersionUID = 2435212050179055801L;

    /**
     * Creates a new service exception from the supplied code and exception message.
     *
     * @param aCode An exception code
     * @param aMessage An exception message
     */
    AbstractServiceException(final int aCode, final String aMessage) {
        super(aCode, aMessage);
    }

    /**
     * Creates a new service exception from the supplied code and exception message.
     *
     * @param aCode An exception code
     * @param aMessage An exception message
     * @param aDebugInfo Additional debugging information
     */
    AbstractServiceException(final int aCode, final String aMessage, final JsonObject aDebugInfo) {
        super(aCode, aMessage, aDebugInfo != null ? aDebugInfo : new JsonObject());
    }

    /**
     * Creates a new service exception from another ServiceException.
     *
     * @param aServiceException A ServiceException
     */
    AbstractServiceException(final ServiceException aServiceException) {
        super(aServiceException.failureCode(), aServiceException.getMessage(), aServiceException.getDebugInfo());
    }

    /**
     * Checks a found error code against the expected one, and if they don't match we throw an exception.
     *
     * @param aExpectedCode An expected error code
     * @param aFoundCode A found error code
     */
    protected void checkErrorCode(final int aExpectedCode, final int aFoundCode) {
        if (aFoundCode != aExpectedCode) {
            final String message = getLogger().getMessage(MessageCodes.VSS_028, aFoundCode, aExpectedCode);
            throw new S3ServiceException(ServiceCodes.UNEXPECTED_ERROR, message);
        }
    }

    /**
     * Get a logger for logging exception related information.
     *
     * @return An exception logger
     */
    protected abstract Logger getLogger();

}
