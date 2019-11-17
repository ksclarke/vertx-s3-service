
package info.freelibrary.vertx.s3;

import io.vertx.core.MultiMap;

/**
 * An AWS authentication signature.
 */
public interface AwsSignature {

    /**
     * Gets the authentication value from the signature. This value can then be put into an &quot;Authentication&quot;
     * header in the S3 request.
     *
     * @param aHeaders Headers for a request being sent to S3
     * @param aMethod The method of the request being sent to S3
     * @param aBucket A S3 bucket
     * @param aKey The key of an S3 object
     * @param aPayload The payload to be signed
     * @return The authentication string
     */
    String getAuthorization(MultiMap aHeaders, String aMethod, String aBucket, String aKey, byte[] aPayload);

}
