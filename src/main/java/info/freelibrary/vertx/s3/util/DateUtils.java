
package info.freelibrary.vertx.s3.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utilities related to dates and times.
 */
public final class DateUtils {

    /**
     * A date format for S3 objects.
     */
    private static final SimpleDateFormat DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    // Set the timezone for supported date-time strings
    static {
        DATE.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Creates a new Date utilities class.
     */
    private DateUtils() {
        // This is intentionally left empty
    }

    /**
     * Parses an AWS date-time string into an instant.
     *
     * @param aDateTime A date-time string
     * @return An instant
     * @throws ParseException If the supplied string doesn't conform to the expected format.
     */
    public static Instant parse(final String aDateTime) throws ParseException {
        return DATE.parse(aDateTime).toInstant();
    }
}
