package demo.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class LocationServiceImpl implements LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationServiceImpl.class);

    private static final String URI_HOST = "https://ipapi.co/";
    private static final String URI_PATH_COUNTRY_NAME = "/country_name";

    private HttpClientService httpClientService;

    public LocationServiceImpl(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    @Override
    public void logCountry(String ip) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(URI_HOST)
                .path(ip)
                .path(URI_PATH_COUNTRY_NAME);

        CompletableFuture.runAsync(() -> httpClientService.getResponseFromPath(uriBuilder.toUriString())
                .ifPresentOrElse(
                        response -> getLogger().info(String.format("Client country is %s", response.body())),
                        () -> getLogger().info("Unable to resolve client country")
                )).orTimeout(10, TimeUnit.SECONDS);
    }

    protected Logger getLogger() {
        return log;
    }

}
