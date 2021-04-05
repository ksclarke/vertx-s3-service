
package info.freelibrary.vertx.s3;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
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

    /** Prefix for the AWS user metadata keys */
    private static final String AWS_NAME_PREFIX = "x-amz-meta-";

    /** The underlying S3 HTTP client request */
    private final HttpClientRequest myRequest;

    /** The AWS S3 credentials */
    private final Optional<AwsCredentials> myCredentials;

    /**
     * Creates a new S3 client request.
     *
     * @param aRequest A HttpClientRequest
     */
    S3ClientRequest(final HttpClientRequest aRequest) {
        this(aRequest, null);
    }

    /**
     * Creates a new S3 client request.
     *
     * @param aRequest A HttpClientRequest
     * @param aCredentials AWS credentials
     */
    S3ClientRequest(final HttpClientRequest aRequest, final AwsCredentials aCredentials) {
        myCredentials = Optional.ofNullable(aCredentials);
        myRequest = aRequest;
    }

    /**
     * Gets the credentials, if any, associated with this request.
     *
     * @return The request's AWS credentials
     */
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
    public boolean isChunked() {
        return myRequest.isChunked();
    }

    @Override
    public HttpMethod getMethod() {
        return myRequest.getMethod();
    }

    @Override
    public String getURI() {
        return myRequest.getURI();
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
    public S3ClientRequest putHeader(final String aName, final Iterable<String> aValueIterable) {
        myRequest.putHeader(aName, aValueIterable);
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
    public Future<Void> write(final String aChunk) throws IllegalStateException {
        return myRequest.write(aChunk);
    }

    @Override
    public Future<Void> write(final String aChunk, final String aEncoding) {
        return myRequest.write(aChunk, aEncoding);
    }

    @Override
    public S3ClientRequest continueHandler(final Handler<Void> aHandler) {
        myRequest.continueHandler(aHandler);
        return this;
    }

    @Override
    public Future<Void> end(final String aChunk) {
        return addAuthorizationHeader(Buffer.buffer(aChunk)).compose(
            result -> myRequest.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(aChunk.length())).end(aChunk));
    }

    @Override
    public Future<Void> end(final String aChunk, final String aEncoding) {
        return addAuthorizationHeader(Buffer.buffer(aChunk, aEncoding)).compose(result -> myRequest
            .putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(aChunk.length())).end(aChunk, aEncoding));
    }

    @Override
    public Future<Void> end(final Buffer aChunk) {
        return addAuthorizationHeader(aChunk).compose(
            result -> myRequest.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(aChunk.length())).end(aChunk));
    }

    @Override
    public Future<Void> end() {
        return addAuthorizationHeader().compose(result -> myRequest.end());
    }

    /**
     * Adds the authentication header.
     *
     * @return The S3 client request
     */
    protected Future<Void> addAuthorizationHeader() {
        return addAuthorizationHeader(Buffer.buffer());
    }

    /**
     * Adds the authentication header.
     *
     * @param aFile An asynchronous file
     * @return The S3 client request
     */
    protected Future<Void> addAuthorizationHeader(final AsyncFile aFile) {
        final Promise<Void> promise = Promise.promise();

        if (myCredentials.isPresent()) {
            final AwsSignatureFactory factory = AwsSignatureFactory.getFactory().setHost(URI.create(absoluteURI()));
            final MultiMap headers = headers();

            factory.setCredentials(myCredentials.get());
            factory.getSignature().getAuthorization(headers, getMethod().name(), aFile).onComplete(auth -> {
                if (auth.succeeded()) {
                    headers.add(HttpHeaders.AUTHORIZATION, auth.result());
                    promise.complete();
                } else {
                    promise.fail(auth.cause());
                }
            });
        } else {
            promise.complete(); // Will probably lead to a 403, but that's okay
        }

        return promise.future();
    }

    /**
     * Adds the authentication header.
     *
     * @param aBuffer A buffer
     * @return The S3 client request
     */
    protected Future<Void> addAuthorizationHeader(final Buffer aBuffer) {
        final Promise<Void> promise = Promise.promise();

        if (myCredentials.isPresent()) {
            final AwsSignatureFactory factory = AwsSignatureFactory.getFactory().setHost(URI.create(absoluteURI()));
            final MultiMap headers = headers();

            factory.setCredentials(myCredentials.get());
            factory.getSignature().getAuthorization(headers, getMethod().name(), aBuffer).onComplete(auth -> {
                if (auth.succeeded()) {
                    headers.add(HttpHeaders.AUTHORIZATION, auth.result());
                    promise.complete();
                } else {
                    promise.fail(auth.cause());
                }
            });
        } else {
            promise.complete(); // Will probably lead to a 403, but that's okay
        }

        return promise.future();
    }

    /**
     * Sets the user metadata for a PUT upload.
     *
     * @param aUserMetadata User metadata to set on an uploaded S3 object
     * @return The S3 client request
     */
    public S3ClientRequest setUserMetadata(final UserMetadata aUserMetadata) {
        Objects.requireNonNull(aUserMetadata);

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
    public S3ClientRequest sendHead(final Handler<AsyncResult<Void>> aHandler) {
        addAuthorizationHeader().onComplete(auth -> {
            if (auth.succeeded()) {
                myRequest.sendHead(aHandler);
            } else {
                aHandler.handle(Future.failedFuture(auth.cause()));
            }
        });

        return this;
    }

    @Override
    public Future<Void> sendHead() {
        return addAuthorizationHeader().compose(result -> myRequest.sendHead());
    }

    @Override
    public Future<HttpClientResponse> send() {
        return addAuthorizationHeader().compose(result -> myRequest.send());
    }

    @Override
    public Future<HttpClientResponse> send(final Buffer aBuffer) {
        return addAuthorizationHeader(aBuffer).compose(
            result -> myRequest.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(aBuffer.length())).send(aBuffer));
    }

    public Future<HttpClientResponse> send(final AsyncFile aFile) {
        return addAuthorizationHeader(aFile).compose(result -> myRequest
            .putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(aFile.getReadLength())).send(aFile));
    }

    @Override
    public Future<HttpClientResponse> send(final String aString) {
        return addAuthorizationHeader().compose(
            result -> myRequest.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(aString.length())).send(aString));
    }

    @Override
    public S3ClientRequest writeCustomFrame(final int aType, final int aFlagsInt, final Buffer aPayload) {
        myRequest.writeCustomFrame(aType, aFlagsInt, aPayload);
        return this;
    }

    @Override
    public StreamPriority getStreamPriority() {
        return myRequest.getStreamPriority();
    }

    @Override
    public void write(final Buffer aBuffer, final Handler<AsyncResult<Void>> aHandler) {
        myRequest.write(aBuffer, aHandler);
    }

    @Override
    public void write(final String aChunk, final Handler<AsyncResult<Void>> aHandler) {
        myRequest.write(aChunk, aHandler);
    }

    @Override
    public void write(final String aChunk, final String aEncoding, final Handler<AsyncResult<Void>> aHandler) {
        myRequest.write(aChunk, aEncoding, aHandler);
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

    @Override
    public S3ClientRequest setMaxRedirects(final int aMaxRedirects) {
        myRequest.setMaxRedirects(aMaxRedirects);
        return this;
    }

    @Override
    public Future<Void> write(final Buffer aBuffer) {
        return myRequest.write(aBuffer);
    }

    @Override
    public boolean reset(final long aCode, final Throwable aCause) {
        return myRequest.reset(aCode, aCause);
    }

    @Override
    public S3ClientRequest setHost(final String aHost) {
        return (S3ClientRequest) myRequest.setHost(aHost);
    }

    @Override
    public String getHost() {
        return myRequest.getHost();
    }

    @Override
    public S3ClientRequest setPort(final int aPort) {
        return (S3ClientRequest) myRequest.setPort(aPort);
    }

    @Override
    public int getPort() {
        return myRequest.getPort();
    }

    @Override
    public S3ClientRequest setMethod(final HttpMethod aMethod) {
        return (S3ClientRequest) myRequest.setMethod(aMethod);
    }

    @Override
    public S3ClientRequest setURI(final String aURI) {
        return (S3ClientRequest) myRequest.setURI(aURI);
    }

    @Override
    public HttpVersion version() {
        return myRequest.version();
    }

    @Override
    public void connect(final Handler<AsyncResult<HttpClientResponse>> aHandler) {
        myRequest.connect(aHandler);
    }

    @Override
    public Future<HttpClientResponse> connect() {
        return myRequest.connect();
    }

    @Override
    public S3ClientRequest response(final Handler<AsyncResult<HttpClientResponse>> aHandler) {
        myRequest.response(aHandler);
        return this;
    }

    @Override
    public Future<HttpClientResponse> response() {
        return myRequest.response();
    }

}
