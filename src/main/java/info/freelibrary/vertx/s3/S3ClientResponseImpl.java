
package info.freelibrary.vertx.s3;

// import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.streams.Pipe;
import io.vertx.core.streams.WriteStream;

/**
 * A successful S3 response that provides a way to access headers and the response body.
 */
class S3ClientResponseImpl implements S3ClientResponse {

    private final HttpClientResponse myHttpResponse;

    /**
     * Creates a new S3 client response from the supplied HTTP client response.
     *
     * @param aResponse An HTTP client response
     */
    S3ClientResponseImpl(final HttpClientResponse aResponse) {
        myHttpResponse = aResponse;
    }

    /**
     * Gets the whole response body.
     *
     * @return The response body
     */
    @Override
    public Future<Buffer> body() {
        return myHttpResponse.body();
    }

    /**
     * Gets the whole response body using the supplied handler.
     *
     * @param aHandler The handler to use for getting the body
     * @return The S3 client response
     */
    @Override
    public S3ClientResponse body(final Handler<AsyncResult<Buffer>> aHandler) {
        myHttpResponse.body(aHandler);
        return this;
    }

    @Override
    public S3ClientResponse handler(final Handler<Buffer> aHandler) {
        myHttpResponse.handler(aHandler);
        return this;
    }

    /**
     * Gets the response headers.
     *
     * @return The response headers
     */
    @Override
    public HttpHeaders headers() {
        return new HttpHeaders(myHttpResponse.headers());
    }

    @Override
    public Pipe<Buffer> pipe() {
        return myHttpResponse.pipe();
    }

    @Override
    public Future<Void> pipeTo(final WriteStream<Buffer> aStream) {
        return myHttpResponse.pipeTo(aStream);
    }

    @Override
    public void pipeTo(final WriteStream<Buffer> aStream, final Handler<AsyncResult<Void>> aHandler) {
        myHttpResponse.pipeTo(aStream, aHandler);
    }

    @Override
    public S3ClientResponse exceptionHandler(final Handler<Throwable> aHandler) {
        myHttpResponse.exceptionHandler(aHandler);
        return this;
    }

    @Override
    public S3ClientResponse pause() {
        myHttpResponse.pause();
        return this;
    }

    @Override
    public S3ClientResponse resume() {
        myHttpResponse.resume();
        return this;
    }

    @Override
    public S3ClientResponse fetch(final long aCount) {
        myHttpResponse.fetch(aCount);
        return this;
    }

    @Override
    public S3ClientResponse endHandler(final Handler<Void> aHandler) { // @Nullable final Handler<Void> aHandler) {
        myHttpResponse.endHandler(aHandler);
        return this;
    }
}
