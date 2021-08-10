
package info.freelibrary.vertx.s3;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * Tests of the S3ObjectData class.
 */
public class S3ObjectDataTest {

    private static final String TEST_FILE = "src/test/resources/green.gif";

    /**
     * Test method for {@link info.freelibrary.vertx.s3.S3ObjectData#S3ObjectData(io.vertx.core.json.JsonObject)}.
     */
    @Test
    public final void testS3ObjectDataJsonObject() {
        final JsonObject json = new JsonObject().put(S3ObjectData.Type.FILE.name(), Buffer.buffer(TEST_FILE));
        final S3ObjectData objectData = new S3ObjectData(json);

        assertEquals(S3ObjectData.Type.FILE, objectData.source());
        // assertEquals(TEST_FILE, objectData.asBuffer(null))
    }

    /**
     * Test method for {@link info.freelibrary.vertx.s3.S3ObjectData#S3ObjectData(java.lang.String)}.
     */
    @Test
    public final void testS3ObjectDataString() {

    }

    /**
     * Test method for {@link info.freelibrary.vertx.s3.S3ObjectData#S3ObjectData(io.vertx.core.buffer.Buffer)}.
     */
    @Test
    public final void testS3ObjectDataBuffer() {

    }

    /**
     * Test method for {@link info.freelibrary.vertx.s3.S3ObjectData#source()}.
     */
    @Test
    public final void testSource() {

    }

    /**
     * Test method for {@link info.freelibrary.vertx.s3.S3ObjectData#asBuffer(io.vertx.core.file.FileSystem)}.
     */
    @Test
    public final void testAsBuffer() {

    }

    /**
     * Test method for {@link info.freelibrary.vertx.s3.S3ObjectData#asFile(io.vertx.core.file.FileSystem)}.
     */
    @Test
    public final void testAsFile() {

    }

    /**
     * Test method for {@link info.freelibrary.vertx.s3.S3ObjectData#toJson()}.
     */
    @Test
    public final void testToJson() {

    }

}
