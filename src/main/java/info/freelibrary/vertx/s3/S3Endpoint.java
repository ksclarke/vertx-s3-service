
package info.freelibrary.vertx.s3;

import java.net.URL;

import info.freelibrary.util.MalformedUrlRuntimeException;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * An S3 endpoint in SafeURL form.
 */
@DataObject
public class S3Endpoint extends info.freelibrary.util.SafeURL {

    private static final String URL = "SafeURL";

    /**
     * Creates a new S3 endpoint from the supplied SafeURL.
     *
     * @param aURL A SafeURL representing the S3 endpoint
     */
    public S3Endpoint(final URL aURL) {
        super(aURL);
    }

    /**
     * Creates a new S3 endpoint from the JSON object. The JSON object should just contain one property: SafeURL.
     *
     * @param aJsonObject A JSON encapsulation of the S3 endpoint
     * @throws MalformedUrlRuntimeException If the value in the supplied JsonObject isn't a valid SafeURL
     */
    public S3Endpoint(final JsonObject aJsonObject) throws MalformedUrlRuntimeException {
        super(aJsonObject.getString(URL));
    }

    /**
     * Creates a new S3 endpoint from a SafeURL supplied in string form.
     *
     * @param aString An S3 endpoint SafeURL in string form
     * @throws MalformedUrlRuntimeException If the supplied string isn't a valid SafeURL
     */
    public S3Endpoint(final String aString) throws MalformedUrlRuntimeException {
        super(aString);
    }

    /**
     * Creates a new S3 endpoint from the supplied protocol, host, and file.
     *
     * @param aProtocol A protocol (i.e., "http" or "https")
     * @param aHost A host
     * @param aFile A file
     * @throws MalformedUrlRuntimeException If the supplied values don't combine to form a valid SafeURL
     */
    public S3Endpoint(final String aProtocol, final String aHost, final String aFile)
            throws MalformedUrlRuntimeException {
        super(aProtocol, aHost, aFile);
    }

    /**
     * Gets the S3 endpoint in the form of a JSON object.
     *
     * @return A JSON object representation of the AWS credentials
     */
    public JsonObject toJson() {
        return new JsonObject().put(URL, toString());
    }
}
