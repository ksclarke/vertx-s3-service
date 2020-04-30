
package info.freelibrary.vertx.s3;

import static info.freelibrary.vertx.s3.AwsSignatureFactory.Version.V2;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.StreamPriority;

/**
 * An S3 client request implementation of <code>HttpClientRequest</code>.
 */
@SuppressWarnings({ "PMD.ExcessivePublicCount", "PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals" })
class S3ClientRequest implements HttpClientRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientRequest.class, Constants.BUNDLE_NAME);

    /** Prefix for the AWS user metadata keys */
    private static final String AWS_NAME_PREFIX = "x-amz-meta-";

    /** The underlying S3 HTTP client request */
    private final HttpClientRequest myRequest;

    /** The method of the S3 client request */
    private final String myMethod;

    /** The S3 bucket for the request */
    private final String myBucket;

    /** The S3 key for the request */
    private final String myKey;

    /** The AWS S3 credentials */
    private final Optional<AwsCredentials> myCredentials;

    /** Whether to use the older v2 syntax */
    private boolean isV2Signature;

    /**
     * Creates a new S3 client request.
     *
     * @param aEndpoint An S3 endpoint
     * @param aMethod An HTTP method
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aRequest A HttpClientRequest
     */
    S3ClientRequest(final String aMethod, final String aBucket, final String aKey, final HttpClientRequest aRequest) {
        this(aMethod, aBucket, aKey, aRequest, null, null, null);
    }

    /**
     * Creates a new S3 client request.
     *
     * @param aEndpoint An S3 endpoint
     * @param aMethod An HTTP method
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aRequest A HttpClientRequest
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AWS secret key
     * @param aSessionToken An S3 session token (optional)
     */
    S3ClientRequest(final String aMethod, final String aBucket, final String aKey, final HttpClientRequest aRequest,
            final String aAccessKey, final String aSecretKey, final String aSessionToken) {
        myMethod = aMethod;
        myBucket = aBucket;
        myKey = aKey;
        myRequest = aRequest;

        if (aAccessKey != null && aSecretKey != null) {
            if (aSessionToken != null) {
                myCredentials = Optional.of(new AwsCredentials(aAccessKey, aSecretKey, aSessionToken));
            } else {
                myCredentials = Optional.of(new AwsCredentials(aAccessKey, aSecretKey));
            }
        } else {
            if (aAccessKey != null || aSecretKey != null) {
                LOGGER.warn(MessageCodes.VS3_009);
            }

            myCredentials = Optional.empty();
        }
    }

    public Optional<AwsCredentials> getCredentials() {
        return myCredentials;
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
        addAuthorizationHeader(aChunk.getBytes());
        myRequest.end(aChunk);
    }

    @Override
    public void end(final String aChunk, final String aEncoding) {
        addAuthorizationHeader(aChunk.getBytes(Charset.forName(aEncoding)));
        myRequest.end(aChunk, aEncoding);
    }

    @Override
    public void end(final Buffer aChunk) {
        addAuthorizationHeader(aChunk.getBytes());
        myRequest.end(aChunk);
    }

    @Override
    public void end() {
        addAuthenticationHeader();
        myRequest.end();
    }

    /**
     * Tells the S3 request to use the older, almost obsolete, AWS V2 signature format.
     *
     * @param aV2Signature
     * @return The S3 client request
     */
    public S3ClientRequest useV2Signature(final boolean aV2Signature) {
        isV2Signature = aV2Signature;
        return this;
    }

    /**
     * Adds the authentication header.
     *
     * @return The S3 client request
     */
    protected S3ClientRequest addAuthenticationHeader() {
        return addAuthorizationHeader(new byte[] {});
    }

    /**
     * Adds the authentication header.
     *
     * @return The S3 client request
     */
    protected S3ClientRequest addAuthorizationHeader(final byte[] aBytes) {
        if (myCredentials.isPresent()) {
            final MultiMap headers = headers();
            final AwsSignatureFactory factory;
            final AwsSignature signature;

            // Get the signature we want; only the latest signature version requires a host
            if (isV2Signature) {
                factory = AwsSignatureFactory.getFactory(V2);
            } else {
                factory = AwsSignatureFactory.getFactory().setHost(URI.create(absoluteURI()));
            }

            factory.setCredentials(myCredentials.get());

            // If the content-md5 header isn't already set, we can set it now using our supplied byte array
            if (!headers.contains(HttpHeaders.CONTENT_MD5) && aBytes != null && aBytes.length > 0) {

            }

            signature = factory.getSignature();
            headers.add("Authorization", signature.getAuthorization(headers, myMethod, myBucket, myKey, aBytes));
        }

        return this;
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
     * Sets the user metadata for a PUT upload.
     *
     * @param aUserMetadata User metadata to set on an uploaded S3 object
     * @return The S3 client request
     */
    public S3ClientRequest setUserMetadata(final UserMetadata aUserMetadata) {
        for (int index = 0; index < aUserMetadata.count(); index++) {
            myRequest.putHeader(AWS_NAME_PREFIX + aUserMetadata.getName(index), aUserMetadata.getValue(index));
        }

        return this;
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
        addAuthenticationHeader();
        myRequest.sendHead(aHandler);
        return this;
    }

    @Override
    public S3ClientRequest sendHead() {
        addAuthenticationHeader();
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
