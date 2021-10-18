
package info.freelibrary.vertx.s3;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;

import info.freelibrary.util.Constants;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;

/**
 * An S3 list object.
 */
@DataObject
public class S3Object {

    /**
     * The S3 object's storage class property.
     */
    private static final String STORAGE_CLASS = "storage_class";

    /**
     * The S3 object's optional eTag property.
     */
    private static final String ETAG = "etag";

    /**
     * The S3 object's last updated date property.
     */
    private static final String LAST_UPDATED = "last_updated";

    /**
     * The S3 object key property.
     */
    private static final String KEY = "key";

    /**
     * The S3 object size property.
     */
    private static final String SIZE = "size";

    /**
     * The type of S3Object.
     */
    public enum Type {

        /**
         * An object that still lives on the local file system.
         */
        FILE, //

        /**
         * An object that lives in local memory.
         */
        BUFFER, //

        /**
         * An object that lives in an S3 bucket.
         */
        STORAGE;

    }

    /**
     * An S3 object key.
     */
    private String myKey;

    /**
     * A last updated date.
     */
    private Instant myLastUpdated;

    /**
     * An eTag.
     */
    private String myETag;

    /**
     * A list of S3 objects.
     */
    private long mySize;

    /**
     * The S3 storage class.
     */
    private String myStorageClass;

    /**
     * The S3 object's data as a buffer.
     */
    private final Buffer myBuffer;

    /**
     * The type of S3 object.
     */
    private final Type myType;

    /**
     * Creates an S3Object wrapper for a file.
     *
     * @param aFilePath A path to a file
     */
    public S3Object(final String aFilePath) {
        myBuffer = Buffer.buffer(aFilePath, StandardCharsets.UTF_8.toString());
        myType = Type.FILE;
        myKey = aFilePath;
    }

    /**
     * Creates an S3Object wrapper for a file.
     *
     * @param aKey A key for the S3 object
     * @param aFilePath A path to a file
     */
    public S3Object(final String aKey, final String aFilePath) {
        myBuffer = Buffer.buffer(aFilePath, StandardCharsets.UTF_8.toString());
        myType = Type.FILE;
        myKey = aKey;
    }

    /**
     * Creates an S3Object wrapper for a buffer.
     *
     * @param aBuffer A buffer of data
     */
    public S3Object(final Buffer aBuffer) {
        myBuffer = aBuffer.copy();
        myType = Type.BUFFER;
    }

    /**
     * Creates an S3Object wrapper for a buffer.
     *
     * @param aKey A key for the S3 object
     * @param aBuffer A buffer of data
     */
    public S3Object(final String aKey, final Buffer aBuffer) {
        myBuffer = aBuffer.copy();
        myType = Type.BUFFER;
        myKey = aKey;
    }

    /**
     * Creates a new S3Object from a JSON serialization of an S3Object.
     *
     * @param aJsonObject A JSON serialization of S3Object
     * @throws IllegalArgumentException If the supplied JSON is not a serialization of this class
     */
    public S3Object(final JsonObject aJsonObject) {
        if (aJsonObject.containsKey(Type.FILE.name())) {
            myBuffer = aJsonObject.getBuffer(Type.FILE.name());
            myType = Type.FILE;
        } else if (aJsonObject.containsKey(Type.BUFFER.name())) {
            myBuffer = aJsonObject.getBuffer(Type.BUFFER.name());
            myType = Type.BUFFER;
        } else {
            throw new IllegalArgumentException();
        }

        addRemainingFields(aJsonObject);
    }

    /**
     * Creates a new S3 object of the storage type.
     */
    S3Object() {
        myType = Type.STORAGE;
        myBuffer = null;
    }

    /**
     * Gets the type of S3 object.
     *
     * @return The type of S3 object
     */
    public Type getSource() {
        return myType;
    }

    /**
     * Sets key on the list object.
     *
     * @param aKey A key
     * @return The list object
     */
    public S3Object setKey(final String aKey) {
        myKey = aKey;
        return this;
    }

    /**
     * Gets the list object key
     *
     * @return The key
     */
    public String getKey() {
        return myKey;
    }

    /**
     * Sets list object ETag.
     *
     * @param aETag A ETag
     * @return The list object
     */
    public S3Object setETag(final String aETag) {
        myETag = aETag;
        return this;
    }

    /**
     * Gets the ETag.
     *
     * @return The ETag
     */
    public String getETag() {
        return myETag;
    }

    /**
     * Sets last updated date.
     *
     * @param aLastUpdated A last updated date
     * @return The list object
     */
    public S3Object setLastUpdated(final Instant aLastUpdated) throws ParseException {
        myLastUpdated = aLastUpdated;
        return this;
    }

    /**
     * Gets the last updated date.
     *
     * @return The last updated date
     */
    public Instant getLastUpdated() {
        return myLastUpdated;
    }

    /**
     * Sets the object size
     *
     * @param aSize A object size
     * @return The list object
     */
    public S3Object setSize(final long aSize) {
        mySize = aSize;
        return this;
    }

