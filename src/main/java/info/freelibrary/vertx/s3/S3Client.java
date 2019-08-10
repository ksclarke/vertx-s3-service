
package info.freelibrary.vertx.s3;

import static info.freelibrary.vertx.s3.Constants.PATH_SEP;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

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
public class S3Client {

    /** Default S3 endpoint */
    private static final String DEFAULT_ENDPOINT = "s3.amazonaws.com";

    /** AWS access key */
    private final String myAccessKey;

    /** AWS secret key */
    private final String mySecretKey;

    /** S3 session token */
    private final String mySessionToken;

    /** HTTP client used to interact with S3 */
    private final HttpClient myHttpClient;

    /**
     * Creates a new S3 client using system defined AWS credentials and the default S3 endpoint.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     */
    public S3Client(final Vertx aVertx) {
        this(DefaultAWSCredentialsProviderChain.getInstance().getCredentials(), aVertx.createHttpClient(
                new HttpClientOptions().setDefaultHost(DEFAULT_ENDPOINT)));
    }

    /**
     * Creates a new S3 client using AWS credentials from a system defined profile and the default S3 endpoint.
     *
     * @param aProfile The name of a profile in the system AWS credentials
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     */
    public S3Client(final Vertx aVertx, final Profile aProfile) {
        this(aProfile.getCredentials(), aVertx.createHttpClient(new HttpClientOptions().setDefaultHost(
                DEFAULT_ENDPOINT)));
    }

    /**
     * Creates a new S3 client using system defined AWS credentials and the supplied HttpClient options.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     * @param aConfig A configuration for the internal HttpClient
     */
    public S3Client(final Vertx aVertx, final HttpClientOptions aConfig) {
        this(DefaultAWSCredentialsProviderChain.getInstance().getCredentials(), aVertx.createHttpClient(aConfig));
    }

    /**
     * Creates a new S3 client using AWS credentials from a system defined profile and the supplied HttpClient
     * options.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     * @param aConfig A configuration for the internal HttpClient
     */
    public S3Client(final Vertx aVertx, final Profile aProfile, final HttpClientOptions aConfig) {
        this(aProfile.getCredentials(), aVertx.createHttpClient(aConfig));
    }

    /**
     * Creates a new S3 client using system defined AWS credentials and the supplied S3
     * <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region">endpoint</a>.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     */
    public S3Client(final Vertx aVertx, final String aEndpoint) {
        this(DefaultAWSCredentialsProviderChain.getInstance().getCredentials(), aVertx.createHttpClient(
                new HttpClientOptions().setDefaultHost(aEndpoint)));
    }

    /**
     * Creates a new S3 client using AWS credentials from a system defined profile and the supplied S3
     * <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region">endpoint</a>.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     */
    public S3Client(final Vertx aVertx, final Profile aProfile, final String aEndpoint) {
        this(aProfile.getCredentials(), aVertx.createHttpClient(new HttpClientOptions().setDefaultHost(aEndpoint)));
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
     * Performs a HEAD request on an object in S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     */
    public void head(final String aBucket, final String aKey, final Handler<HttpClientResponse> aHandler) {
        createHeadRequest(aBucket, aKey, aHandler).end();
    }

    /**
     * Gets an object, represented by the supplied key, from an S3 bucket.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     */
    public void get(final String aBucket, final String aKey, final Handler<HttpClientResponse> aHandler) {
        createGetRequest(aBucket, aKey, aHandler).end();
    }

    /**
     * Lists an S3 bucket.
     *
     * @param aBucket A bucket from which to get a listing
     * @param aHandler A response handler
     */
    public void list(final String aBucket, final Handler<HttpClientResponse> aHandler) {
        createGetRequest(aBucket, "?list-type=2", aHandler).end();
    }

    /**
     * Lists an S3 bucket using the supplied prefix as a filter.
     *
     * @param aBucket An S3 bucket
     * @param aPrefix A prefix to use to limit which objects are listed
     * @param aHandler A response handler
     */
    public void list(final String aBucket, final String aPrefix, final Handler<HttpClientResponse> aHandler) {
        createGetRequest(aBucket, "?list-type=2&prefix=" + aPrefix, aHandler).end();
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
            final Handler<HttpClientResponse> aHandler) {
        createPutRequest(aBucket, aKey, aHandler).end(aBuffer);
    }

    /**
     * Uploads the file contents to S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aFile A file to upload
     * @param aHandler A response handler for the upload
     */
    public void put(final String aBucket, final String aKey, final AsyncFile aFile,
            final Handler<HttpClientResponse> aHandler) {
        final S3ClientRequest request = createPutRequest(aBucket, aKey, aHandler);
        final Buffer buffer = Buffer.buffer();

        aFile.endHandler(event -> {
            request.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
            request.end(buffer);
        });

        aFile.handler(data -> {
            buffer.appendBuffer(data);
        });
    }

    /**
     * Uploads the file contents to S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aUpload An HttpServerFileUpload
     * @param aHandler An upload response handler
     */
    public void put(final String aBucket, final String aKey, final HttpServerFileUpload aUpload,
            final Handler<HttpClientResponse> aHandler) {
        final S3ClientRequest request = createPutRequest(aBucket, aKey, aHandler);
        final Buffer buffer = Buffer.buffer();

        aUpload.endHandler(event -> {
            request.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
            request.end(buffer);
        });

        aUpload.handler(data -> {
            buffer.appendBuffer(data);
        });
    }

    /**
     * Deletes the S3 resource represented by the supplied key.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     */
    public void delete(final String aBucket, final String aKey, final Handler<HttpClientResponse> aHandler) {
        createDeleteRequest(aBucket, aKey, aHandler).end();
    }

    /**
     * Creates an S3 PUT request.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @return An S3 PUT request
     */
    protected S3ClientRequest createPutRequest(final String aBucket, final String aKey,
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
    protected S3ClientRequest createHeadRequest(final String aBucket, final String aKey,
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
    protected S3ClientRequest createGetRequest(final String aBucket, final String aKey,
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
    protected S3ClientRequest createDeleteRequest(final String aBucket, final String aKey,
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
            httpClient = aVertx.createHttpClient();
        } else {
            httpClient = aVertx.createHttpClient(aConfig);
        }

        return httpClient;
    }

}
