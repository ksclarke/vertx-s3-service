
package info.freelibrary.vertx.s3;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TimeZone;

/**
 * An S3 list object.
 */
public class ListObject {

    private static final SimpleDateFormat DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private String myKey;

    private Instant myLastUpdated;

    private String myETag;

    private int mySize;

    private String myStorageClass;

    /**
     * Creates a new S3 list object.
     */
    public ListObject() {
        DATE.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Sets key on the list object.
     *
     * @param aKey A key
     * @return The list object
     */
    public ListObject setKey(final String aKey) {
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
    public ListObject setETag(final String aETag) {
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
    public ListObject setLastUpdated(final String aLastUpdated) throws ParseException {
        myLastUpdated = DATE.parse(aLastUpdated).toInstant();
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
    public ListObject setSize(final String aSize) {
        mySize = Integer.parseInt(aSize);
        return this;
    }

    /**
     * Gets the object size.
     *
     * @return The object size
     */
    public int getSize() {
        return mySize;
    }

    /**
     * Sets the storage class.
     *
     * @param aStorageClass A storage class
     * @return The list object
     */
    public ListObject setStorageClass(final String aStorageClass) {
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
}
