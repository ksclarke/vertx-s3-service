
package info.freelibrary.vertx.s3;

import static info.freelibrary.util.Constants.EMPTY;
import static org.junit.Assert.*;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import info.freelibrary.util.StringUtils;

/**
 * Tests of the HttpHeaders class.
 */
public class HttpHeadersTest {

    private static final String[] ALLOWED_HEADERS = new String[] { "authorization", "content-type", "content-length",
        "content-md5", "cache-control", "x-amz-content-sha256", "x-amz-date", "x-amz-security-token",
        "x-amz-user-agent", "x-amz-target", "x-amz-acl", "x-amz-version-id", "x-localstack-target", "x-amz-tagging" };

    private static final String[] ALLOWED_METHODS =
        new String[] { "HEAD", "GET", "PUT", "POST", "DELETE", "OPTIONS", "PATCH" };

    private static final String AMZ_ID = "MzRISOwyjmnup426780B5CBE705517/JypPGXLh0OVFGcJaaO3KW/hRAqKOpIEEp";

    private static final String AMZ_ID_HEADER = "x-amz-id-2";

    private static final String AC_ALLOW_HEADERS = "access-control-allow-headers";

    private static final String AC_ALLOW_METHODS = "access-control-allow-methods";

    private static final String TIMESTAMP = "Fri, 19 Feb 2021 03:16:38 GMT";

    private static final String COMMA = ",";

    private HttpHeaders myHeaders;

    /**
     * Sets up the testing environment.
     */
    @Before
    public void setUp() {
        final Map<String, String> myHeaderMap = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(ALLOWED_HEADERS[1], "text/html; charset=utf-8"),
            new AbstractMap.SimpleEntry<>(ALLOWED_HEADERS[2], "0"),
            new AbstractMap.SimpleEntry<>("etag", "618a996c65e02322bd5b5932c9b05714"),
            new AbstractMap.SimpleEntry<>("last-modified", TIMESTAMP),
            new AbstractMap.SimpleEntry<>("access-control-allow-origin", "*"),
            new AbstractMap.SimpleEntry<>("x-amz-request-id", "426780B5CBE70551"),
            new AbstractMap.SimpleEntry<>(AMZ_ID_HEADER, AMZ_ID),
            new AbstractMap.SimpleEntry<>(AC_ALLOW_METHODS, String.join(COMMA, ALLOWED_METHODS)),
            new AbstractMap.SimpleEntry<>(AC_ALLOW_HEADERS, String.join(COMMA, ALLOWED_HEADERS)),
            new AbstractMap.SimpleEntry<>("access-control-expose-headers", ALLOWED_HEADERS[11]),
            new AbstractMap.SimpleEntry<>("connection", "close"), //
            new AbstractMap.SimpleEntry<>("date", TIMESTAMP), new AbstractMap.SimpleEntry<>("server", "hypercorn-h11"));

        myHeaders = new HttpHeaders(myHeaderMap);
    }

    /**
     * Tests creating a HttpHeader.
     */
    @Test
    public final void testHttpHeadersMap() {
        assertEquals(13, myHeaders.size());
    }

    /**
     * Tests creating a HttpHeader using the package constructor.
     */
    @Test
    public final void testHttpHeadersMultiMap() {
        assertEquals(0, new HttpHeaders(io.vertx.core.http.HttpHeaders.headers()).size());
    }

    /**
     * Tests getting a header value.
     */
    @Test
    public final void testGet() {
        assertEquals(String.valueOf(0), myHeaders.get(ALLOWED_HEADERS[2]));
    }

    /**
     * Tests getting all the values for a header.
     */
    @Test
    public final void testGetAll() {
        assertArrayEquals(ALLOWED_METHODS, myHeaders.getAll(AC_ALLOW_METHODS).toArray(new String[7]));
    }

    /**
     * Tests whether a header can be found.
     */
    @Test
    public final void testContains() {
        assertTrue(myHeaders.contains(ALLOWED_HEADERS[2]));
    }

    /**
     * Tests whether there are any headers.
     */
    @Test
    public final void testIsEmpty() {
        assertTrue(new HttpHeaders().isEmpty());
    }

    /**
     * Tests getting all the header names.
     */
    @Test
    public final void testNames() {
        assertTrue(myHeaders.names().contains(ALLOWED_HEADERS[2]));
    }

    /**
     * Tests adding a header.
     */
    @Test
    public final void testAdd() {
        final String header = UUID.randomUUID().toString();
        assertTrue(myHeaders.add(header, TIMESTAMP).contains(header));
    }

    /**
     * Tests adding multiple headers.
     */
    @Test
    public final void testAddAllHttpHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        final String header = UUID.randomUUID().toString();

        headers.add(header, TIMESTAMP);
        assertEquals(14, myHeaders.addAll(headers).size());
    }

    /**
     * Tests adding multiple headers from a map.
     */
    @Test
    public final void testAddAllMapOfStringString() {
        final Map<String, String> map = new HashMap<>();
        final String header1 = UUID.randomUUID().toString();
        final String header2 = UUID.randomUUID().toString();

        map.put(header1, TIMESTAMP);
        map.put(header2, TIMESTAMP);
        assertEquals(15, myHeaders.addAll(map).size());
    }

    /**
     * Tests setting a header value.
     */
    @Test
    public final void testSet() {
        final String value = UUID.randomUUID().toString();

        myHeaders.set(AMZ_ID_HEADER, value);
        assertEquals(value, myHeaders.get(AMZ_ID_HEADER));
    }

    /**
     * Tests setting multiple headers.
     */
    @Test
    public final void testSetAllHttpHeaders() {
        assertTrue(myHeaders.setAll(new HttpHeaders()).isEmpty());
    }

    /**
     * Tests setting multiple headers from a map.
     */
    @Test
    public final void testSetAllMapOfStringString() {
        assertTrue(new HashMap<String, String>().isEmpty());
    }

    /**
     * Tests removing a header.
     */
    @Test
    public final void testRemove() {
        assertEquals(12, myHeaders.remove(AMZ_ID_HEADER).size());
    }

    /**
     * Tests clearing all the existing headers.
     */
    @Test
    public final void testClear() {
        assertTrue(myHeaders.clear().isEmpty());
    }

    /**
     * Tests getting the number of headers.
     */
    @Test
    public final void testSize() {
        assertEquals(13, myHeaders.size());
    }

    /**
     * Tests turning the collection of headers into a string.
     */
    @Test
    public final void testToString() {
        final String header = UUID.randomUUID().toString();
        final String value = "{}=Fri, 19 Feb 2021 03:16:38 GMT";

        myHeaders.clear();
        myHeaders.add(header, TIMESTAMP);

        assertEquals(StringUtils.format(value, header), myHeaders.toString().replaceAll(System.lineSeparator(), EMPTY));
    }

}
