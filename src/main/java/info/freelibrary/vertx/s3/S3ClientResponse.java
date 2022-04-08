
package info.freelibrary.vertx.s3;

import io.vertx.codegen.annotations.Nullable;
// import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.Pipe;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

/**
 * A successful S3 response that provides a way to access headers and the response body.
 */
public interface S3ClientResponse extends ReadStream<Buffer> {

    /**
     * Gets the whole response body.
     *
     * @return The response body
     */
    Future<Buffer> body();

    /**
     * Gets the whole response body using the supplied handler.
     *
     * @param aHandler The handler to use for getting the body
     * @return The S3 client response
     */
    S3ClientResponse body(Handler<AsyncResult<Buffer>> aHandler);

    /**
     * Gets the response headers.
     *
     * @return The response headers
     */
    HttpHeaders headers();

    @Override
    S3ClientResponse handler(Handler<Buffer> aHandler);

    @Override
    Pipe<Buffer> pipe();

    @Override
    Future<Void> pipeTo(WriteStream<Buffer> aStream);

    @Override
    void pipeTo(WriteStream<Buffer> aStream, Handler<AsyncResult<Void>> aHandler);

    @Override
    S3ClientResponse exceptionHandler(Handler<Throwable> aHandler);

    @Override
    S3ClientResponse pause();

    @Override
    S3ClientResponse resume();

    @Override
    S3ClientResponse fetch(long aCount);

    @Override
    S3ClientResponse endHandler(@Nullable Handler<Void> aHandler);
}
