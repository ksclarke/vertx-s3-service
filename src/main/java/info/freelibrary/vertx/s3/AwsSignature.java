
package info.freelibrary.vertx.s3;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;

/**
 * An AWS authentication signature.
 */
public interface AwsSignature {

    /**
     * Gets the authentication value from the signature. This value can then be put into an &quot;Authentication&quot;
     * header in the S3 request.
     *
     * @param aHeaders HttpHeaders for a request being sent to S3
     * @param aMethod The method of the request being sent to S3
     * @param aPayload The payload to be signed
     * @return The authentication string
     */
    Future<String> getAuthorization(MultiMap aHeaders, String aMethod, Buffer aPayload);

    /**
     * Gets the authentication value from the signature. This value can then be put into an &quot;Authentication&quot;
     * header in the S3 request.
     *
     * @param aHeaders HttpHeaders for a request being sent to S3
     * @param aMethod The method of the request being sent to S3
     * @param aPayload The payload to be signed
     * @return The authentication string
     */
    Future<String> getAuthorization(MultiMap aHeaders, String aMethod, AsyncFile aPayload);

    /**
     * Gets the authentication value from the signature. This value can then be put into an &quot;Authentication&quot;
     * header in the S3 request.
     *
     * @param aHeaders HttpHeaders for a request being sent to S3
     * @param aMethod The method of the request being sent to S3
     * @param aPayload The payload to be signed
     * @return The authentication string
     */
    Future<String> getAuthorization(MultiMap aHeaders, String aMethod, String aPayload);
}
