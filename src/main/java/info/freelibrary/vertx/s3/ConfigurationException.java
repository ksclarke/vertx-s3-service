
package info.freelibrary.vertx.s3;

import info.freelibrary.util.I18nRuntimeException;

/**
 * An I18n aware configuration exception.
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
     * @param aMessageKey An I18n MessageCodes key
     */
    public ConfigurationException(final String aMessageKey) {
        super(Constants.BUNDLE_NAME, aMessageKey);
    }

    /**
     * Creates a new configuration exception from the supplied I18n message key and additional details.
     *
     * @param aMessageKey An I18n MessageCodes key
     * @param aDetailsArray Additional details about the configuration exception
     */
    public ConfigurationException(final String aMessageKey, final Object... aDetailsArray) {
        super(Constants.BUNDLE_NAME, aMessageKey, aDetailsArray);
    }
}
