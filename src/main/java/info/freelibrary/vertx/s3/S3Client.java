
package info.freelibrary.vertx.s3;

import static info.freelibrary.vertx.s3.Constants.PATH_SEP;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerFileUpload;

/**
 * An S3 client implementation used by the S3Pairtree object.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class S3Client {

    /** Default S3 endpoint */
    public static final String DEFAULT_ENDPOINT = "s3.amazonaws.com";

    private static final Logger LOGGER = LoggerFactory.getLogger(S3Client.class, Constants.BUNDLE_NAME);

    private static final String LIST_CMD = "?list-type=2";

    private static final String PREFIX_LIST_CMD = "?list-type=2&prefix=";

    /** AWS access key */
    private final String myAccessKey;

    /** AWS secret key */
    private final String mySecretKey;

    /** S3 session token */
    private final String mySessionToken;

    /** HTTP client used to interact with S3 */
    private final HttpClient myHttpClient;

    /** Whether the client uses a V2 signature */
    private boolean isV2Signature;

    /**
     * Creates a new S3 client using system defined AWS credentials and the default S3 endpoint.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     */
    public S3Client(final Vertx aVertx) {
        this(DefaultAWSCredentialsProviderChain.getInstance().getCredentials(), getHttpClient(aVertx,
                new HttpClientOptions().setDefaultHost(DEFAULT_ENDPOINT)));
    }

    /**
     * Creates a new S3 client using AWS credentials from a system defined profile and the default S3 endpoint.
     *
     * @param aProfile The name of a profile in the system AWS credentials
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     */
    public S3Client(final Vertx aVertx, final Profile aProfile) {
        this(aProfile.getCredentials(), getHttpClient(aVertx, new HttpClientOptions().setDefaultHost(
                DEFAULT_ENDPOINT)));
    }

    /**
     * Creates a new S3 client using system defined AWS credentials and the supplied HttpClient options.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     * @param aConfig A configuration for the internal HttpClient
     */
    public S3Client(final Vertx aVertx, final HttpClientOptions aConfig) {
        this(DefaultAWSCredentialsProviderChain.getInstance().getCredentials(), getHttpClient(aVertx, aConfig));
    }

    /**
     * Creates a new S3 client using AWS credentials from a system defined profile and the supplied HttpClient
     * options.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     * @param aConfig A configuration for the internal HttpClient
     */
    public S3Client(final Vertx aVertx, final Profile aProfile, final HttpClientOptions aConfig) {
        this(aProfile.getCredentials(), getHttpClient(aVertx, aConfig));
    }

    /**
     * Creates a new S3 client using system defined AWS credentials and the supplied S3
     * <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region">endpoint</a>.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     */
    public S3Client(final Vertx aVertx, final String aEndpoint) {
        this(DefaultAWSCredentialsProviderChain.getInstance().getCredentials(), getHttpClient(aVertx,
                new HttpClientOptions().setDefaultHost(aEndpoint)));
    }

    /**
     * Creates a new S3 client using AWS credentials from a system defined profile and the supplied S3
     * <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region">endpoint</a>.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     */
    public S3Client(final Vertx aVertx, final Profile aProfile, final String aEndpoint) {
        this(aProfile.getCredentials(), getHttpClient(aVertx, new HttpClientOptions().setDefaultHost(aEndpoint)));
    }

    /**
     * Creates a new S3 client using the supplied access and secret keys.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     */
    public S3Client(final Vertx aVertx, final String aAccessKey, final String aSecretKey) {
        this(aVertx, aAccessKey, aSecretKey, null, new HttpClientOptions().setDefaultHost(DEFAULT_ENDPOINT));
    }

    /**
     * Creates a new S3 client using the supplied access key, secret key, and HttpClient options.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @param aConfig A configuration for the internal HttpClient
     */
    public S3Client(final Vertx aVertx, final String aAccessKey, final String aSecretKey,
            final HttpClientOptions aConfig) {
        this(aVertx, aAccessKey, aSecretKey, null, aConfig);
    }

    /**
     * Creates a new S3 client using the supplied access key, secret key, and S3
     * <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region">endpoint</a>.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @param aEndpoint An S3 endpoint
     */
    public S3Client(final Vertx aVertx, final String aAccessKey, final String aSecretKey, final String aEndpoint) {
        this(aVertx, aAccessKey, aSecretKey, null, new HttpClientOptions().setDefaultHost(aEndpoint));
    }

    /**
     * Creates a new S3 client using the supplied access key, secret key, session token, and HttpClient options.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @param aSessionToken An S3 session token
     * @param aConfig A configuration of HTTP options to use when connecting
     */
    public S3Client(final Vertx aVertx, final String aAccessKey, final String aSecretKey, final String aSessionToken,
            final HttpClientOptions aConfig) {
        this(getCredentials(aAccessKey, aSecretKey, aSessionToken), getHttpClient(aVertx, aConfig));
    }

    /**
     * Creates a new S3 client using the supplied access key, secret key, session token, and S3
     * <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region">endpoint</a>.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @param aSessionToken An S3 session token
     * @param aEndpoint An S3 endpoint
     */
    public S3Client(final Vertx aVertx, final String aAccessKey, final String aSecretKey, final String aSessionToken,
            final String aEndpoint) {
        this(getCredentials(aAccessKey, aSecretKey, aSessionToken), getHttpClient(aVertx, new HttpClientOptions()
                .setDefaultHost(aEndpoint)));
    }

    /**
     * Creates a new S3 client from the supplied AWS credentials and HttpClient.
     *
     * @param aCredentials AWS credentials for the S3Client
     * @param aHttpClient A Vert.x HttpClient to use from the S3Client
     */
    protected S3Client(final AWSCredentials aCredentials, final HttpClient aHttpClient) {
        if (aCredentials instanceof AWSSessionCredentials) {
            mySessionToken = ((AWSSessionCredentials) aCredentials).getSessionToken();
        } else {
            mySessionToken = null;
        }

        myAccessKey = aCredentials.getAWSAccessKeyId();
        mySecretKey = aCredentials.getAWSSecretKey();

        myHttpClient = aHttpClient;
    }

    /**
     * Sets the client to use the almost obsolete V2 authentication signature.
     *
     * @return The S3 client.
     */
    public S3Client useV2Signature(final boolean aV2Signature) {
        isV2Signature = aV2Signature;
        return this;
    }

    /**
     * Performs a HEAD request on an object in S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     */
    public void head(final String aBucket, final String aKey, final Handler<HttpClientResponse> aHandler) {
        createHeadRequest(aBucket, aKey, aHandler).useV2Signature(isV2Signature).end();
    }

    /**
     * Performs a HEAD request on an object in S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @param aExceptionHandler An exception handler
     */
    public void head(final String aBucket, final String aKey, final Handler<HttpClientResponse> aHandler,
            final Handler<Throwable> aExceptionHandler) {
        createHeadRequest(aBucket, aKey, aHandler).exceptionHandler(aExceptionHandler).useV2Signature(isV2Signature)
                .end();
    }

    /**
     * Gets an object, represented by the supplied key, from an S3 bucket. Logs any exceptions.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     */
    public void get(final String aBucket, final String aKey, final Handler<HttpClientResponse> aHandler) {
        createGetRequest(aBucket, aKey, aHandler).useV2Signature(isV2Signature).end();
    }

    /**
     * Gets an object, represented by the supplied key, from an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @param aExceptionHandler An exception handler
     */
    public void get(final String aBucket, final String aKey, final Handler<HttpClientResponse> aHandler,
            final Handler<Throwable> aExceptionHandler) {
        createGetRequest(aBucket, aKey, aHandler).exceptionHandler(aExceptionHandler).useV2Signature(isV2Signature)
                .end();
    }

    /**
     * Lists an S3 bucket. Logs any exceptions.
     *
     * @param aBucket A bucket from which to get a listing
     * @param aHandler A response handler
     */
    public void list(final String aBucket, final Handler<HttpClientResponse> aHandler) {
        createGetRequest(aBucket, LIST_CMD, aHandler).useV2Signature(isV2Signature).end();
    }

    /**
     * Lists an S3 bucket.
     *
     * @param aBucket A bucket from which to get a listing
     * @param aHandler A response handler
     * @param aExceptionHandler An exception handler
     */
    public void list(final String aBucket, final Handler<HttpClientResponse> aHandler,
            final Handler<Throwable> aExceptionHandler) {
        createGetRequest(aBucket, LIST_CMD, aHandler).exceptionHandler(aExceptionHandler).useV2Signature(
                isV2Signature).end();
    }

    /**
     * Lists an S3 bucket using the supplied prefix as a filter. Logs any exceptions.
     *
     * @param aBucket An S3 bucket
     * @param aPrefix A prefix to use to limit which objects are listed
     * @param aHandler A response handler
     */
    public void list(final String aBucket, final String aPrefix, final Handler<HttpClientResponse> aHandler) {
        createGetRequest(aBucket, PREFIX_LIST_CMD + aPrefix, aHandler).useV2Signature(isV2Signature).end();
    }

    /**
     * Lists an S3 bucket using the supplied prefix as a filter.
     *
     * @param aBucket An S3 bucket
     * @param aPrefix A prefix to use to limit which objects are listed
     * @param aHandler A response handler
     * @param aExceptionHandler An exception handler
     */
    public void list(final String aBucket, final String aPrefix, final Handler<HttpClientResponse> aHandler,
            final Handler<Throwable> aExceptionHandler) {
        createGetRequest(aBucket, PREFIX_LIST_CMD + aPrefix, aHandler).exceptionHandler(aExceptionHandler)
                .useV2Signature(isV2Signature).end();
    }

    /**
     * Uploads contents of the Buffer to S3. Logs any exceptions.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aBuffer A data buffer
     * @param aHandler A response handler
     */
    public void put(final String aBucket, final String aKey, final Buffer aBuffer,
            final Handler<HttpClientResponse> aHandler) {
        createPutRequest(aBucket, aKey, aHandler).useV2Signature(isV2Signature).end(aBuffer);
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
            final Handler<HttpClientResponse> aHandler, final Handler<Throwable> aExceptionHandler) {
        createPutRequest(aBucket, aKey, aHandler).exceptionHandler(aExceptionHandler).useV2Signature(isV2Signature)
                .end(aBuffer);
    }

    /**
     * Uploads contents of the Buffer to S3. Logs any exceptions.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aBuffer A data buffer
     * @param aMetadata User metadata that should be set on the S3 object
     * @param aHandler A response handler
     */
    public void put(final String aBucket, final String aKey, final Buffer aBuffer, final UserMetadata aMetadata,
            final Handler<HttpClientResponse> aHandler) {
        createPutRequest(aBucket, aKey, aHandler).setUserMetadata(aMetadata).useV2Signature(isV2Signature).end(
                aBuffer);
    }

    /**
     * Uploads contents of the Buffer to S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aBuffer A data buffer
     * @param aMetadata User metadata that should be set on the S3 object
     * @param aHandler A response handler
     * @param aExceptionHandler An exception handler
     */
    public void put(final String aBucket, final String aKey, final Buffer aBuffer, final UserMetadata aMetadata,
            final Handler<HttpClientResponse> aHandler, final Handler<Throwable> aExceptionHandler) {
        createPutRequest(aBucket, aKey, aHandler).exceptionHandler(aExceptionHandler).setUserMetadata(aMetadata)
                .useV2Signature(isV2Signature).end(aBuffer);
    }

    /**
     * Uploads the file contents to S3. Logs any exceptions.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aFile A file to upload
     * @param aHandler A response handler for the upload
     */
    public void put(final String aBucket, final String aKey, final AsyncFile aFile,
            final Handler<HttpClientResponse> aHandler) {
        put(aBucket, aKey, aFile, null, aHandler);
    }

    /**
     * Uploads the file contents to S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aFile A file to upload
     * @param aHandler A response handler for the upload
     * @param aExceptionHandler An exception handler
     */
    public void put(final String aBucket, final String aKey, final AsyncFile aFile,
            final Handler<HttpClientResponse> aHandler, final Handler<Throwable> aExceptionHandler) {
        put(aBucket, aKey, aFile, null, aHandler, aExceptionHandler);
    }

    /**
     * Uploads the file contents to S3. Logs any exceptions.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aFile A file to upload
     * @param aMetadata User metadata to set on the S3 object
     * @param aHandler A response handler for the upload
     */
    public void put(final String aBucket, final String aKey, final AsyncFile aFile, final UserMetadata aMetadata,
            final Handler<HttpClientResponse> aHandler) {
        put(aBucket, aKey, aFile, aMetadata, aHandler, null);
    }

    /**
     * Uploads the file contents to S3. Logs any exceptions.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aFile A file to upload
     * @param aMetadata User metadata to set on the S3 object
     * @param aHandler A response handler for the upload
     * @param aExceptionHandler An exception handler for the upload
     */
    public void put(final String aBucket, final String aKey, final AsyncFile aFile, final UserMetadata aMetadata,
            final Handler<HttpClientResponse> aHandler, final Handler<Throwable> aExceptionHandler) {
        final S3ClientRequest request = createPutRequest(aBucket, aKey, aHandler);
        final Buffer buffer = Buffer.buffer();

        if (aMetadata != null) {
            request.setUserMetadata(aMetadata);
        }

        request.useV2Signature(isV2Signature);

        aFile.handler(data -> {
            buffer.appendBuffer(data);
        });

        aFile.endHandler(event -> {
            request.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
            request.end(buffer);
        });

        // If we have an exception handler, use it; else, just log any exceptions
        if (aExceptionHandler == null) {
            request.exceptionHandler(exception -> {
                LOGGER.error(exception, exception.getMessage());
            });
        } else {
            request.exceptionHandler(aExceptionHandler);
        }
    }

    /**
     * Uploads the file contents to S3. Logs any exceptions.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aUpload An HttpServerFileUpload
     * @param aHandler An upload response handler
     */
    public void put(final String aBucket, final String aKey, final HttpServerFileUpload aUpload,
            final Handler<HttpClientResponse> aHandler) {
        put(aBucket, aKey, aUpload, null, aHandler);
    }

    /**
     * Uploads the file contents to S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aUpload An HttpServerFileUpload
     * @param aHandler An upload response handler
     * @param aExceptionHandler An exception handler
     */
    public void put(final String aBucket, final String aKey, final HttpServerFileUpload aUpload,
            final Handler<HttpClientResponse> aHandler, final Handler<Throwable> aExceptionHandler) {
        put(aBucket, aKey, aUpload, null, aHandler, aExceptionHandler);
    }

    /**
     * Uploads the file contents to S3. Logs any exceptions.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aUpload An HttpServerFileUpload
     * @param aMetadata User metadata to set on the uploaded S3 object
     * @param aHandler An upload response handler
     */
    public void put(final String aBucket, final String aKey, final HttpServerFileUpload aUpload,
            final UserMetadata aMetadata, final Handler<HttpClientResponse> aHandler) {
        put(aBucket, aKey, aUpload, aMetadata, aHandler, null);
    }

    /**
     * Uploads the file contents to S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aUpload An HttpServerFileUpload
     * @param aMetadata User metadata to set on the uploaded S3 object
     * @param aHandler An upload response handler
     * @param aExceptionHandler An upload exception handler
     */
    public void put(final String aBucket, final String aKey, final HttpServerFileUpload aUpload,
            final UserMetadata aMetadata, final Handler<HttpClientResponse> aHandler,
            final Handler<Throwable> aExceptionHandler) {
        final S3ClientRequest request = createPutRequest(aBucket, aKey, aHandler);
        final Buffer buffer = Buffer.buffer();

        if (aMetadata != null) {
            request.setUserMetadata(aMetadata);
        }

        request.useV2Signature(isV2Signature);

        aUpload.handler(data -> {
            buffer.appendBuffer(data);
        });

        aUpload.endHandler(event -> {
            request.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
            request.end(buffer);
        });

        if (aExceptionHandler == null) {
            request.exceptionHandler(exception -> {
                LOGGER.error(exception, exception.getMessage());
            });
        } else {
            request.exceptionHandler(aExceptionHandler);
        }
    }

    /**
     * Deletes the S3 resource represented by the supplied key. Logs any exceptions.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     */
    public void delete(final String aBucket, final String aKey, final Handler<HttpClientResponse> aHandler) {
        createDeleteRequest(aBucket, aKey, aHandler).useV2Signature(isV2Signature).end();
    }

    /**
     * Deletes the S3 resource represented by the supplied key.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @param aExceptionHandler An exception handler
     */
    public void delete(final String aBucket, final String aKey, final Handler<HttpClientResponse> aHandler,
            final Handler<Throwable> aExceptionHandler) {
        createDeleteRequest(aBucket, aKey, aHandler).exceptionHandler(aExceptionHandler).useV2Signature(isV2Signature)
                .end();
    }

    /**
     * Creates an S3 PUT request.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @return An S3 PUT request
     */
    private S3ClientRequest createPutRequest(final String aBucket, final String aKey,
            final Handler<HttpClientResponse> aHandler) {
        @SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "deprecation" })
        final HttpClientRequest httpRequest = myHttpClient.put(PATH_SEP + aBucket + PATH_SEP + aKey, aHandler);
        return new S3ClientRequest("PUT", aBucket, aKey, httpRequest, myAccessKey, mySecretKey, mySessionToken);
    }

    /**
     * Creates an S3 HEAD request.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @return A S3 client HEAD request
     */
    private S3ClientRequest createHeadRequest(final String aBucket, final String aKey,
            final Handler<HttpClientResponse> aHandler) {
        @SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "deprecation" })
        final HttpClientRequest httpRequest = myHttpClient.head(PATH_SEP + aBucket + PATH_SEP + aKey, aHandler);
        return new S3ClientRequest("HEAD", aBucket, aKey, httpRequest, myAccessKey, mySecretKey, mySessionToken);
    }

    /**
     * Creates an S3 GET request.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @return A S3 client GET request
     */
    private S3ClientRequest createGetRequest(final String aBucket, final String aKey,
            final Handler<HttpClientResponse> aHandler) {
        @SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "deprecation" })
        final HttpClientRequest httpRequest = myHttpClient.get(PATH_SEP + aBucket + PATH_SEP + aKey, aHandler);
        return new S3ClientRequest("GET", aBucket, aKey, httpRequest, myAccessKey, mySecretKey, mySessionToken);
    }

    /**
     * Creates an S3 DELETE request.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler An S3 handler
     * @return An S3 client request
     */
    private S3ClientRequest createDeleteRequest(final String aBucket, final String aKey,
            final Handler<HttpClientResponse> aHandler) {
        @SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "deprecation" })
        final HttpClientRequest httpRequest = myHttpClient.delete(PATH_SEP + aBucket + PATH_SEP + aKey, aHandler);
        return new S3ClientRequest("DELETE", aBucket, aKey, httpRequest, myAccessKey, mySecretKey, mySessionToken);
    }

    /**
     * Closes the S3 client.
     */
    public void close() {
        myHttpClient.close();
    }

    /**
     * A convenience method to get AWS credentials for some supplied information.
     *
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @param aSessionToken A token from an existing session
     * @return AWS credentials representing the supplied information
     */
    private static AWSCredentials getCredentials(final String aAccessKey, final String aSecretKey,
            final String aSessionToken) {
        final AWSCredentials credentials;

        if (aSessionToken != null) {
            credentials = new BasicSessionCredentials(aAccessKey, aSecretKey, aSessionToken);
        } else {
            credentials = new BasicAWSCredentials(aAccessKey, aSecretKey);
        }

        return credentials;
    }

    /**
     * A convenience method to create a new HttpClient for use by our S3 client.
     *
     * @param aVertx A Vert.x instance
     * @param aConfig A HttpClient configuration
     * @return A newly created HttpClient
     */
    private static HttpClient getHttpClient(final Vertx aVertx, final HttpClientOptions aConfig) {
        final HttpClient httpClient;

        if (aConfig == null) {
            httpClient = aVertx.createHttpClient(new HttpClientOptions().setSsl(true).setDefaultPort(443));
        } else {
            httpClient = aVertx.createHttpClient(aConfig.setSsl(true).setDefaultPort(443));
        }

        return httpClient;
    }

}
