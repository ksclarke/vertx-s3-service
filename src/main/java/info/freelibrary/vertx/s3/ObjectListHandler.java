
package info.freelibrary.vertx.s3;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX handler for S3's ObjectList response.
 */
class ObjectListHandler extends DefaultHandler {

    /** The element name for an S3 key */
    private static final String KEY = "Key";

    /** List of S3 keys */
    final List<String> myKeys = new ArrayList<>();

    /** Builder for an S3's key value */
    final StringBuilder myKeyText = new StringBuilder(); // NOPMD

    /** The last element encountered while parsing S3 output */
    String myLastElement;

    @Override
    public void characters(final char[] aCharArray, final int aStart, final int aLength) throws SAXException {
        if (myLastElement.equals(KEY)) {
            myKeyText.append(aCharArray, aStart, aLength);
        }
    }

    @Override
    public void startElement(final String aURI, final String aLocalName, final String aQName,
            final Attributes aAttributes) throws SAXException {
        if (aLocalName.equals(KEY)) {
            myKeyText.delete(0, myKeyText.length());
        }

        myLastElement = aLocalName;
    }

    @Override
    public void endElement(final String aURI, final String aLocalName, final String aQName) {
        if (aLocalName.equals(KEY)) {
            myKeys.add(myKeyText.toString());
            myKeyText.delete(0, myKeyText.length());
        }
    }

    /**
     * Gets the S3 keys returned by a List Objects command.
     *
     * @return The S3 keys returned by a List Objects command
     */
    public List<String> getKeys() {
        return myKeys;
    }

}
