
package info.freelibrary.vertx.s3.service;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import info.freelibrary.vertx.s3.util.MessageCodes;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;

/**
 * A general S3 service exception.
 */
public class S3ServiceException extends AbstractServiceException {

    /**
     * A service exception logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(S3ServiceException.class, MessageCodes.BUNDLE);

    /**
     * The <code>serialVersionUID</code> for an S3 service exception.
     */
    private static final long serialVersionUID = -141348299829570221L;

    /**
     * Creates a new S3 service exception from the supplied code and exception message.
     *
     * @param aCode An exception code
     * @param aMessage An exception message
     */
    public S3ServiceException(final int aCode, final String aMessage) {
        super(aCode, aMessage);
    }

    /**
     * Creates a new S3 service exception from the supplied code and exception message.
     *
     * @param aCode An exception code
     * @param aMessage An exception message
     * @param aDebugInfo Additional debugging information
     */
    public S3ServiceException(final int aCode, final String aMessage, final JsonObject aDebugInfo) {
        super(aCode, aMessage, aDebugInfo);
    }

    /**
     * Creates a new S3 service exception from another more generic ServiceException.
     *
     * @param aServiceException The more generic ServiceException
     */
    public S3ServiceException(final ServiceException aServiceException) {
        super(aServiceException);
    }

    /**
     * Gets the exception logger.
     *
     * @return An exception logger
     */
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    /**
     * Wraps an S3 service exception in a failed future.
     *
     * @param aCode A failure code
     * @param aMessage An exception message
     * @return A failed future
     */
    @SuppressWarnings("unchecked")
    public static Future<ServiceException> fail(final int aCode, final String aMessage) {
        return Future.failedFuture(new S3ServiceException(aCode, aMessage));
    }

    /**
     * Wraps an S3 service exception in a failed future.
     *
     * @param aCode A failure code
     * @param aMessage An exception message
     * @param aDebugInfo Additional details about the failure
     * @return A failed future
     */
    @SuppressWarnings("unchecked")
    public static Future<ServiceException> fail(final int aCode, final String aMessage, final JsonObject aDebugInfo) {
        return Future.failedFuture(new S3ServiceException(aCode, aMessage, aDebugInfo));
    }

    /**
     * A message codec for sending S3ServiceException(s) over the Vert.x event bus.
     */
    static class S3ServiceExceptionMessageCodec implements MessageCodec<S3ServiceException, S3ServiceException> {

        /**
         * The underlying ServiceExceptionMessageCodec (that does all the work for us).
         */
        private final ServiceExceptionMessageCodec myDelegateCodec = new ServiceExceptionMessageCodec();

        @Override
        public void encodeToWire(final Buffer aBuffer, final S3ServiceException aServiceException) {
            myDelegateCodec.encodeToWire(aBuffer, aServiceException);
        }

        @Override
        public S3ServiceException decodeFromWire(final int aPosition, final Buffer aBuffer) {
            return new S3ServiceException(myDelegateCodec.decodeFromWire(aPosition, aBuffer));
        }

        @Override
        public S3ServiceException transform(final S3ServiceException aServiceException) {
            return aServiceException;
        }

        @Override
        public String name() {
            return getClass().getSimpleName();
        }

        @Override
        public byte systemCodecID() {
            return -1; // Always -1 (the Vert.x examples say)
        }

    }
}
