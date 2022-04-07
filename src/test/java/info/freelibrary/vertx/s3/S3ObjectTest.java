
package info.freelibrary.vertx.s3;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * Tests of the S3DataObject class.
 */
public class S3ObjectTest {

    private static final String TEST_FILE = "src/test/resources/green.gif";

    /**
     * Test method for {@link info.freelibrary.vertx.s3.S3DataObject#S3ObjectData(io.vertx.core.json.JsonObject)}.
     */
    @Test
    public final void testS3ObjectDataJsonObject() {
        final JsonObject json = new JsonObject().put(S3Object.Type.FILE.name(), Buffer.buffer(TEST_FILE));
        final S3Object objectData = new S3Object(json);

        assertEquals(S3Object.Type.FILE, objectData.getSource());
        // assertEquals(TEST_FILE, objectData.asBuffer(null))
    }

    /**
     * Test method for {@link info.freelibrary.vertx.s3.S3DataObject#S3ObjectData(java.lang.String)}.
     */
    @Test
    public final void testS3ObjectDataString() {

    }

    /**
     * Test method for {@link info.freelibrary.vertx.s3.S3DataObject#S3ObjectData(io.vertx.core.buffer.Buffer)}.
     */
    @Test
    public final void testS3ObjectDataBuffer() {

    }

    /**
     * Test method for {@link info.freelibrary.vertx.s3.S3DataObject#source()}.
     */
    @Test
    public final void testSource() {

    }

    /**
     * Test method for {@link info.freelibrary.vertx.s3.S3DataObject#asBuffer(io.vertx.core.file.FileSystem)}.
     */
    @Test
    public final void testAsBuffer() {

    }

    /**
     * Test method for {@link info.freelibrary.vertx.s3.S3DataObject#asFile(io.vertx.core.file.FileSystem)}.
     */
    @Test
    public final void testAsFile() {

    }

    /**
     * Test method for {@link info.freelibrary.vertx.s3.S3DataObject#toJson()}.
     */
    @Test
    public final void testToJson() {

    }

}
