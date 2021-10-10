
package info.freelibrary.vertx.s3.service;

import static info.freelibrary.vertx.s3.service.ServiceCodes.MISSING_KEY_ERROR;

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
 * A missing key exception thrown when an S3Object is missing its key.
 */
public class MissingKeyException extends S3ServiceException {

    /**
     * The missing key exception logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MissingKeyException.class, MessageCodes.BUNDLE);

    /**
     * The missing key exception's <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = 8991712732390980726L;

    /**
     * Creates a new missing key exception.
     *
     * @param aMessage A message exception
     */
    public MissingKeyException(final String aMessage) {
        super(MISSING_KEY_ERROR, aMessage);
    }

    /**
     * Creates a new missing key exception.
     *
     * @param aMessage A message exception
     * @param aDebugInfo Additional details about the missing key exception
     */
    public MissingKeyException(final String aMessage, final JsonObject aDebugInfo) {
        super(MISSING_KEY_ERROR, aMessage, aDebugInfo);
    }

    /**
     * Creates a new missing key exception.
     *
     * @param aServiceException A service exception
     */
    public MissingKeyException(final ServiceException aServiceException) {
        super(aServiceException);
        checkErrorCode(MISSING_KEY_ERROR, aServiceException.failureCode());
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
     * Wraps a missing key exception in a failed future.
     *
     * @param aMessage An exception message
     * @return A failed future
     */
    public static Future<ServiceException> fail(final String aMessage) {
        return Future.failedFuture(new MissingKeyException(aMessage));
    }

    /**
     * Wraps a missing key exception in a failed future.
     *
     * @param aMessage An exception message
     * @param aDebugInfo Additional details about the failure
     * @return A failed future
     */
    public static Future<ServiceException> fail(final String aMessage, final JsonObject aDebugInfo) {
        return Future.failedFuture(new MissingKeyException(aMessage, aDebugInfo));
    }

    /**
     * A message codec for sending MissingKeyException(s) over the Vert.x event bus.
     */
    static class MissingKeyExceptionMessageCodec implements MessageCodec<MissingKeyException, MissingKeyException> {

        /**
         * The underlying ServiceExceptionMessageCodec (that does all the work for us).
         */
        private final ServiceExceptionMessageCodec myDelegateCodec = new ServiceExceptionMessageCodec();

        @Override
        public void encodeToWire(final Buffer aBuffer, final MissingKeyException aServiceException) {
            myDelegateCodec.encodeToWire(aBuffer, aServiceException);
        }

        @Override
        public MissingKeyException decodeFromWire(final int aPosition, final Buffer aBuffer) {
            return new MissingKeyException(myDelegateCodec.decodeFromWire(aPosition, aBuffer));
        }

        @Override
        public MissingKeyException transform(final MissingKeyException aServiceException) {
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
