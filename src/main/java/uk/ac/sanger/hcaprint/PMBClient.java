package uk.ac.sanger.hcaprint;

import java.io.*;
import java.net.*;

/**
 * @author dr6
 */
public class PMBClient {
    private String location;
    private Proxy proxy;

    public PMBClient(String location, String proxyString) {
        this.location = location;
        this.proxy = getProxy(proxyString);
    }

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

    public void print(PrintRequest request) throws IOException {
        HttpURLConnection connection = openConnection();
        try {
            post(request, connection);
        } finally {
            connection.disconnect();
        }
    }

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
