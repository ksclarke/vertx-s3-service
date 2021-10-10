
package info.freelibrary.vertx.s3;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import io.vertx.core.buffer.Buffer;

/**
 * A listing of objects in an S3 bucket.
 */
public class S3BucketList implements Iterable<S3Object> {

    /**
     * The S3 object list.
     */
    private List<S3Object> myList;

    /**
     * Creates a new listing of S3 objects.
     *
     * @param aBuffer The XML response from an S3 list request
     * @throws IOException If there is trouble reading the XML response
     */
    public S3BucketList(final Buffer aBuffer) throws IOException {
        this(aBuffer.toString(StandardCharsets.UTF_8));
    }

    /**
     * Creates a new listing of S3 objects.
     *
     * @param aString The XML response from an S3 list request
     * @throws IOException If there is trouble reading the XML response
     */
    public S3BucketList(final String aString) throws IOException {
        final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

        saxParserFactory.setNamespaceAware(true);

        try {
            final SAXParser saxParser = saxParserFactory.newSAXParser();
            final XMLReader xmlReader = saxParser.getXMLReader();
            final S3BucketListHandler s3BucketListHandler = new S3BucketListHandler();

            xmlReader.setContentHandler(s3BucketListHandler);
            xmlReader.parse(new InputSource(new StringReader(aString)));

            myList = Collections.unmodifiableList(s3BucketListHandler.getList());
        } catch (final ParserConfigurationException | SAXException details) {
            throw new IOException(details);
        }
    }

    /**
     * Checks whether the list contains an object with the supplied key.
     *
     * @param aKey An S3 object key
     * @return True if the list contains an object with the supplied key; else, false
     */
    public boolean containsKey(final String aKey) {
        Objects.requireNonNull(aKey);
        return indexOfKey(aKey) != -1;
    }

    /**
     * Gets the S3 object at the supplied list index position.
     *
     * @param aIndex A position in the list
     * @return The list object at the supplied index position
     */
    public S3Object get(final int aIndex) {
        return myList.get(aIndex);
    }

    /**
     * Gets the index position of the S3 object that has the supplied key.
     *
     * @param aKey An S3 object key
     * @return The index position of the S3 object with the supplied key
     */
    public int indexOfKey(final String aKey) {
        Objects.requireNonNull(aKey);

        for (int index = 0; index < myList.size(); index++) {
            if (aKey.equals(myList.get(index).getKey())) {
                return index;
            }
        }

        return -1;
    }

    /**
     * Checks whether the bucket listing is empty.
     *
     * @return True if the bucket list has no contents; else, false
     */
    public boolean isEmpty() {
        return myList.isEmpty();
    }

    @Override
    public Iterator<S3Object> iterator() {
        return myList.iterator();
    }

    @Override
    public Spliterator<S3Object> spliterator() {
        return myList.spliterator();
    }

    @Override
    public void forEach(final Consumer<? super S3Object> aEvent) {
        myList.forEach(aEvent);
    }

    /**
     * Gets the size of the bucket list.
     *
     * @return The size of the bucket list
     */
    public int size() {
        return myList.size();
    }

    /**
     * Converts the bucket list into an array of {@link S3Object}s.
     *
     * @param aArray An array in which to put the bucket list's objects
     * @return The array of {@link S3Object}s
     */
    public S3Object[] toArray(final S3Object... aArray) {
        return myList.toArray(aArray);
    }

}
