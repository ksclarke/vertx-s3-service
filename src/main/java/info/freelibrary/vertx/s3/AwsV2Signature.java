
package info.freelibrary.vertx.s3;

import static info.freelibrary.vertx.s3.Constants.PATH_SEP;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.StringJoiner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import info.freelibrary.util.I18nRuntimeException;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;

/**
 * <p>
 * http://docs.amazonwebservices.com/AmazonS3/latest/dev/RESTAuthentication.html#ConstructingTheAuthenticationHeader
 * </p>
 * <p>
 * Date should look like Thu, 17 Nov 2005 18:49:58 GMT, and must be within 15 min. of S3 server time.
 * </p>
 * <p>
 * Content-MD5 and Content-Type headers are optional.
 * </p>
 */
public class AwsV2Signature implements AwsSignature {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsV2Signature.class, Constants.BUNDLE_NAME);

    private static final String SIGNATURE_PATTERN = "AWS {}:{}";

    /** The date format used for timestamping S3 requests */
    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /** Prefix for the AWS user metadata keys */
    private static final String AWS_NAME_PREFIX = "x-amz-meta-";

    /** Hash-based message authentication code used for signing AWS requests */
    private static final String HASH_CODE = "HmacSHA1";

    /** System-independent end of line */
    private static final String EOL = "\n";

    /** Colon, which is used as a header delimiter **/
    private static final String COLON = ":";

    private static final String EMPTY = "";

    private final AwsCredentials myCredentials;

    private String mySessionToken;

    /**
     * Creates a new AWS v2 signature. It must be used within 15 minutes of its creation.
     *
     * @param aCredentials An AWS credentials object
     */
    public AwsV2Signature(final AwsCredentials aCredentials) {
        myCredentials = aCredentials;
    }

    /**
     * Creates a new AWS v2 signature. It must be used within 15 minutes of its creation.
     *
     * @param aCredentials AWS credentials
     * @param aSessionToken An AWS session token
     */
    public AwsV2Signature(final AwsCredentials aCredentials, final String aSessionToken) {
        mySessionToken = aSessionToken;
        myCredentials = aCredentials;
    }

    /**
     * Set the S3 object for which the signature should be created.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     */
    @Override
    public String getAuthorization(final MultiMap aHeaders, final String aMethod, final String aBucket,
            final String aKey, final byte[] aPayload) {
        final String xamzdate = new SimpleDateFormat(DATE_FORMAT, Locale.US).format(new Date());
        final StringJoiner signedHeaders = new StringJoiner(EOL, EMPTY, EOL);
        final StringBuilder toSign = new StringBuilder();
        final String key = aKey.charAt(0) == '?' ? EMPTY : aKey;
        final Iterator<Entry<String, String>> iterator;

        // Default values for content-type and content-md5 are empty strings
        String contentType = EMPTY;
        String contentMD5 = EMPTY;

        aHeaders.add("X-Amz-Date", xamzdate);
        signedHeaders.add("x-amz-date:" + xamzdate);

        if (!StringUtils.isEmpty(mySessionToken)) {
            aHeaders.add("X-Amz-Security-Token", mySessionToken);
            signedHeaders.add("x-amz-security-token:" + mySessionToken);
        }

        // If we have any user metadata set, add it to the signed headers
        iterator = aHeaders.iterator();

        while (iterator.hasNext()) {
            final Entry<String, String> entry = iterator.next();
            final String headerKey = entry.getKey();

            if (headerKey.startsWith(AWS_NAME_PREFIX)) {
                signedHeaders.add(headerKey + COLON + entry.getValue());
            } else if (headerKey.equalsIgnoreCase(HttpHeaders.CONTENT_MD5.toString())) {
                contentMD5 = entry.getValue();
            } else if (headerKey.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE.toString())) {
                contentType = entry.getValue();
            }
        }

        // Create the authorization string to sign
        toSign.append(aMethod).append(EOL).append(contentMD5).append(EOL).append(contentType).append(EOL).append(EOL);
        toSign.append(signedHeaders).append(PATH_SEP).append(aBucket).append(PATH_SEP).append(key);

        // Create the signed authorization string
        try {
            final String signature = b64SignHmacSha1(myCredentials.getSecretKey(), toSign.toString());
            return StringUtils.format(SIGNATURE_PATTERN, myCredentials.getAccessKey(), signature);
        } catch (final NoSuchAlgorithmException details) {
            final String message = LOGGER.getMessage(MessageCodes.SS3_007, details.getMessage());

            LOGGER.error(details, message);
            throw new I18nRuntimeException(details);
        } catch (final InvalidKeyException details) {
            final String message = LOGGER.getMessage(MessageCodes.SS3_008, details.getMessage());

            LOGGER.error(details, message);
            throw new I18nRuntimeException(details);
        }
    }

    /**
     * Returns a Base64 HmacSha1 signature.
     *
     * @param aAwsSecretKey An AWS secret key
     * @param aCanonicalString A canonical string to encode
     * @return A Base64 HmacSha1 signature
     * @throws NoSuchAlgorithmException If the system doesn't support the encoding algorithm
     * @throws InvalidKeyException If the supplied AWS secret key is invalid
     */
    private static String b64SignHmacSha1(final String aAwsSecretKey, final String aCanonicalString)
            throws NoSuchAlgorithmException, InvalidKeyException {
        final SecretKeySpec signingKey = new SecretKeySpec(aAwsSecretKey.getBytes(), HASH_CODE);
        final Mac mac = Mac.getInstance(HASH_CODE);

        mac.init(signingKey);

        return new String(Base64.getEncoder().encode(mac.doFinal(aCanonicalString.getBytes())));
    }

}
