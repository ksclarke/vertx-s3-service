
package info.freelibrary.vertx.s3;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import info.freelibrary.util.StringUtils;

/**
 * Tests of the S3 endpoint.
 */
public class S3EndpointTest {

    private static final String ENDPOINT_PATTERN = "https://s3.{}.amazonaws.com";

    /**
     * Tests the {@link S3Endpoint#toString()} value.
     */
    @Test
    public final void testToString() {
        for (final S3Endpoint endpoint : S3Endpoint.values()) {
            assertEquals(StringUtils.format(ENDPOINT_PATTERN, endpoint.getRegion()), endpoint.toString());
        }
    }

}
