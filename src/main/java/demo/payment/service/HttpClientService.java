package demo.payment.service;

import java.net.http.HttpResponse;
import java.util.Optional;

public interface HttpClientService {

    Optional<HttpResponse<String>> getResponseFromPath(String uri);

}
