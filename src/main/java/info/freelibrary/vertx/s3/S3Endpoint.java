
package info.freelibrary.vertx.s3;

import static info.freelibrary.util.Constants.COLON;
import static info.freelibrary.util.Constants.SLASH;

import java.net.URI;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * An S3 endpoint.
 */
@DataObject
public class S3Endpoint {

    public static final String PROPERTY = "endpoint";

    private static final String SCHEME_DELIM = COLON + SLASH + SLASH;

    private final URI myURI;

    /**
     * Creates a new S3 endpoint from the supplied URI.
     *
     * @param aURI A URI representing the S3 endpoint
     */
    public S3Endpoint(final URI aURI) {
        myURI = aURI;
    }

    /**
     * Creates a new S3 endpoint from a URI supplied in string form.
     *
     * @param aString An S3 endpoint URI in string form
     */
    public S3Endpoint(final String aString) {
        myURI = URI.create(aString);
    }

    /**
     * Creates a new S3 endpoint from the supplied protocol, host, and file.
     *
     * @param aProtocol A protocol (i.e., "http" or "https")
     * @param aHost A host
     * @param aFile A file
     */
    public S3Endpoint(final String aProtocol, final String aHost, final String aFile) {
        myURI = URI.create(aProtocol + SCHEME_DELIM + aHost + SLASH + aFile);
    }

    /**
     * Creates a new endpoint from a JSON serialization.
     */
    @SuppressWarnings("unused")
    private S3Endpoint(final JsonObject aJsonObject) {
        myURI = URI.create(aJsonObject.getString(PROPERTY));
    }

    /**
     * Gets the host of this S3 endpoint.
     *
     * @return The S3 endpoint host
     */
    public String getHost() {
        return myURI.getHost();
    }

    /**
     * Gets the protocol of this S3 endpoint.
     *
     * @return The S3 endpoint protocol
     */
    public String getProtocol() {
        return myURI.getScheme();
    }

    /**
     * Gets the port of this S3 endpoint.
     *
     * @return The S3 endpoint port
     */
    public int getPort() {
        return myURI.getPort();
    }

    /**
     * Gets the S3 endpoint in the form of a JSON object.
     *
     * @return A JSON object representation of the AWS credentials
     */
    public JsonObject toJson() {
        return new JsonObject().put(PROPERTY, toString());
    }

}
