
package info.freelibrary.vertx.s3;

import static info.freelibrary.util.Constants.EMPTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;
import info.freelibrary.vertx.s3.util.MessageCodes;

/**
 * S3 user metadata.
 */
public class UserMetadata {

    /**
     * A metadata logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserMetadata.class, MessageCodes.BUNDLE);

    /**
     * A metadata value delimiter.
     */
    private static final String AWS_VALUE_DELIMITER = ",";

    /**
     * A default locale.
     */
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();

    /**
     * A list of metadata.
     */
    private final List<NameValuePair> myMetadata;

    /**
     * Creates new user metadata for attaching to S3 objects.
     */
    public UserMetadata() {
        myMetadata = new ArrayList<>();
    }

    /**
     * Creates new user metadata using the supplied metadata name and value. AWS canonical metadata rules requires that
     * the supplied name is lower-cased (so this is done automatically).
     *
     * @param aName
     * @param aValue
     */
    public UserMetadata(final String aName, final String aValue) {
        Objects.requireNonNull(aName, LOGGER.getMessage(MessageCodes.VSS_003));

        myMetadata = new ArrayList<>();
        myMetadata.add(new NameValuePair(aName, aValue));
    }

    /**
     * Adds a new key-value pair to the S3 user metadata. If you add a metadata name that already exists, the new value
     * is added to the old one, with a comma as the delimiter.
     *
     * @param aName A metadata property name
     * @param aValue A metadata value
     * @return The user metadata object itself
     */
    @SuppressWarnings("PMD.ForLoopCanBeForeach")
    public UserMetadata add(final String aName, final String aValue) {
        Objects.requireNonNull(aName, LOGGER.getMessage(MessageCodes.VSS_003));

        boolean found = false;

        // Check to make sure we don't already have metadata with the same name
        for (final NameValuePair element : myMetadata) {
            if (element.myName.equals(aName.toLowerCase(DEFAULT_LOCALE))) {
                final String oldValue = element.getValue();
                final String newValue = aValue != null ? StringUtils.trimTo(aValue, "") : "";

                if (!EMPTY.equals(newValue)) {
                    element.setValue(oldValue + AWS_VALUE_DELIMITER + newValue);
                }

                found = true;
            }
        }

        // If we don't have metadata with this name already, add it
        if (!found) {
            myMetadata.add(new NameValuePair(aName, aValue));
        }

        return this;
    }

    /**
     * Remove the user metadata associated with the supplied property name.
     *
     * @param aName A metadata property name
     * @return The user metadata object itself
     */
    public UserMetadata remove(final String aName) {
        Objects.requireNonNull(aName, LOGGER.getMessage(MessageCodes.VSS_003));

        myMetadata.remove(indexOf(aName));

        return this;
    }

    /**
     * Gets the value of the supplied name.
     *
     * @param aName A property name
     * @return The value of the supplied name
     */
    public String getValue(final String aName) {
        Objects.requireNonNull(aName, LOGGER.getMessage(MessageCodes.VSS_003));

        final int index = indexOf(aName);

        return index == -1 ? null : myMetadata.get(index).getValue();
    }

    /**
     * Gets the name at the supplied index position.
     *
     * @param aIndex An index position
     * @return The property name at the supplied index position
     */
    public String getValue(final int aIndex) {
        return myMetadata.get(aIndex).getValue();
    }

    /**
     * Gets the name at the supplied index position.
     *
     * @param aIndex An index position
     * @return The property name at the supplied index position
     */
    public String getName(final int aIndex) {
        return myMetadata.get(aIndex).getName();
    }

    /**
     * The number of metadata properties in the user metadata object.
     *
     * @return The number of metadata properties in the user metadata object
     */
    public int count() {
        return myMetadata.size();
    }

    /**
     * Whether the user metadata contains the supplied metadata name.
     *
     * @param aName A metadata property to check
     * @return True if the metadata property exists; else, false
     */
    public boolean contains(final String aName) {
        Objects.requireNonNull(aName, LOGGER.getMessage(MessageCodes.VSS_003));
        return indexOf(aName) != -1;
    }

    /**
     * Get the index position of the supplied name.
     *
     * @param aName A metadata property name
     * @return The index position of the supplied name
     */
    public int indexOf(final String aName) {
        Objects.requireNonNull(aName, LOGGER.getMessage(MessageCodes.VSS_003));

        final String name = aName.toLowerCase(DEFAULT_LOCALE);

        for (int index = 0; index < myMetadata.size(); index++) {
            if (myMetadata.get(index).getName().equals(name)) {
                return index;
            }
        }

        return -1;
    }

    /**
     * A metadata name-value pair.
     */
    class NameValuePair {

        /**
         * The name part of the name-value pair.
         */
        private final String myName;

        /**
         * The value part of the name-value pair.
         */
        private String myValue;

        /**
         * Creates a new name-value pair.
         *
         * @param aName A metadata name
         * @param aValue A metadata value
         */
        NameValuePair(final String aName, final String aValue) {
            myName = aName.toLowerCase(DEFAULT_LOCALE);
            myValue = aValue;
        }

        /**
         * Gets the metadata name.
         *
         * @return The metadata name
         */
        private String getName() {
            return myName;
        }

        /**
         * Gets the metadata value.
         *
         * @return The metadata value
         */
        private String getValue() {
            return myValue;
        }

        /**
         * Sets the metadata value.
         *
         * @param aValue A metadata value
         * @return The name-value pair
         */
        private NameValuePair setValue(final String aValue) {
            myValue = aValue;
            return this;
        }
    }
}
