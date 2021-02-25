
package info.freelibrary.vertx.s3;

import info.freelibrary.util.I18nException;

import info.freelibrary.vertx.s3.util.MessageCodes;

/**
 * An exception thrown when an S3 interaction has an unexpected response.
 */
public class UnexpectedStatusException extends I18nException {

    /**
     * The <code>serialVersionUID</code> of UnexpectedStatusException.
     */
    private static final long serialVersionUID = 8120662372258955206L;

    /**
     * A status code related to the exception.
     */
    private final int myStatusCode;

    /**
     * Creates an S3 exception from the supplied status code and message.
     *
     * @param aStatusCode A status code related to the exception
     * @param aStatusMessage A status message related to the exception
     */
    public UnexpectedStatusException(final int aStatusCode, final String aStatusMessage) {
        super(MessageCodes.BUNDLE, MessageCodes.VSS_017, aStatusCode, aStatusMessage);
        myStatusCode = aStatusCode;
    }

    /**
     * Gets the status code related to this exception.
     *
     * @return The status code related to this exception
     */
    public int getStatusCode() {
        return myStatusCode;
    }

}
