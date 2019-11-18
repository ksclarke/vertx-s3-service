
package info.freelibrary.vertx.s3;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map.Entry;

import info.freelibrary.util.I18nRuntimeException;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import uk.co.lucasweb.aws.v4.signer.HttpRequest;
import uk.co.lucasweb.aws.v4.signer.Signer;

public class AwsV4Signature implements AwsSignature {

    private static final String DATE_TIME_FORMAT = "yyyyMMdd'T'HHmmss'Z'";

    /** The date format used for timestamping requests */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)
            .withZone(ZoneOffset.UTC);

    private static final String DIGEST_ALGORITHM = "SHA-256";

    private static final String HOST = "Host";

    private static final String X_AMZ_DATE = "X-Amz-Date";

    private static final String X_AMZ_CONTENT_SHA256 = "x-amz-content-sha256";

    /** Prefix for the AWS user metadata keys */
    private static final String AWS_NAME_PREFIX = "x-amz-meta-";

    private final AwsCredentials myCredentials;

    private final URI myHost;

    /**
     * Creates a new AWS v4 signature. It must be used within 15 minutes of its creation.
     *
     * @param aHost An S3 host
     * @param aCredentials An AWS credentials
     */
    public AwsV4Signature(final URI aHost, final AwsCredentials aCredentials) {
        myCredentials = aCredentials;
        myHost = aHost;
    }

    /**
     * Creates a new AWS v4 signature. It must be used within 15 minutes of its creation.
     *
     * @param aHost An S3 host
     * @param aCredentials An AWS credentials
     * @param aSessionToken An S3 token session
     */
    public AwsV4Signature(final URI aHost, final AwsCredentials aCredentials, final String aSessionToken) {
        myCredentials = aCredentials;
        myHost = aHost;
    }

    @Override
    public String getAuthorization(final MultiMap aHeaders, final String aMethod, final String aBucket,
            final String aKey, final byte[] aPayload) {
        try {
            final LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
            final String timestamp = localDateTime.format(DATE_TIME_FORMATTER);
            final HttpRequest request = new HttpRequest(aMethod, myHost);
            final Signer.Builder signer = Signer.builder().awsCredentials(myCredentials);
            final MessageDigest digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
            final String sha256Hex = hashToHex(digest.digest(aPayload));
            final Iterator<Entry<String, String>> iterator;

            if (myCredentials != null && myCredentials.hasSessionToken()) {
                final String sessionToken = myCredentials.getSessionToken();

                aHeaders.add("X-Amz-Security-Token", sessionToken);
                signer.header("x-amz-security-token", sessionToken);
            }

            // If we have any user metadata set, add it to the signed headers
            iterator = aHeaders.iterator();

            while (iterator.hasNext()) {
                final Entry<String, String> entry = iterator.next();
                final String headerKey = entry.getKey();

                if (headerKey.startsWith(AWS_NAME_PREFIX)) {
                    signer.header(headerKey, entry.getValue());
                } else if (headerKey.equalsIgnoreCase(HttpHeaders.CONTENT_MD5.toString())) {
                    // contentMD5 = entry.getValue();
                } else if (headerKey.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE.toString())) {
                    // contentType = entry.getValue();
                }
            }

            signer.header(HOST, myHost.getHost());
            aHeaders.add(HOST, myHost.getHost());
            signer.header(X_AMZ_DATE, timestamp);
            aHeaders.add(X_AMZ_DATE, timestamp);
            signer.header(X_AMZ_CONTENT_SHA256, sha256Hex);
            aHeaders.add(X_AMZ_CONTENT_SHA256, sha256Hex);

            return signer.buildS3(request, sha256Hex).getSignature();
        } catch (final NoSuchAlgorithmException details) {
            throw new I18nRuntimeException(details);
        }
    }

    /**
     * Converts the supplied hash to hex.
     *
     * @param aEncodedHash A hash to be converted into hex format
     * @return The supplied hash in hex format
     */
    private String hashToHex(final byte[] aEncodedHash) {
        final StringBuilder hexString = new StringBuilder();

        for (int index = 0; index < aEncodedHash.length; index++) {
            final String hex = Integer.toHexString(0xff & aEncodedHash[index]);

            if (hex.length() == 1) {
                hexString.append('0');
            }

            hexString.append(hex);
        }

        return hexString.toString();
    }
}
