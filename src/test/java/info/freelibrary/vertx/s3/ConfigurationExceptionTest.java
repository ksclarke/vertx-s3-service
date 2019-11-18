
package info.freelibrary.vertx.s3;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests of ConfigurationException.
 */
public class ConfigurationExceptionTest {

    @Test
    public final void testConfigurationException() {
        assertEquals(null, new ConfigurationException().getMessage());
    }

    @Test
    public final void testConfigurationExceptionString() {
        final String message = "asdf-asdf";

        assertEquals(message, new ConfigurationException(message).getMessage());
    }

    @Test
    public final void testConfigurationExceptionStringObjectArray() {
        final String message = "asdf";

        assertEquals(message + "-1234", new ConfigurationException(message + "-{}", "1234").getMessage());
    }

}
