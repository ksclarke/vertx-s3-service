
package info.freelibrary.vertx.s3;

import static info.freelibrary.vertx.s3.Constants.PATH_SEP;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.StringJoiner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.StreamPriority;

/**
 * An S3 client request implementation of <code>HttpClientRequest</code>.
 */
@SuppressWarnings({ "PMD.ExcessivePublicCount", "PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals" })
public class S3ClientRequest implements HttpClientRequest {

    /** Hash-based message authentication code used for signing AWS requests */
    private static final String HASH_CODE = "HmacSHA1";

    /** System-independent end of line */
    private static final String EOL = "\n";

    /** The date format used for timestamping S3 requests */
    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /** The logger used for S3 client requests */
    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientRequest.class);

    /** The underlying S3 HTTP client request */
    private final HttpClientRequest myRequest;

    /** The method of the S3 client request */
    private final String myMethod;

    /** The S3 bucket for the request */
    private final String myBucket;

    /** The S3 key for the request */
    private final String myKey;

    /** The MD5 for the content */
    private String myContentMd5;

    /** The content type for the request */
    private String myContentType;

    /** AWS access key */
    private String myAccessKey;

    /** AWS secret key */
    private String mySecretKey;

    /** The S3 session token (optional) */
    private final String mySessionToken;

    /**
     * Creates a new S3 client request.
     *
     * @param aMethod An HTTP method
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aRequest A HttpClientRequest
     */
    public S3ClientRequest(final String aMethod, final String aBucket, final String aKey,
            final HttpClientRequest aRequest) {
        this(aMethod, aBucket, aKey, aRequest, null, null, null);
    }

    /**
     * Creates a new S3 client request.
     *
     * @param aMethod An HTTP method
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aRequest A HttpClientRequest
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AWS secret key
     * @param aSessionToken An S3 session token (optional)
     */
    public S3ClientRequest(final String aMethod, final String aBucket, final String aKey,
            final HttpClientRequest aRequest, final String aAccessKey, final String aSecretKey,
            final String aSessionToken) {
        this(aMethod, aBucket, aKey, aRequest, aAccessKey, aSecretKey, aSessionToken, "", "");
    }

    /**
     * Creates a new S3 client request.
     *
     * @param aMethod An HTTP method
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aRequest A HttpClientRequest
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AWS secret key
     * @param aSessionToken An S3 session token (optional)
     * @param aContentMd5 An MD5 hash for the request's content
     * @param aContentType A type of the request's content
     */
    public S3ClientRequest(final String aMethod, final String aBucket, final String aKey,
            final HttpClientRequest aRequest, final String aAccessKey, final String aSecretKey,
            final String aSessionToken, final String aContentMd5, final String aContentType) {
        myMethod = aMethod;
        myBucket = aBucket;
        myKey = aKey;
        myRequest = aRequest;
        myAccessKey = aAccessKey;
        mySecretKey = aSecretKey;
        mySessionToken = aSessionToken;
        myContentMd5 = aContentMd5;
        myContentType = aContentType;
    }

    @Override
    public S3ClientRequest setFollowRedirects(final boolean aBool) {
        myRequest.setFollowRedirects(aBool);
        return this;
    }

    @Override
    public S3ClientRequest setWriteQueueMaxSize(final int aMaxSize) {
        myRequest.setWriteQueueMaxSize(aMaxSize);
        return this;
    }

    @Override
    public String absoluteURI() {
        return myRequest.absoluteURI();
    }

    @Override
    @Deprecated
    @SuppressWarnings("MissingDeprecated")
    public S3ClientRequest handler(final Handler<HttpClientResponse> aHandler) {
        myRequest.handler(aHandler);
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        return myRequest.writeQueueFull();
    }

    @Override
    public S3ClientRequest drainHandler(final Handler<Void> aHandler) {
        myRequest.drainHandler(aHandler);
        return this;
    }

    @Override
    public S3ClientRequest exceptionHandler(final Handler<Throwable> aHandler) {
        myRequest.exceptionHandler(aHandler);
        return this;
    }

    @Override
    public S3ClientRequest setChunked(final boolean aChunked) {
        myRequest.setChunked(aChunked);
        return this;
    }

    @Override
    public MultiMap headers() {
        return myRequest.headers();
    }

    @Override
    @Deprecated
    @SuppressWarnings("MissingDeprecated")
    public S3ClientRequest pause() {
        myRequest.pause();
        return this;
    }

    @Override
    @Deprecated
    @SuppressWarnings("MissingDeprecated")
    public S3ClientRequest resume() {
        myRequest.resume();
        return this;
    }

    @Override
    @Deprecated
    @SuppressWarnings("MissingDeprecated")
    public S3ClientRequest endHandler(final Handler<Void> aEndHandler) {
        myRequest.endHandler(aEndHandler);
        return this;
    }

    @Override
    public boolean isChunked() {
        return myRequest.isChunked();
    }

    @Override
    public HttpMethod method() {
        return myRequest.method();
    }

    @Override
    public String uri() {
        return myRequest.uri();
    }

    @Override
    public S3ClientRequest putHeader(final String aName, final String aValue) {
        myRequest.putHeader(aName, aValue);
        return this;
    }

    @Override
    public S3ClientRequest putHeader(final CharSequence aName, final CharSequence aValue) {
        myRequest.putHeader(aName, aValue);
        return this;
    }

    @Override
    public S3ClientRequest putHeader(final String aName, final Iterable<String> aValues) {
        myRequest.putHeader(aName, aValues);
        return this;
    }

    @Override
    public S3ClientRequest putHeader(final CharSequence aName, final Iterable<CharSequence> aValues) {
        myRequest.putHeader(aName, aValues);
        return this;
    }

    @Override
    public S3ClientRequest setTimeout(final long aTimeoutMs) {
        myRequest.setTimeout(aTimeoutMs);
        return this;
    }

    @Override
    public S3ClientRequest write(final Buffer aChunk) {
        myRequest.write(aChunk);
        return this;
    }

    @Override
    public S3ClientRequest write(final String aChunk) {
        myRequest.write(aChunk);
        return this;
    }

    @Override
    public S3ClientRequest write(final String aChunk, final String aEncoding) {
        myRequest.write(aChunk, aEncoding);
        return this;
    }

    @Override
    public S3ClientRequest continueHandler(final Handler<Void> aHandler) {
        myRequest.continueHandler(aHandler);
        return this;
    }

    @Override
    public void end(final String aChunk) {
        initAuthenticationHeader();
        myRequest.end(aChunk);
    }

    @Override
    public void end(final String aChunk, final String aEncoding) {
        initAuthenticationHeader();
        myRequest.end(aChunk, aEncoding);
    }

    @Override
    public void end(final Buffer aChunk) {
        initAuthenticationHeader();
        myRequest.end(aChunk);
    }

    @Override
    public void end() {
        initAuthenticationHeader();
        myRequest.end();
    }

    /**
     * Adds the authentication header.
     */
    protected void initAuthenticationHeader() {
        if (isAuthenticated()) {
            // Calculate the v2 signature
            // http://docs.amazonwebservices.com/AmazonS3/latest/dev/RESTAuthentication.html
            // #ConstructingTheAuthenticationHeader

            // Date should look like Thu, 17 Nov 2005 18:49:58 GMT, and must be
            // within 15 min. of S3 server time. contentMd5 and type are optional

            // We can't risk letting our date get clobbered and being inconsistent
            final String xamzdate = new SimpleDateFormat(DATE_FORMAT, Locale.US).format(new Date());
            final StringJoiner signedHeaders = new StringJoiner(EOL, "", EOL);
            final StringBuilder toSign = new StringBuilder();
            final String key = myKey.charAt(0) == '?' ? "" : myKey;

            headers().add("X-Amz-Date", xamzdate);
            signedHeaders.add("x-amz-date:" + xamzdate);

            if (!StringUtils.isEmpty(mySessionToken)) {
                headers().add("X-Amz-Security-Token", mySessionToken);
                signedHeaders.add("x-amz-security-token:" + mySessionToken);
            }

            toSign.append(myMethod).append(EOL).append(myContentMd5).append(EOL).append(myContentType).append(EOL)
                    .append(EOL).append(signedHeaders).append(PATH_SEP).append(myBucket).append(PATH_SEP).append(key);

            try {
                final String signature = b64SignHmacSha1(mySecretKey, toSign.toString());
                final String authorization = "AWS" + " " + myAccessKey + ":" + signature;

                headers().add("Authorization", authorization);
            } catch (InvalidKeyException | NoSuchAlgorithmException details) {
                LOGGER.error("Failed to sign S3 request due to {}", details);
            }
        }
    }

    /**
     * Returns whether the request is authenticated.
     *
     * @return True if request is authenticated; else, false
     */
    public boolean isAuthenticated() {
        return myAccessKey != null && mySecretKey != null;
    }

    /**
     * Sets an access key for the request.
     *
     * @param aAccessKey An S3 access key
     */
    public void setAccessKey(final String aAccessKey) {
        myAccessKey = aAccessKey;
    }

    /**
     * Sets a secret key for the request.
     *
     * @param aSecretKey An S3 secret key
     */
    public void setSecretKey(final String aSecretKey) {
        mySecretKey = aSecretKey;
    }

    /**
     * Gets the HTTP method to be used with the request.
     *
     * @return The HTTP method to be used with the request
     */
    public String getMethod() {
        return myMethod;
    }

    /**
     * Gets the content MD5.
     *
     * @return The content MD5
     */
    public String getContentMd5() {
        return myContentMd5;
    }

    /**
     * Sets the content MD5.
     *
     * @param aContentMd5 An MD5 value for the content
     */
    public void setContentMd5(final String aContentMd5) {
        myContentMd5 = aContentMd5;
    }

    /**
     * Gets the content type for the request.
     *
     * @return The content type for the request
     */
    public String getContentType() {
        return myContentType;
    }

    /**
     * Sets the content type for the request
     *
     * @param aContentType The content type for the request
     */
    public void setContentType(final String aContentType) {
        myContentType = aContentType;
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

    @Override
    public HttpConnection connection() {
        return myRequest.connection();
    }

    @Override
    public S3ClientRequest connectionHandler(final Handler<HttpConnection> aHandler) {
        myRequest.connectionHandler(aHandler);
        return this;
    }

    @Override
    public String getHost() {
        return myRequest.getHost();
    }

    @Override
    public String getRawMethod() {
        return myRequest.getRawMethod();
    }

    @Override
    public String path() {
        return myRequest.path();
    }

    @Override
    public S3ClientRequest pushHandler(final Handler<HttpClientRequest> aHandler) {
        myRequest.pushHandler(aHandler);
        return this;
    }

    @Override
    public String query() {
        return myRequest.query();
    }

    @Override
    public boolean reset(final long aCode) {
        return myRequest.reset(aCode);
    }

    @Override
    public S3ClientRequest sendHead(final Handler<HttpVersion> aHandler) {
        myRequest.sendHead(aHandler);
        return this;
    }

    @Override
    public S3ClientRequest sendHead() {
        initAuthenticationHeader();
        myRequest.sendHead();
        return this;
    }

    @Override
    public S3ClientRequest setHost(final String aHost) {
        myRequest.setHost(aHost);
        return this;
    }

    @Override
    public S3ClientRequest setRawMethod(final String aMethod) {
        myRequest.setRawMethod(aMethod);
        return this;
    }

    @Override
    public S3ClientRequest writeCustomFrame(final int aType, final int aFlagsInt, final Buffer aPayload) {
        myRequest.writeCustomFrame(aType, aFlagsInt, aPayload);
        return this;
    }

    @Override
    @Deprecated
    @SuppressWarnings("MissingDeprecated")
    public S3ClientRequest fetch(final long aAmount) {
        myRequest.fetch(aAmount);
        return this;
    }

    @Override
    public StreamPriority getStreamPriority() {
        return myRequest.getStreamPriority();
    }

    @Override
    public S3ClientRequest write(final Buffer aBuffer, final Handler<AsyncResult<Void>> aHandler) {
        myRequest.write(aBuffer, aHandler);
        return this;
    }

    @Override
    public S3ClientRequest write(final String aChunk, final Handler<AsyncResult<Void>> aHandler) {
        myRequest.write(aChunk, aHandler);
        return this;
    }

    @Override
    public S3ClientRequest write(final String aChunk, final String aEncoding,
            final Handler<AsyncResult<Void>> aHandler) {
        myRequest.write(aChunk, aEncoding, aHandler);
        return this;
    }

    @Override
    public void end(final String aChunk, final Handler<AsyncResult<Void>> aHandler) {
        myRequest.end(aChunk, aHandler);
    }

    @Override
    public void end(final String aChunk, final String aEncoding, final Handler<AsyncResult<Void>> aHandler) {
        myRequest.end(aChunk, aEncoding, aHandler);
    }

    @Override
    public void end(final Buffer aBuffer, final Handler<AsyncResult<Void>> aHandler) {
        myRequest.end(aBuffer, aHandler);
    }

    @Override
    public void end(final Handler<AsyncResult<Void>> aHandler) {
        myRequest.end(aHandler);
    }

}
