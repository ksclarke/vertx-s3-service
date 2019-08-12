
package info.freelibrary.vertx.s3;

/**
 * HTTP constants defined for use by the Pairtree library.
 */
public final class HTTP {

    /** OK HTTP response code */
    public static final int OK = 200;

    /** Not Found HTTP response code */
    public static final int NOT_FOUND = 404;

    /** No content HTTP response code */
    public static final int NO_CONTENT = 204;

    /** Forbidden HTTP response code */
    public static final int FORBIDDEN = 403;

    /** Content-Length HTTP header */
    public static final String CONTENT_LENGTH = "Content-Length";

    /** HTTP GET request */
    public static final String GET = "GET";

    /** HTTP LIST request */
    public static final String LIST = "LIST";

    /** HTTP PUT request */
    public static final String PUT = "PUT";

    /** HTTP DELETE request */
    public static final String DELETE = "DELETE";

    /** HTTP POST request */
    public static final String POST = "POST";

    /** HTTP HEAD request */
    public static final String HEAD = "HEAD";

    /** An empty private constructor for this utility class */
    private HTTP() {
    }
}
