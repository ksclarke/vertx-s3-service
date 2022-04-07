
package info.freelibrary.vertx.s3;

import info.freelibrary.util.I18nRuntimeException;

import info.freelibrary.vertx.s3.util.MessageCodes;

/**
 * A configuration exception.
 */
public class ConfigurationException extends I18nRuntimeException {

    /**
     * The <code>serialVersionUID</code> for ConfigurationException.
     */
    private static final long serialVersionUID = 8933742820053151915L;

    /**
     * Creates a new generic configuration exception.
     */
    public ConfigurationException() {
        super();
    }

    /**
     * Creates a new configuration exception from the supplied I18n message key.
     *
     * @param aMessageKey A message key
     */
    public ConfigurationException(final String aMessageKey) {
        super(MessageCodes.BUNDLE, aMessageKey);
    }

    /**
     * Creates a new configuration exception from the supplied message key and additional details.
     *
     * @param aMessageKey A message key
     * @param aDetailsArray Additional details about the configuration exception
     */
    public ConfigurationException(final String aMessageKey, final Object... aDetailsArray) {
        super(MessageCodes.BUNDLE, aMessageKey, aDetailsArray);
    }
}
