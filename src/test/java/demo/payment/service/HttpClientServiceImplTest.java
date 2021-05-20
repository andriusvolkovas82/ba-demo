package demo.payment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpClientServiceImplTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private final HttpClientService underTest = new HttpClientServiceImpl() {
        @Override
        protected HttpClient getHttpClient() {
            return httpClient;
        }
    };

    @Test
    void shouldReturnResponseWhenCallSuccessful() throws IOException, InterruptedException, URISyntaxException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        Optional<HttpResponse<String>> response = underTest.getResponseFromPath("http://localhost");

        verify(httpClient, times(1)).send(any(HttpRequest.class), any());
        assertTrue(response.isPresent());
    }

    @Test
    void shouldReturnEmptyResponseWhenCallFailed() throws IOException, InterruptedException, URISyntaxException {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException());

        Optional<HttpResponse<String>> response = underTest.getResponseFromPath("http://localhost");

        verify(httpClient, times(1)).send(any(HttpRequest.class), any());
        assertTrue(response.isEmpty());
    }

}