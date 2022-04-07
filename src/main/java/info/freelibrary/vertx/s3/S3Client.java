
package info.freelibrary.vertx.s3;

import static info.freelibrary.util.Constants.EOL;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import javax.xml.transform.TransformerException;

import info.freelibrary.util.HTTP;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;
import info.freelibrary.util.XmlUtils;

import info.freelibrary.vertx.s3.util.MessageCodes;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpMethod;

/**
 * An S3 client implementation.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class S3Client {

    /**
     * The S3 client logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(S3Client.class, MessageCodes.BUNDLE);

    /**
     * The S3 list command.
     */
    private static final String LIST_CMD = "?list-type=2";

    /**
     * The S3 list command with a prefix.
     */
    private static final String PREFIX_LIST_CMD = "?list-type=2&prefix=";

    /**
     * A URL request template.
     */
    private static final String REQUEST = "/{}/{}";

    /**
     * AWS credentials.
     */
    private final AwsCredentials myCredentials;

    /**
     * HTTP client used to interact with S3.
     */
    private final HttpClient myHttpClient;

    /**
     * The internal Vert.x instance.
     */
    private final Vertx myVertx;

    /**
     * Creates a new S3 client using system defined AWS credentials and the default S3 endpoint.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     */
    public S3Client(final Vertx aVertx) {
        myCredentials = new AwsCredentialsProviderChain().getCredentials();
        myHttpClient = getHttpClient(aVertx, (S3ClientOptions) null);
        myVertx = aVertx;
    }

    /**
     * Creates a new S3 client using system defined AWS credentials and the supplied HttpClient options.
     *
     * @param aVertx A Vert.x instance from which to create the S3 client
     * @param aConfig A configuration for the internal HttpClient
     */
    public S3Client(final Vertx aVertx, final S3ClientOptions aConfig) {
        final Optional<AwsCredentials> credentials = aConfig.getCredentials();

        if (credentials.isPresent()) {
            myCredentials = credentials.get();
        } else {
            myCredentials = new AwsCredentialsProviderChain().getCredentials();
        }

        myHttpClient = getHttpClient(aVertx, aConfig);
        myVertx = aVertx;
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
            }

            return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
        }));
    }

    /**
     * Deletes the S3 resource represented by the supplied key.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     */
    public void delete(final String aBucket, final String aKey, final Handler<AsyncResult<Void>> aHandler) {
        final Promise<Void> promise = Promise.promise();

        // Set the supplied handler as the handler for our response promise
        promise.future().onComplete(aHandler);

        createDeleteRequest(aBucket, aKey).onComplete(deleteRequest -> {
            if (deleteRequest.succeeded()) {
                deleteRequest.result().response(send -> {
                    if (send.succeeded()) {
                        final HttpClientResponse response = send.result();
                        final int statusCode = response.statusCode();

                        if (statusCode == HTTP.NO_CONTENT) {
                            promise.complete();
                        } else {
                            promise.fail(new UnexpectedStatusException(statusCode, response.statusMessage()));
                        }
                    } else {
                        promise.fail(send.cause());
                    }
                }).exceptionHandler(error -> {
                    promise.fail(error);
                }).send();
            } else {
                promise.fail(deleteRequest.cause());
            }
        });
    }

    /**
     * Gets an object, represented by the supplied key, from an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @return A future indicating the success or failure of the GET
     */
    public Future<S3ClientResponse> get(final String aBucket, final String aKey) {
        return createGetRequest(aBucket, aKey).compose(request -> request.send().compose(response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                return Future.succeededFuture(new S3ClientResponseImpl(response));
            }

            return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
        }));
    }

    /**
     * Gets an object, represented by the supplied key, from an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     */
    public void get(final String aBucket, final String aKey, final Handler<AsyncResult<S3ClientResponse>> aHandler) {
        final Promise<S3ClientResponse> promise = Promise.promise();

        // Set the supplied handler as the handler for our response promise
        promise.future().onComplete(aHandler);

        createGetRequest(aBucket, aKey).onComplete(getRequest -> {
            if (getRequest.succeeded()) {
                getRequest.result().response(send -> {
                    if (send.succeeded()) {
                        final HttpClientResponse response = send.result();
                        final int statusCode = response.statusCode();

                        if (statusCode == HTTP.OK) {
                            promise.complete(new S3ClientResponseImpl(response));
                        } else {
                            promise.fail(new UnexpectedStatusException(statusCode, response.statusMessage()));
                        }
                    } else {
                        promise.fail(send.cause());
                    }
                }).exceptionHandler(error -> {
                    promise.fail(error);
                }).send();
            } else {
                promise.fail(getRequest.cause());
            }
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
            }

            return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
        }));
    }

    /**
     * Performs a HEAD request on an object in S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     */
    public void head(final String aBucket, final String aKey, final Handler<AsyncResult<HttpHeaders>> aHandler) {
        final Promise<HttpHeaders> promise = Promise.promise();

        // Set the supplied handler as the handler for our response promise
        promise.future().onComplete(aHandler);

        createHeadRequest(aBucket, aKey).onComplete(headRequest -> {
            if (headRequest.succeeded()) {
                headRequest.result().response(send -> {
                    if (send.succeeded()) {
                        final HttpClientResponse response = send.result();
                        final int statusCode = response.statusCode();

                        if (statusCode == HTTP.OK) {
                            promise.complete(new HttpHeaders(response.headers()));
                        } else {
                            promise.fail(new UnexpectedStatusException(statusCode, response.statusMessage()));
                        }
                    } else {
                        promise.fail(send.cause());
                    }
                }).exceptionHandler(error -> {
                    promise.fail(error);
                }).send();
            } else {
                promise.fail(headRequest.cause());
            }
        });
    }

    /**
     * Performs a list request on an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @return A future with the list response
     */
    public Future<S3BucketList> list(final String aBucket) {
        return createGetRequest(aBucket, LIST_CMD).compose(request -> request.send().compose(response -> {
            final Promise<S3BucketList> promise = Promise.promise();

            // Builds an S3BucketList from the response and completes the promise with it
            buildList(response, promise);

            return promise.future();
        }));
    }

    /**
     * Lists an S3 bucket.
     *
     * @param aBucket A bucket from which to get a listing
     * @param aHandler A response handler
     */
    public void list(final String aBucket, final Handler<AsyncResult<S3BucketList>> aHandler) {
        final Promise<S3BucketList> promise = Promise.promise();

        // Set the supplied handler as the handler for our response promise
        promise.future().onComplete(aHandler);

        createGetRequest(aBucket, LIST_CMD).onComplete(getRequest -> {
            if (getRequest.succeeded()) {
                getRequest.result().response(send -> {
                    if (send.succeeded()) {
                        // Builds an S3BucketList from the response and completes the promise with it
                        buildList(send.result(), promise);
                    } else {
                        promise.fail(send.cause());
                    }
                }).exceptionHandler(error -> {
                    promise.fail(error);
                }).send();
            } else {
                promise.fail(getRequest.cause());
            }
        });
    }

    /**
     * Performs a prefixed LIST request on an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aPrefix A prefix to use to limit which objects are listed
     * @return A future with the LIST results buffer
     */
    public Future<S3BucketList> list(final String aBucket, final String aPrefix) {
        final String prefixedList = PREFIX_LIST_CMD + aPrefix;
        return createGetRequest(aBucket, prefixedList).compose(request -> request.send().compose(response -> {
            final Promise<S3BucketList> promise = Promise.promise();

            // Builds an S3BucketList from the response and completes the promise with it
            buildList(response, promise);

            return promise.future();
        }));
    }

    /**
     * Lists an S3 bucket using the supplied prefix as a filter.
     *
     * @param aBucket An S3 bucket
     * @param aPrefix A prefix to use to limit which objects are listed
     * @param aHandler A response handler
     */
    public void list(final String aBucket, final String aPrefix, final Handler<AsyncResult<S3BucketList>> aHandler) {
        final Promise<S3BucketList> promise = Promise.promise();

        // Set the supplied handler as the handler for our response promise
        promise.future().onComplete(aHandler);

        createGetRequest(aBucket, PREFIX_LIST_CMD + aPrefix).onComplete(getRequest -> {
            if (getRequest.succeeded()) {
                getRequest.result().response(send -> {
                    if (send.succeeded()) {
                        // Builds an S3BucketList from the response and completes the promise with it
                        buildList(send.result(), promise);
                    } else {
                        promise.fail(send.cause());
                    }
                }).exceptionHandler(error -> {
                    promise.fail(error);
                }).send();
            } else {
                promise.fail(getRequest.cause());
            }
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
            }

            return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
        }));
    }

    /**
     * Uploads contents of the Buffer to S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aBuffer A data buffer
     * @param aHandler A response handler
     */
    public void put(final String aBucket, final String aKey, final Buffer aBuffer,
            final Handler<AsyncResult<HttpHeaders>> aHandler) {
        final Promise<HttpHeaders> promise = Promise.promise();

        // Set the supplied handler as the handler for our response promise
        promise.future().onComplete(aHandler);

        createPutRequest(aBucket, aKey).onComplete(putRequest -> {
            if (putRequest.succeeded()) {
                putRequest.result().response(send -> {
                    if (send.succeeded()) {
                        final HttpClientResponse response = send.result();
                        final int statusCode = response.statusCode();

                        if (statusCode == HTTP.OK) {
                            promise.complete(new HttpHeaders(response.headers()));
                        } else {
                            promise.fail(new UnexpectedStatusException(statusCode, response.statusMessage()));
                        }
                    } else {
                        promise.fail(send.cause());
                    }
                }).exceptionHandler(error -> {
                    promise.fail(error);
                }).send(aBuffer);
            } else {
                promise.fail(putRequest.cause());
            }
        });
    }

    /**
     * Puts a buffer into an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 object key
     * @param aBuffer A buffer to PUT
     * @param aMetadata A metadata object
     * @return A future indicating when the buffer has been uploaded
     */
    public Future<HttpHeaders> put(final String aBucket, final String aKey, final Buffer aBuffer,
            final UserMetadata aMetadata) {
        final Future<S3ClientRequest> future = createPutRequest(aBucket, aKey);
        return future.compose(request -> request.setUserMetadata(aMetadata).send(aBuffer).compose(response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                return Future.succeededFuture(new HttpHeaders(response.headers()));
            }

            return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
        }));
    }

    /**
     * Puts a buffer into an S3 bucket and sets the supplied user metadata.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aBuffer A data buffer
     * @param aMetadata User metadata that should be set on the S3 object
     * @param aHandler An upload response handler
     */
    public void put(final String aBucket, final String aKey, final Buffer aBuffer, final UserMetadata aMetadata,
            final Handler<AsyncResult<HttpHeaders>> aHandler) {
        final Promise<HttpHeaders> promise = Promise.promise();

        // Set the supplied handler as the handler for our response promise
        promise.future().onComplete(aHandler);

        createPutRequest(aBucket, aKey).onComplete(putRequest -> {
            if (putRequest.succeeded()) {
                putRequest.result().setUserMetadata(aMetadata).response(send -> {
                    if (send.succeeded()) {
                        final HttpClientResponse response = send.result();
                        final int statusCode = response.statusCode();

                        if (statusCode == HTTP.OK) {
                            promise.complete(new HttpHeaders(response.headers()));
                        } else {
                            promise.fail(new UnexpectedStatusException(statusCode, response.statusMessage()));
                        }
                    } else {
                        promise.fail(send.cause());
                    }
                }).exceptionHandler(error -> {
                    promise.fail(error);
                }).send(aBuffer);
            } else {
                promise.fail(putRequest.cause());
            }
        });
    }

    /**
     * Uploads the file contents to S3. You are responsible for closing the AsyncFile when you're done with it.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 object key
     * @param aFile A file from which to read the buffer
     * @return A future indicating when the buffer has been uploaded
     */
    @SuppressWarnings("PMD.CognitiveComplexity")
    public Future<HttpHeaders> put(final String aBucket, final String aKey, final AsyncFile aFile) {
        return createPutRequest(aBucket, aKey).compose(request -> request.send(aFile).compose(response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                return Future.succeededFuture(new HttpHeaders(response.headers()));
            }

            // Log more details if we get an unexpected result
            response.body(body -> {
                if (body.succeeded()) {
                    try {
                        // Additional details are wrapped in an XML wrapper
                        final String xml = body.result().toString(StandardCharsets.UTF_8);

                        try {
                            LOGGER.error(MessageCodes.VSS_021, aKey, EOL + XmlUtils.format(xml));
                        } catch (final TransformerException details) {
                            LOGGER.error(MessageCodes.VSS_021, aKey, xml);
                        }
                    } catch (final Exception details) { // NOPMD
                        LOGGER.error(MessageCodes.VSS_022, details.getMessage());
                    }
                } else {
                    LOGGER.error(MessageCodes.VSS_022, body.cause().getMessage());
                }
            });

            return Future.failedFuture(new UnexpectedStatusException(statusCode, response.statusMessage()));
        }));
    }

    /**
     * Uploads the file contents to S3. You are responsible for closing the AsyncFile when you're done with it.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aFile A file from which to read the content to be sent
     * @param aHandler An upload response handler
     */
    public void put(final String aBucket, final String aKey, final AsyncFile aFile,
            final Handler<AsyncResult<HttpHeaders>> aHandler) {
        put(aBucket, aKey, aFile, null, aHandler);
    }

    /**
     * Put a streamed buffer into an S3 bucket. You are responsible for closing the AsyncFile when you're done with it.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 object key
     * @param aFile A file from which to read the buffer
     * @param aMetadata A metadata object
     * @return A future indicating when the buffer has been uploaded
     */
    public Future<HttpHeaders> put(final String aBucket, final String aKey, final AsyncFile aFile,
            final UserMetadata aMetadata) {
        final Future<S3ClientRequest> futurePut = createPutRequest(aBucket, aKey);

        return futurePut.compose(request -> request.setUserMetadata(aMetadata).send(aFile).compose(response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                return Future.succeededFuture(new HttpHeaders(response.headers()));
            }

            final String statusMessage = response.statusMessage();
            return Future.failedFuture(new UnexpectedStatusException(statusCode, statusMessage));
        }));
    }

    /**
     * Uploads the file contents to S3. You are responsible for closing the AsyncFile when you're done with it.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aFile A file from which to read the content to be sent
     * @param aMetadata User metadata to set on the uploaded S3 object
     * @param aHandler An upload response handler
     */
    @SuppressWarnings("PMD.CognitiveComplexity")
    public void put(final String aBucket, final String aKey, final AsyncFile aFile, final UserMetadata aMetadata,
            final Handler<AsyncResult<HttpHeaders>> aHandler) {
        final Promise<HttpHeaders> promise = Promise.promise();

        // Set the supplied handler as the handler for our response promise
        promise.future().onComplete(aHandler);

        createPutRequest(aBucket, aKey).onComplete(putRequest -> {
            if (putRequest.succeeded()) {
                final S3ClientRequest request = putRequest.result();

                if (aMetadata != null) {
                    request.setUserMetadata(aMetadata);
                }

                request.response(send -> {
                    if (send.succeeded()) {
                        final HttpClientResponse response = send.result();
                        final int statusCode = response.statusCode();

                        if (statusCode == HTTP.OK) {
                            promise.complete(new HttpHeaders(response.headers()));
                        } else {
                            promise.fail(new UnexpectedStatusException(statusCode, response.statusMessage()));
                        }
                    } else {
                        promise.fail(send.cause());
                    }
                }).exceptionHandler(error -> {
                    promise.fail(error);
                }).send(aFile);
            } else {
                promise.fail(putRequest.cause());
            }
        });
    }

    /**
     * Closes the S3 client.
     *
     * @return The result of closing the client
     */
    public Future<Void> close() {
        return myHttpClient.close();
    }

    /**
     * Gets the Vert.x instance used by the S3 client.
     *
     * @return The underlying Vert.x instance
     */
    public Vertx getVertx() {
        return myVertx;
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
     * Builds a S3BucketList from an S3 response.
     *
     * @param aResponse A response from an HttpClientRequest
     * @param aPromise A promise of completion
     */
    private void buildList(final HttpClientResponse aResponse, final Promise<S3BucketList> aPromise) {
        final int statusCode = aResponse.statusCode();

        if (statusCode == HTTP.OK) {
            aResponse.body(body -> {
                if (body.succeeded()) {
                    try {
                        aPromise.complete(new S3BucketList(body.result()));
                    } catch (final IOException details) {
                        aPromise.fail(details);
                    }
                } else {
                    aPromise.fail(body.cause());
                }
            });
        } else {
            aPromise.fail(new UnexpectedStatusException(statusCode, aResponse.statusMessage()));
        }
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
            if (request.succeeded()) {
                promise.complete(new S3ClientRequest(request.result(), myCredentials));
            } else {
                promise.fail(request.cause());
            }
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
            if (request.succeeded()) {
                promise.complete(new S3ClientRequest(request.result(), myCredentials));
            } else {
                promise.fail(request.cause());
            }
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
            if (request.succeeded()) {
                promise.complete(new S3ClientRequest(request.result(), myCredentials));
            } else {
                promise.fail(request.cause());
            }
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
            if (request.succeeded()) {
                promise.complete(new S3ClientRequest(request.result(), myCredentials));
            } else {
                promise.fail(request.cause());
            }
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
        Objects.requireNonNull(aVertx);
        return aConfig == null ? aVertx.createHttpClient(new S3ClientOptions()) : aVertx.createHttpClient(aConfig);
    }

}
