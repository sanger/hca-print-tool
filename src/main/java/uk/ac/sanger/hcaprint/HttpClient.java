package uk.ac.sanger.hcaprint;

import java.io.*;
import java.net.*;

/**
 * Http client class for sending post requests.
 * @author dr6
 */
public class HttpClient {
    private Proxy proxy;

    protected void setProxy(String proxy) {
        setProxy(toProxy(proxy));
    }

    protected void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    /**
     * Creates an instance of {@code Proxy} based on the given string.
     * The string should be an address and a port number, separated by a colon.
     * If the string is invalid (or null), this method will return null.
     * @param proxyString the string describing the proxy
     * @return a new proxy, or null if the given string is invalid
     */
    protected static Proxy toProxy(String proxyString) {
        if (proxyString==null) {
            return null;
        }
        proxyString = proxyString.trim();
        if (proxyString.isEmpty()) {
            return null;
        }
        int c = proxyString.indexOf(':');
        if (c <= 0) {
            System.err.println("Invalid proxy string: "+proxyString);
            return null;
        }
        int port;
        try {
            port = Integer.parseInt(proxyString.substring(c+1));
        } catch (NumberFormatException e) {
            System.err.println("Invalid proxy string: "+proxyString);
            e.printStackTrace();
            return null;
        }
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyString.substring(0, c), port));
    }

    /**
     * Posts the given request over the given connection.
     * @param request the request to send
     * @param connection an open connection
     * @exception IOException there was a problem sending the print request, or if
     *            the response code was not in the 2## range.
     */
    protected void post(Object request, HttpURLConnection connection) throws IOException {
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.connect();
        try (OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream())) {
            out.write(request.toString());
            out.flush();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new IOException("Print service could not be reached.");
            }
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("Unexpected response code from print service: "+responseCode
                        + "\n" + getResponseString(connection.getErrorStream()));
            }
        }
    }

    /**
     * Reads from an {@code InputStream} line by line, concatenates the lines and returns the result.
     * Returns null if the given {@code InputStream} is null.
     * @param is stream to read
     * @return the text read from the input stream
     * @exception IOException there was a communication problem
     */
    protected static String getResponseString(InputStream is) throws IOException {
        if (is==null) {
            return null;
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    /**
     * Opens a new connection from the url specified by this instance's location string.
     * If a proxy was specified, it will be included in the connection.
     * @return an open {@code HttpURLConnection}.
     * @exception IOException there was a problem opening the connection
     */
    protected HttpURLConnection openConnection(String location) throws IOException {
        URL url = new URL(location);
        URLConnection con;
        if (proxy==null) {
            con = url.openConnection();
        } else {
            con = url.openConnection(proxy);
        }
        return (HttpURLConnection) con;
    }
}
