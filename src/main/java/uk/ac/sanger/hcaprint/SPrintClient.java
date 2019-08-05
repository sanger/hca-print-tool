package uk.ac.sanger.hcaprint;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Client for the SPrint service.
 * @author dr6
 */
public class SPrintClient extends HttpClient {
    private String location;

    public SPrintClient(String location, String proxy) {
        this.location = location;
        setProxy(proxy);
    }

    public void print(PrintRequest request) throws IOException {
        HttpURLConnection connection = openConnection(this.location);
        try {
            post(request, connection);
        } finally {
            connection.disconnect();
        }
    }
}
