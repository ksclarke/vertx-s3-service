
package info.freelibrary.vertx.s3;

import static info.freelibrary.util.Constants.EMPTY;
import static info.freelibrary.util.Constants.INADDR_ANY;
import static info.freelibrary.util.Constants.SPACE;
import static io.vertx.core.http.HttpMethod.GET;

import java.io.IOException;
import java.util.Scanner;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;
import info.freelibrary.vertx.s3.util.MessageCodes;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * A test that runs the profile tests (S3ClientProfilesTest) in a Docker container.
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientProfilesFT extends AbstractS3FT {

    /**
     * Logger for the S3ClientProfiles functional test.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientProfilesFT.class, MessageCodes.BUNDLE);

    /**
     * The Maven log web-server port property.
     */
    private static final String MAVEN_PORT_PROPERTY = "maven.port";

    /**
     * A default port for the Maven log server.
     */
    private static final String DEFAULT_MAVEN_PORT = "9999";

    /**
     * The expected path of the expected Maven log.
     */
    private static final String LOG_FILE = "/maven.log";

    /**
     * The delimiter that marks the end of a failed log file.
     */
    private static final String LOG_FILE_END = "[INFO] BUILD FAILURE";

    /**
     * The delimiter that indicates an error in the log file.
     */
    private static final String LOG_FILE_ERROR = "[ERROR] ";

    /**
     * The Vert.x testing context.
     */
    @Rule
    public RunTestOnContext myContext = new RunTestOnContext();

    /**
     * Runs the profile tests and returns their output into the main Maven log stream.
     *
     * @param aContext A test context
     * @throws InterruptedException If the test is interrupted
     * @throws IOException If there an I/O throw while running the container tests
     */
    @Test
    public void checkContainerTests(final TestContext aContext) throws InterruptedException, IOException {
        final int port = Integer.parseInt(System.getProperty(MAVEN_PORT_PROPERTY, DEFAULT_MAVEN_PORT));
        final HttpClient client = myContext.vertx().createHttpClient();
        final Async asyncTask = aContext.async();

        client.request(GET, port, INADDR_ANY, LOG_FILE).compose(request -> request.send().onComplete(response -> {
            if (response.succeeded()) {
                response.result().body() //
                    .onSuccess(body -> testBody(body.toString(), asyncTask)) //
                    .onFailure(details -> aContext.fail(details));
            } else {
                aContext.fail(response.cause());
            }
        }));
    }

    /**
     * Tests the body of a log file query.
     *
     * @param aBody An HTTP response body
     * @param aAsyncTask An asynchronous task
     */
    private void testBody(final String aBody, final Async aAsyncTask) {
        try (Scanner scanner = new Scanner(aBody)) {
            boolean reading = true;

            while (reading && scanner.hasNextLine()) {
                reading = readLine(scanner.nextLine(), scanner);
            }
        } finally {
            aAsyncTask.complete();
        }
    }

    /**
     * Reads a line from the log file.
     *
     * @param aLine A log file line
     * @param aScanner The scanner that's reading the log file
     * @return True if reading should continue; else, false
     */
    private boolean readLine(final String aLine, final Scanner aScanner) {
        if (aLine.startsWith(LOG_FILE_ERROR)) {
            LOGGER.error(aLine.replace(LOG_FILE_ERROR, EMPTY));

            while (aScanner.hasNextLine()) {
                final String line = aScanner.nextLine().trim();

                // If we're not reading a stack trace, continue
                if (line.startsWith("[")) {
                    return readLine(line, aScanner);
                }

                // Do a little extra formatting to distinguish the output
                if (line.startsWith("at")) {
                    LOGGER.error(StringUtils.padStart(line, SPACE, 4));
                } else {
                    LOGGER.error(StringUtils.padStart(line, SPACE, 2));
                }
            }
        } else if (aLine.startsWith(LOG_FILE_END)) {
            return false; // Break as soon as we can
        }

        return true;
    }
}
