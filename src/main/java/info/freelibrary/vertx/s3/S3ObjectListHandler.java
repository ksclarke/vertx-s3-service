
package info.freelibrary.vertx.s3;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX handler for S3's S3ObjectList response.
 */
class S3ObjectListHandler extends DefaultHandler {

    /** The element name for an S3 key. */
    private static final String KEY = "Key";

    /** The element name for the S3 object's last modified timestamp. */
    private static final String LAST_MODIFIED = "LastModified";

    /** The element name for the S3 object's storage class. */
    private static final String STORAGE_CLASS = "StorageClass";

    /** The element name for an S3 list object. */
    private static final String CONTENTS = "Contents";

    /** The element name for S3 object size. */
    private static final String SIZE = "Size";

    /** The element name for the S3 object's ETag. */
    private static final String ETAG = "ETag";

    /** The S3 list in a Java {@link List}. */
    private final List<S3ObjectList> myList = new ArrayList<>();

    /** Temporary storage for characters parsed through SAX */
    private final StringBuilder myValue = new StringBuilder(); // NOPMD

    /** A single S3 list object */
    private S3ObjectList myListObject;

    /** The last element encountered while parsing S3 output */
    private String myCurrentElement;

    @Override
    public void characters(final char[] aCharArray, final int aStart, final int aLength) throws SAXException {
        switch (myCurrentElement) {
            case KEY:
            case ETAG:
            case SIZE:
            case STORAGE_CLASS:
            case LAST_MODIFIED:
                myValue.append(aCharArray, aStart, aLength);
                break;
            default:
                // ignore everything else
        }
    }

    @Override
    public void startElement(final String aURI, final String aLocalName, final String aQName,
        final Attributes aAttributes) throws SAXException {
        switch (aLocalName) {
            case CONTENTS:
                myListObject = new S3ObjectList();
                break;
            case KEY:
            case ETAG:
            case SIZE:
            case STORAGE_CLASS:
            case LAST_MODIFIED:
                myValue.delete(0, myValue.length());
                break;
            default:
                // ignore everything else
        }

        myCurrentElement = aLocalName;
    }

    @Override
    public void endElement(final String aURI, final String aLocalName, final String aQName) throws SAXException {
        switch (aLocalName) {
            case CONTENTS:
                myList.add(myListObject);
                break;
            case KEY:
                myListObject.setKey(getValue());
                break;
            case ETAG:
                myListObject.setETag(getValue());
                break;
            case SIZE:
                myListObject.setSize(getValue());
                break;
            case STORAGE_CLASS:
                myListObject.setStorageClass(getValue());
                break;
            case LAST_MODIFIED:
                try {
                    myListObject.setLastUpdated(getValue());
                } catch (final ParseException details) {
                    throw new SAXException(details);
                }

                break;
            default:
                myValue.delete(0, myValue.length());
        }
    }

    /**
     * Gets S3 list objects.
     *
     * @return The list of S3 objects
     */
    public List<S3ObjectList> getList() {
        return myList;
    }

    /**
     * Gets the current element's text value.
     *
     * @return The current element's text value
     */
    private String getValue() {
        try {
            return myValue.toString();
        } finally {
            myValue.delete(0, myValue.length());
        }
    }

}
