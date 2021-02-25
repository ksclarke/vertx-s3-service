
package info.freelibrary.vertx.s3;

import java.io.IOException;

import info.freelibrary.util.HTTP;
import info.freelibrary.util.StringUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.streams.ReadStream;

/**
 * An S3 client implementation.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class S3Client {

    /** Default S3 endpoint */
    static final S3Endpoint DEFAULT_ENDPOINT = new S3Endpoint("https://s3.amazonaws.com");

    private static final String LIST_CMD = "?list-type=2";

    private static final String PREFIX_LIST_CMD = "?list-type=2&prefix=";

    private static final String REQUEST = "/{}/{}";

    /** AWS credentials */
    private final AwsCredentials myCredentials;

    /** HTTP client used to interact with S3 */
    private final HttpClient myHttpClient;

    /**
     * Creates a new S3 client using system defined AWS credentials and the default S3 endpoint.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     */
    public S3Client(final Vertx aVertx) {
        this(new AwsCredentialsProviderChain().getCredentials(), getHttpClient(aVertx, (S3ClientOptions) null));
    }

    /**
     * Creates a new S3 client using system defined AWS credentials and the supplied HttpClient options.
     *
     * @param aVertx A Vert.x instance from which to create the S3 client
     * @param aConfig A configuration for the internal HttpClient
     */
    public S3Client(final Vertx aVertx, final S3ClientOptions aConfig) {
        this(new AwsCredentialsProviderChain().getCredentials(), getHttpClient(aVertx, aConfig));
    }

    /**
     * Creates a new S3 client using AWS credentials from a system defined profile and the default S3 endpoint.
     *
     * @param aVertx A Vert.x instance from which to create the S3 client
     * @param aProfile The name of a profile in the system AWS credentials
     */
    public S3Client(final Vertx aVertx, final AwsProfile aProfile) {
        this(aProfile.getCredentials(), getHttpClient(aVertx, (S3ClientOptions) null));
    }

    /**
     * Creates a new S3 client using AWS credentials from a system defined profile and the supplied S3 client options.
     *
     * @param aVertx A Vert.x instance from which to create the S3 client
     * @param aProfile An AWS credentials profile
     * @param aConfig An S3 client configuration
     */
    public S3Client(final Vertx aVertx, final AwsProfile aProfile, final S3ClientOptions aConfig) {
        this(aProfile.getCredentials(), getHttpClient(aVertx, aConfig));
    }

    /**
     * Creates a new S3 client using the supplied AWS credentials.
     *
     * @param aVertx A Vert.x instance from which to create the S3 client
     * @param aCredentials AWS credentials
     */
    public S3Client(final Vertx aVertx, final AwsCredentials aCredentials) {
        this(aCredentials, getHttpClient(aVertx, (S3ClientOptions) null));
    }

    /**
     * Creates a new S3 client using the supplied AWS credentials and the supplied S3 client options.
     *
     * @param aVertx A Vert.x instance from which to create the S3 client
     * @param aCredentials AWS credentials
     * @param aConfig An S3 client configuration
     */
    public S3Client(final Vertx aVertx, final AwsCredentials aCredentials, final S3ClientOptions aConfig) {
        this(aCredentials, getHttpClient(aVertx, aConfig));
    }

    /**
     * Creates a new S3 client from the supplied AWS credentials and HttpClient.
     *
     * @param aCredentials AWS credentials for the S3Client
     * @param aHttpClient A Vert.x HttpClient to use from the S3Client
     */
    private S3Client(final AwsCredentials aCredentials, final HttpClient aHttpClient) {
        myCredentials = aCredentials;
        myHttpClient = aHttpClient;
    }

    /**
     * Deletes the S3 resource represented by the supplied key.
     *
     * @param aBucket A bucket from which to delete the object
     * @param aKey The S3 key of the object to delete
     * @return A future indicating the success of the deletion
     */
    public Future<Void> delete(final String aBucket, final String aKey) {
        return createDeleteRequest(aBucket, aKey).compose(request -> request.send().compose(response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.NO_CONTENT) {
                return Future.<Void>succeededFuture();
            } else {
                return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
            }
        }));
    }

    /**
     * Deletes the S3 resource represented by the supplied key.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @param aExceptionHandler An exception handler
     */
    public void delete(final String aBucket, final String aKey, final Handler<AsyncResult<HttpClientResponse>> aHandler,
            final Handler<Throwable> aExceptionHandler) {
        createDeleteRequest(aBucket, aKey).onComplete(deleteRequest -> {
            deleteRequest.result().response(aHandler).exceptionHandler(aExceptionHandler).end();
        });
    }

    /**
     * Gets an object, represented by the supplied key, from an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @return A future indicating the success or failure of the GET
     */
    public Future<Buffer> get(final String aBucket, final String aKey) {
        return createGetRequest(aBucket, aKey).compose(request -> request.send().compose(response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                return response.body();
            } else {
                return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
            }
        }));
    }

    /**
     * Gets an object, represented by the supplied key, from an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @param aExceptionHandler An exception handler
     */
    public void get(final String aBucket, final String aKey, final Handler<AsyncResult<HttpClientResponse>> aHandler,
            final Handler<Throwable> aExceptionHandler) {
        createGetRequest(aBucket, aKey).onComplete(getRequest -> {
            getRequest.result().response(aHandler).exceptionHandler(aExceptionHandler).end();
        });
    }

    /**
     * Performs a HEAD request on an object in S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @return A future with the response headers
     */
    public Future<HttpHeaders> head(final String aBucket, final String aKey) {
        return createHeadRequest(aBucket, aKey).compose(request -> request.send().compose(response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                return Future.succeededFuture(new HttpHeaders(response.headers()));
            } else {
                return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
            }
        }));
    }

    /**
     * Performs a HEAD request on an object in S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @param aExceptionHandler An exception handler
     */
    public void head(final String aBucket, final String aKey, final Handler<AsyncResult<HttpClientResponse>> aHandler,
            final Handler<Throwable> aExceptionHandler) {
        createHeadRequest(aBucket, aKey).onComplete(headRequest -> {
            headRequest.result().response(aHandler).exceptionHandler(aExceptionHandler).end();
        });
    }

    /**
     * Performs a LIST request on an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @return A future with the LIST buffer
     */
    public Future<BucketList> list(final String aBucket) {
        return createGetRequest(aBucket, LIST_CMD).compose(request -> request.send().compose(response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                final Promise<BucketList> promise = Promise.promise();

                response.body(body -> {
                    try {
                        promise.complete(new BucketList(body.result()));
                    } catch (final IOException details) {
                        promise.fail(details);
                    }
                });

                return promise.future();
            } else {
                return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
            }
        }));
    }

    /**
     * Lists an S3 bucket.
     *
     * @param aBucket A bucket from which to get a listing
     * @param aHandler A response handler
     * @param aExceptionHandler An exception handler
     */
    public void list(final String aBucket, final Handler<AsyncResult<HttpClientResponse>> aHandler,
            final Handler<Throwable> aExceptionHandler) {
        createGetRequest(aBucket, LIST_CMD).onComplete(getRequest -> {
            getRequest.result().response(aHandler).exceptionHandler(aExceptionHandler).end();
        });
    }

    /**
     * Performs a prefixed LIST request on an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aPrefix A prefix to use to limit which objects are listed
     * @return A future with the LIST results buffer
     */
    public Future<BucketList> list(final String aBucket, final String aPrefix) {
        final String prefixedList = PREFIX_LIST_CMD + aPrefix;
        return createGetRequest(aBucket, prefixedList).compose(request -> request.send().compose(response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                final Promise<BucketList> promise = Promise.promise();

                response.body(body -> {
                    try {
                        promise.complete(new BucketList(body.result()));
                    } catch (final IOException details) {
                        promise.fail(details);
                    }
                });

                return promise.future();
            } else {
                return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
            }
        }));
    }

    /**
     * Lists an S3 bucket using the supplied prefix as a filter.
     *
     * @param aBucket An S3 bucket
     * @param aPrefix A prefix to use to limit which objects are listed
     * @param aHandler A response handler
     * @param aExceptionHandler An exception handler
     */
    public void list(final String aBucket, final String aPrefix,
            final Handler<AsyncResult<HttpClientResponse>> aHandler, final Handler<Throwable> aExceptionHandler) {
        createGetRequest(aBucket, PREFIX_LIST_CMD + aPrefix).onComplete(getRequest -> {
            getRequest.result().response(aHandler).exceptionHandler(aExceptionHandler).end();
        });
    }

    /**
     * Puts a buffer into an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 object key
     * @param aBuffer A buffer to PUT
     * @return A future indicating when the buffer has been uploaded
     */
    public Future<HttpHeaders> put(final String aBucket, final String aKey, final Buffer aBuffer) {
        return createPutRequest(aBucket, aKey).compose(request -> request.send(aBuffer).compose(response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                return Future.succeededFuture(new HttpHeaders(response.headers()));
            } else {
                return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
            }
        }));
    }

    /**
     * Uploads contents of the Buffer to S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aBuffer A data buffer
     * @param aHandler A response handler
     * @param aExceptionHandler An exception handler
     */
    public void put(final String aBucket, final String aKey, final Buffer aBuffer,
            final Handler<AsyncResult<HttpClientResponse>> aHandler, final Handler<Throwable> aExceptionHandler) {
        createPutRequest(aBucket, aKey).onComplete(putRequest -> {
            putRequest.result().response(aHandler).exceptionHandler(aExceptionHandler).end(aBuffer);
        });
    }

    /**
     * Put a buffer into an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 object key
     * @param aBuffer A buffer to PUT
     * @param aMetadata A metadata object
     * @return A future indicating when the buffer has been uploaded
     */
    public Future<HttpHeaders> put(final String aBucket, final String aKey, final Buffer aBuffer,
            final UserMetadata aMetadata) {
        final Future<S3ClientRequest> futurePut = createPutRequest(aBucket, aKey);
        return futurePut.compose(request -> request.setUserMetadata(aMetadata).send(aBuffer).compose(response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                return Future.succeededFuture(new HttpHeaders(response.headers()));
            } else {
                return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
            }
        }));
    }

    /**
     * Uploads the file contents to S3. Logs any exceptions.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aBuffer A data buffer
     * @param aMetadata User metadata that should be set on the S3 object
     * @param aHandler An upload response handler
     * @param aExceptionHandler An exception handler
     */
    public void put(final String aBucket, final String aKey, final Buffer aBuffer, final UserMetadata aMetadata,
            final Handler<AsyncResult<HttpClientResponse>> aHandler, final Handler<Throwable> aExceptionHandler) {
        createPutRequest(aBucket, aKey).onComplete(putRequest -> {
            final S3ClientRequest request = putRequest.result().setUserMetadata(aMetadata);
            request.response(aHandler).exceptionHandler(aExceptionHandler).end(aBuffer);
        });
    }

    /**
     * Put a streamed buffer into an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 object key
     * @param aReadStream A stream from which to read the buffer
     * @return A future indicating when the buffer has been uploaded
     */
    public Future<HttpHeaders> put(final String aBucket, final String aKey, final ReadStream<Buffer> aReadStream) {
        return createPutRequest(aBucket, aKey).compose(request -> request.send(aReadStream).compose(response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                return Future.succeededFuture(new HttpHeaders(response.headers()));
            } else {
                return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
            }
        }));
    }

    /**
     * Uploads the file contents to S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aReadStream A stream from which to read the content to be sent
     * @param aHandler An upload response handler
     * @param aExceptionHandler An exception handler
     */
    public void put(final String aBucket, final String aKey, final ReadStream<Buffer> aReadStream,
            final Handler<AsyncResult<HttpClientResponse>> aHandler, final Handler<Throwable> aExceptionHandler) {
        put(aBucket, aKey, aReadStream, null, aHandler, aExceptionHandler);
    }

    /**
     * Put a streamed buffer into an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 object key
     * @param aReadStream A stream from which to read the buffer
     * @param aMetadata A metadata object
     * @return A future indicating when the buffer has been uploaded
     */
    public Future<HttpHeaders> put(final String aBucket, final String aKey, final ReadStream<Buffer> aReadStream,
            final UserMetadata aMetadata) {
        final Future<S3ClientRequest> futurePut = createPutRequest(aBucket, aKey);
        return futurePut.compose(request -> request.setUserMetadata(aMetadata).send(aReadStream).compose(response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                return Future.succeededFuture(new HttpHeaders(response.headers()));
            } else {
                return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
            }
        }));
    }

    /**
     * Uploads the file contents to S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aReadStream A stream from which to read the content to be sent
     * @param aMetadata User metadata to set on the uploaded S3 object
     * @param aHandler An upload response handler
     * @param aExceptionHandler An upload exception handler
     */
    public void put(final String aBucket, final String aKey, final ReadStream<Buffer> aReadStream,
            final UserMetadata aMetadata, final Handler<AsyncResult<HttpClientResponse>> aHandler,
            final Handler<Throwable> aExceptionHandler) {
        createPutRequest(aBucket, aKey).onComplete(putRequest -> {
            final S3ClientRequest request = putRequest.result();

            if (aMetadata != null) {
                request.setUserMetadata(aMetadata);
            }

            request.response(aHandler).exceptionHandler(aExceptionHandler);
            request.send(aReadStream);
        });
    }

    /**
     * Closes the S3 client.
     */
    public void close() {
        myHttpClient.close();
    }

    /**
     * Sets a connection handler for the client. This handler is called when a new connection is established.
     *
     * @param aHandler A connection handler
     * @return This S3 client
     */
    S3Client connectionHandler(final Handler<HttpConnection> aHandler) {
        myHttpClient.connectionHandler(aHandler);
        return this;
    }

    /**
     * A convenience method for building the request URI.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 object key
     * @return A request URI
     */
    private String getURI(final String aBucket, final String aKey) {
        return StringUtils.format(REQUEST, aBucket, aKey);
    }

    /**
     * Creates an S3 DELETE request.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @return An S3 client request
     */
    private Future<S3ClientRequest> createDeleteRequest(final String aBucket, final String aKey) {
        final Future<HttpClientRequest> futureRequest = myHttpClient.request(HttpMethod.DELETE, getURI(aBucket, aKey));
        final Promise<S3ClientRequest> promise = Promise.promise();

        futureRequest.onComplete(request -> {
            promise.complete(new S3ClientRequest(request.result(), myCredentials));
        });

        return promise.future();
    }

    /**
     * Creates an S3 GET request.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @return A S3 client GET request
     */
    private Future<S3ClientRequest> createGetRequest(final String aBucket, final String aKey) {
        final Future<HttpClientRequest> futureRequest = myHttpClient.request(HttpMethod.GET, getURI(aBucket, aKey));
        final Promise<S3ClientRequest> promise = Promise.promise();

        futureRequest.onComplete(request -> {
            promise.complete(new S3ClientRequest(request.result(), myCredentials));
        });

        return promise.future();
    }

    /**
     * Creates an S3 HEAD request.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @return A S3 client HEAD request
     */
    private Future<S3ClientRequest> createHeadRequest(final String aBucket, final String aKey) {
        final Future<HttpClientRequest> futureRequest = myHttpClient.request(HttpMethod.HEAD, getURI(aBucket, aKey));
        final Promise<S3ClientRequest> promise = Promise.promise();

        futureRequest.onComplete(request -> {
            promise.complete(new S3ClientRequest(request.result(), myCredentials));
        });

        return promise.future();
    }

    /**
     * Creates an S3 PUT request.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @return An S3 PUT request
     */
    private Future<S3ClientRequest> createPutRequest(final String aBucket, final String aKey) {
        final Future<HttpClientRequest> futureRequest = myHttpClient.request(HttpMethod.PUT, getURI(aBucket, aKey));
        final Promise<S3ClientRequest> promise = Promise.promise();

        futureRequest.onComplete(request -> {
            promise.complete(new S3ClientRequest(request.result(), myCredentials));
        });

        return promise.future();
    }

    /**
     * A convenience method to create a new HttpClient for use by our S3 client.
     *
     * @param aVertx A Vert.x instance
     * @param aConfig A S3 client configuration
     * @return A newly created HttpClient
     */
    private static HttpClient getHttpClient(final Vertx aVertx, final S3ClientOptions aConfig) {
        final HttpClient httpClient;

        if (aConfig != null && aConfig.getDefaultHost() != null && aConfig.getDefaultPort() != -1) {
            httpClient = aVertx.createHttpClient(aConfig);
        } else {
            final HttpClientOptions options = new HttpClientOptions().setSsl(true).setDefaultPort(443);
            httpClient = aVertx.createHttpClient(options.setDefaultHost(S3Client.DEFAULT_ENDPOINT.getHost()));
        }

        return httpClient;
    }

}
