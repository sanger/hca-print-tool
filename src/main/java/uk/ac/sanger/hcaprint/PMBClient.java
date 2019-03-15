package uk.ac.sanger.hcaprint;

import java.io.*;
import java.net.*;

/**
 * A tool for sending print requests to PrintMyBarcode.
 * @author dr6
 */
public class PMBClient {
    private String location;
    private Proxy proxy;

    /**
     * Constructs a client for sending print requests to the given location.
     * @param location the location where PMB will receive print requests
     * @param proxyString optional proxy string in format {@code "location:port"}
     */
    public PMBClient(String location, String proxyString) {
        this.location = location;
        this.proxy = getProxy(proxyString);
    }

    /**
     * Gets a {@code Proxy} object based on the given string.
     * If the string is null or has no content, null is returned.
     * If given, the string should be of the form {@code "location:port"}.
     * If it cannot be understood, a message will be output to {@code System.err},
     * and then null will be returned.
     * @param proxyString the string describing the proxy, or null (or a blank string) if no proxy is required
     * @return a proxy, or null
     */
    private static Proxy getProxy(String proxyString) {
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
     * Sends a print request
     * @param request the print request to send
     * @exception IOException there was a problem sending the print request
     */
    public void print(PrintRequest request) throws IOException {
        HttpURLConnection connection = openConnection();
        try {
            post(request, connection);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Post the given request over the given connection.
     * @param request the request to send
     * @param connection an open connection
     * @exception IOException there was a problem sending the print request, or if
     *            the response code was not in the 2## range.
     */
    private void post(PrintRequest request, HttpURLConnection connection) throws IOException {
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
                throw new IOException("PrintMyBarcode could not be reached.");
            }
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("Unexpected response code from PrintMyBarcode: "+responseCode
                        + "\n" + getResponseString(connection.getErrorStream()));
            }
        }
    }

    /**
     * Reads from an {@code InputStream} line by line, concatenates the lines and returns the result.
     * @param is stream to read
     * @return the text read from the input stream
     * @exception IOException there was a communication problem
     */
    private static String getResponseString(InputStream is) throws IOException {
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
    private HttpURLConnection openConnection() throws IOException {
        URL url = new URL(this.location);
        URLConnection con;
        if (proxy==null) {
            con = url.openConnection();
        } else {
            con = url.openConnection(proxy);
        }
        return (HttpURLConnection) con;
    }
}
