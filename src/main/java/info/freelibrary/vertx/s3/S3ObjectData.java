
package info.freelibrary.vertx.s3;

import static info.freelibrary.vertx.s3.S3ObjectData.Type.BUFFER;
import static info.freelibrary.vertx.s3.S3ObjectData.Type.FILE;

import java.nio.charset.StandardCharsets;

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
 * An S3 service interface for the different types of supported S3 data.
 */
@DataObject
public class S3ObjectData {

    /**
     * The types of supported S3 service data.
     */
    public enum Type {
        FILE, BUFFER
    }

    /**
     * The S3 object's data as a buffer.
     */
    private final Buffer myBuffer;

    /**
     * The type of S3 object data
     */
    private final Type myType;

    /**
     * Creates a new S3ObjectData from a JSON serialization.
     *
     * @param aJsonObject A JSON serialization of S3ObjectData
     * @throws IllegalArgumentException If the supplied JSON is not a serialization of this class
     */
    public S3ObjectData(final JsonObject aJsonObject) throws IllegalArgumentException {
        if (aJsonObject.containsKey(FILE.name())) {
            myBuffer = aJsonObject.getBuffer(FILE.name());
            myType = FILE;
        } else if (aJsonObject.containsKey(BUFFER.name())) {
            myBuffer = aJsonObject.getBuffer(BUFFER.name());
            myType = BUFFER;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Creates an S3ObjectData wrapper for a file.
     *
     * @param aFilePath A path to a file
     */
    public S3ObjectData(final String aFilePath) {
        myBuffer = Buffer.buffer(aFilePath, StandardCharsets.UTF_8.toString());
        myType = FILE;
    }

    /**
     * Creates an S3ObjectData wrapper for a buffer.
     *
     * @param aBuffer A buffer for data
     */
    public S3ObjectData(final Buffer aBuffer) {
        myBuffer = aBuffer.copy();
        myType = BUFFER;
    }

    /**
     * Gets what type of data.
     *
     * @return The type of data
     */
    public Type source() {
        return myType;
    }

    /**
     * Gets the S3 data as a buffer.
     *
     * @param aFileSystem A file system reference
     * @return The data as a buffer
     */
    public Future<Buffer> asBuffer(final FileSystem aFileSystem) {
        if (myType.equals(BUFFER)) {
            return Future.succeededFuture(myBuffer);
        } else {
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
        if (myType.equals(BUFFER)) {
            final Promise<AsyncFile> promise = Promise.promise();

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
        } else {
            final Promise<AsyncFile> promise = Promise.promise();

            aFileSystem.open(myBuffer.toString(StandardCharsets.UTF_8), new OpenOptions()).onSuccess(openFile -> {
                promise.complete(openFile);
            }).onFailure(error -> promise.fail(error));

            return promise.future();
        }
    }

    /**
     * Gets the S3 object data serialized as JSON.
     *
     * @return A JSON serialization of the S3 object data
     */
    public JsonObject toJson() {
        return new JsonObject().put(myType.name(), myBuffer);
    }
}
