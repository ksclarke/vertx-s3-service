
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
import java.util.concurrent.atomic.AtomicInteger;

import info.freelibrary.util.Constants;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpHeaders;
import uk.co.lucasweb.aws.v4.signer.HttpRequest;
import uk.co.lucasweb.aws.v4.signer.Signer;

/**
 * An AWS signature that conforms to the version four specification.
 */
public class AwsV4Signature implements AwsSignature {

    /**
     * The date-time format.
     */
    private static final String DATE_TIME_FORMAT = "yyyyMMdd'T'HHmmss'Z'";

    /**
     * The date format used for timestamping requests
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).withZone(ZoneOffset.UTC);

    /**
     * The signature's digest algorithm.
     */
    private static final String DIGEST_ALGORITHM = "SHA-256";

    /**
     * The signature's host header.
     */
    private static final String HOST = "Host";

    /**
     * The AMZ date header.
     */
    private static final String X_AMZ_DATE = "X-Amz-Date";

    /**
     * The AMZ content SHA-256 header.
     */
    private static final String X_AMZ_CONTENT_SHA256 = "x-amz-content-sha256";

    /**
     * Prefix for the AWS user metadata keys.
     */
    private static final String AWS_NAME_PREFIX = "x-amz-meta-";

    /**
     * The credentials used when signing.
     */
    private final AwsCredentials myCredentials;

    /**
     * The signature host.
     */
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

    @Override
    public Future<String> getAuthorization(final MultiMap aHeaders, final String aMethod, final AsyncFile aPayload) {
        return getSignature(aHeaders, aMethod, getDigest(aPayload));
    }

    @Override
    public Future<String> getAuthorization(final MultiMap aHeaders, final String aMethod, final Buffer aPayload) {
        return getSignature(aHeaders, aMethod, getDigest(aPayload));
    }

    @Override
    public Future<String> getAuthorization(final MultiMap aHeaders, final String aMethod, final String aPayload) {
        return getSignature(aHeaders, aMethod, getDigest(Buffer.buffer(aPayload)));
    }

    /**
     * Gets the value of the "Authorization" header.
     *
     * @param aHeaders The request headers
     * @param aMethod The request method
     * @param aDigest The SHA-256 digest of the payload
     * @return The header value string
     */
    private Future<String> getSignature(final MultiMap aHeaders, final String aMethod, final Future<String> aDigest) {
        final LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        final Signer.Builder signer = Signer.builder().awsCredentials(myCredentials);
        final String timestamp = localDateTime.format(DATE_TIME_FORMATTER);
        final HttpRequest request = new HttpRequest(aMethod, myHost);
        final Promise<String> promise = Promise.promise();
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

            if (headerKey.startsWith(AWS_NAME_PREFIX) ||
                headerKey.equalsIgnoreCase(HttpHeaders.CONTENT_MD5.toString()) ||
                headerKey.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE.toString())) {
                signer.header(headerKey, entry.getValue());
            }
        }

        signer.header(HOST, myHost.getHost());
        aHeaders.add(HOST, myHost.getHost());
        signer.header(X_AMZ_DATE, timestamp);
        aHeaders.add(X_AMZ_DATE, timestamp);

        aDigest.onComplete(digest -> {
            if (digest.succeeded()) {
                signer.header(X_AMZ_CONTENT_SHA256, digest.result());
                aHeaders.add(X_AMZ_CONTENT_SHA256, digest.result());
                promise.complete(signer.buildS3(request, digest.result()).getSignature());
            } else {
                promise.fail(digest.cause());
            }
        });

        return promise.future();
    }

    /**
     * Gets theSHA-256 digest string of the supplied file.
     *
     * @param aFile A payload
     * @return The SHA-256 digest string
     */
    private Future<String> getDigest(final AsyncFile aFile) {
        final Promise<String> promise = Promise.promise();

        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
            final AtomicInteger counter = new AtomicInteger();

            aFile.handler(read -> {
                final byte[] bytes = read.getBytes();

                counter.addAndGet(bytes.length);
                messageDigest.update(bytes);
            }).endHandler(end -> {
                aFile.setReadPos(0);
                aFile.setReadLength(counter.longValue());
                promise.complete(hashToHex(messageDigest.digest()));
            }).exceptionHandler(error -> {
                promise.fail(error);
            });
        } catch (final NoSuchAlgorithmException details) {
            promise.fail(details);
        }

        return promise.future();
    }

    /**
     * Gets theSHA-256 digest string of the supplied buffer.
     *
     * @param aBuffer A payload
     * @return The SHA-256 digest string
     */
    private Future<String> getDigest(final Buffer aBuffer) {
        final Promise<String> promise = Promise.promise();

        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
            promise.complete(hashToHex(messageDigest.digest(aBuffer.getBytes())));
        } catch (final NoSuchAlgorithmException details) {
            promise.fail(details);
        }

        return promise.future();
    }

    /**
     * Converts the supplied hash to hex.
     *
     * @param aEncodedHash A hash to be converted into hex format
     * @return The supplied hash in hex format
     */
    private String hashToHex(final byte[] aEncodedHash) {
        final StringBuilder hexString = new StringBuilder();

        for (final byte element : aEncodedHash) {
            final String hex = Integer.toHexString(0xff & element);

            if (hex.length() == Constants.SINGLE_INSTANCE) {
                hexString.append('0');
            }

            hexString.append(hex);
        }

        return hexString.toString();
    }
}
