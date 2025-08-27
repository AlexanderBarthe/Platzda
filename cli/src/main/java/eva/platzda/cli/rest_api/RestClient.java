package eva.platzda.cli.rest_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class RestClient {

    private static final String BASE_URL = Dotenv.load().get("backend_restapi_address");

    /**
     *
     * Sends a rest api address to the server.
     *
     * @param url Url (no leading '/'
     * @param method Http method
     * @param body message body (Will be ignored for get requests)
     * @return Answer or error string
     */
    public static String sendRequest(String url, HttpMethod method, String body) {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + url))
                .header("Content-Type", "application/json");

        switch (method) {
            case GET -> builder.GET();
            case DELETE -> builder.DELETE();
            case POST -> builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
            case PUT  -> builder.PUT(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        }

        HttpRequest request = builder.build();
        HttpResponse<String> response;

        try(HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            response = null;
        }

        if(response == null) {
            return "Unknown Error. Is the server running?";
        }


        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Object json = mapper.readValue(response.body(), Object.class);
                ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
                return writer.writeValueAsString(json);
            } catch (Exception e) {
                return response.body();
            }
        } else {
            return "Error " + response.statusCode() + ": " + response.body();
        }

    }

}
