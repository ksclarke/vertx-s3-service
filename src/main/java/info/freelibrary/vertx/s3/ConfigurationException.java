
package info.freelibrary.vertx.s3;

import info.freelibrary.util.I18nRuntimeException;

public class ConfigurationException extends I18nRuntimeException {

    /**
     * The <code>serialVersionUID</code> for ConfigurationException.
     */
    private static final long serialVersionUID = 8933742820053151915L;

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(final String aMessageKey) {
        super(Constants.BUNDLE_NAME, aMessageKey);
    }

    public ConfigurationException(final String aMessageKey, final Object... aDetailsArray) {
        super(Constants.BUNDLE_NAME, aMessageKey, aDetailsArray);
    }
}