    /**
     * Gets the object size.
     *
     * @return The object size
     */
    public long getSize() {
        if (mySize == 0) {
            if (myType == Type.BUFFER) {
                mySize = myBuffer.length();
            } else if (myType == Type.FILE) {
                mySize = new File(myBuffer.toString()).length();
            }
        }

        return mySize;
    }

    /**
     * Sets the storage class.
     *
     * @param aStorageClass A storage class
     * @return The list object
     */
    public S3Object setStorageClass(final String aStorageClass) {
        myStorageClass = aStorageClass;
        return this;
    }

    /**
     * Gets the storage class.
     *
     * @return The storage class
     */
    public String getStorageClass() {
        return myStorageClass;
    }

    /**
     * Gets the S3 object data serialized as JSON.
     *
     * @return A JSON serialization of the S3 object data
     */
    public JsonObject toJson() {
        final JsonObject jsonObject = new JsonObject().put(myType.name(), myBuffer);

        if (myStorageClass != null) {
            jsonObject.put(STORAGE_CLASS, myStorageClass);
        }

        if (myETag != null) {
            jsonObject.put(ETAG, myETag);
        }

        if (myKey != null) {
            jsonObject.put(KEY, myKey);
        }

        if (myLastUpdated != null) {
            jsonObject.put(LAST_UPDATED, myLastUpdated);
        }

        if (mySize > 0) {
            jsonObject.put(SIZE, mySize);
        }

        return jsonObject;
    }

    /**
     * Gets the S3 data as a buffer.
     *
     * @param aFileSystem A file system reference
     * @return The data as a buffer
     */
    public Future<Buffer> asBuffer(final FileSystem aFileSystem) {
        if (Type.BUFFER.equals(myType)) {
            return Future.succeededFuture(myBuffer);
        }

        final Promise<Buffer> promise = Promise.promise();
        final Buffer buffer = Buffer.buffer();

        aFileSystem.open(myBuffer.toString(StandardCharsets.UTF_8), new OpenOptions()).onSuccess(openFile -> {
            openFile.handler(read -> {
                buffer.appendBytes(read.getBytes());
            }).endHandler(end -> {
                openFile.close(close -> {
                    if (close.succeeded()) {
                        promise.complete(buffer);
                    } else {
                        promise.fail(close.cause());
                    }
                });
            }).exceptionHandler(error -> {
                promise.fail(error);
            });
        }).onFailure(error -> promise.fail(error));

        return promise.future();
    }

    /**
     * Gets the data as a local AsyncFile. If you ask for a file for something that was supplied as a buffer, string, or
     * JSON object, a temporary file will be created in your system's temporary files directory. It will be removed up
     * when the file reference is closed.
     *
     * @param aFileSystem A file system reference
     * @return The upload as an AsyncFile
     */
    public Future<AsyncFile> asFile(final FileSystem aFileSystem) {
        final Promise<AsyncFile> promise;

        if (!Type.BUFFER.equals(myType)) {
            promise = Promise.promise();

            aFileSystem.open(myBuffer.toString(StandardCharsets.UTF_8), new OpenOptions()).onSuccess(openFile -> {
                promise.complete(openFile);
            }).onFailure(error -> promise.fail(error));

            return promise.future();
        }

        promise = Promise.promise();

        aFileSystem.createTempFile("s3-upload-", Constants.EMPTY).onComplete(fileCreation -> {
            if (fileCreation.succeeded()) {
                aFileSystem.open(fileCreation.result(), new OpenOptions().setDeleteOnClose(true), open -> {
                    if (open.succeeded()) {
                        final AsyncFile file = open.result();

                        file.exceptionHandler(error -> promise.fail(error)).write(myBuffer, write -> {
                            if (write.succeeded()) {
                                file.setReadPos(0);
                                promise.complete(file);
                            } else {
                                promise.fail(write.cause());
                            }
                        });
                    } else {
                        promise.fail(open.cause());
                    }
                });
            } else {
                promise.fail(fileCreation.cause());
            }
        });

        return promise.future();
    }

    /**
     * Populate the various fields on object construction.
     *
     * @param aJsonObject A serialized form of the S3Object
     */
    private void addRemainingFields(final JsonObject aJsonObject) {
        if (aJsonObject.containsKey(KEY)) {
            myKey = aJsonObject.getString(KEY);
        }

        if (aJsonObject.containsKey(ETAG)) {
            myETag = aJsonObject.getString(ETAG);
        }

        if (aJsonObject.containsKey(LAST_UPDATED)) {
            myLastUpdated = aJsonObject.getInstant(LAST_UPDATED);
        }

        if (aJsonObject.containsKey(SIZE)) {
            mySize = aJsonObject.getInteger(SIZE);
        }

        if (aJsonObject.containsKey(STORAGE_CLASS)) {
            myStorageClass = aJsonObject.getString(STORAGE_CLASS);
        }
    }
}
