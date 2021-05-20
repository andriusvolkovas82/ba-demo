package demo.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Service
public class HttpClientServiceImpl implements HttpClientService{

    private static final Logger log = LoggerFactory.getLogger(HttpClientServiceImpl.class);

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public Optional<HttpResponse<String>> getResponseFromPath(String uri) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI(uri))
                    .build();
            log.debug("Sending request to {}", uri);
            return Optional.of(getHttpClient().send(request, HttpResponse.BodyHandlers.ofString()));
        } catch (InterruptedException | IOException | URISyntaxException e) {
            log.warn(String.format("Unable to call external service at %s. Error:%s", uri, e.getMessage()));
            return Optional.empty();
        }
    }

    protected HttpClient getHttpClient() {
        return httpClient;
    }

}
