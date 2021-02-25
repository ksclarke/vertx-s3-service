
package info.freelibrary.vertx.s3;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.Container.ExecResult;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import info.freelibrary.vertx.s3.util.MessageCodes;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * A test that runs the profile tests (S3ClientProfilesTest) in a Docker container.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientProfilesFT extends AbstractS3FT {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientProfilesFT.class, MessageCodes.BUNDLE);

    private static final String EOL = "\\r?\\n";

    private static final String EMPTY = "";

    private static final String TAB = "\\t";

    private static final String SPACE = System.lineSeparator() + "    ";

    /**
     * Runs the profile tests and returns their output into the main Maven log stream.
     *
     * @param aContext A test context
     * @throws InterruptedException If the test is interrupted
     * @throws IOException If there an I/O throw while running the container tests
     */
    @Test
    public void runContainerTests(final TestContext aContext) throws InterruptedException, IOException {
        final ExecResult result = getContainer().execInContainer("mvn", "-B", "-PprofileTests,!test", "test",
                "-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn");
        final int index;

        String stdOut = result.getStdout();

        // Output this on success or failure
        if (StringUtils.trimToNull(stdOut) != null) {
            stdOut = stdOut.replaceAll(EOL, EMPTY); // Comes out of the container with some weird EOLs
            index = stdOut.indexOf("[INFO] --- maven-surefire-plugin");

            // Isolate the testing logs
            if (index != -1) {
                stdOut = stdOut.substring(index);
            }

            // Do our own formatting to make it look a little nicer in the nested Maven output
            stdOut = stdOut.replaceAll("\\[INFO\\]", System.lineSeparator() + "  [INFO]");
            stdOut = stdOut.replaceAll("\\[ERROR\\]", System.lineSeparator() + "  [ERROR]");
            stdOut = stdOut.replaceAll(TAB, SPACE);

            LOGGER.info(MessageCodes.VSS_013, stdOut);
        }

        if (result.getExitCode() != 0) {
            final String stdErr = result.getStderr();

            if (StringUtils.trimToNull(stdErr) != null) {
                LOGGER.error("STDERR: {}", stdErr.replaceAll(EOL, EMPTY).replaceAll(TAB, SPACE));
            }

            aContext.fail();
        }
    }
}
