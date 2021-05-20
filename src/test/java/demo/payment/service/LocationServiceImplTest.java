package demo.payment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private Logger logger;

    @Mock
    private HttpResponse<String> httpResponse;

    @Mock
    private HttpClientService httpClientService;

    @Captor
    private ArgumentCaptor<String> loggerArgumentCaptor;

    @InjectMocks
    private final LocationService underTest = new LocationServiceImpl(httpClientService) {
        @Override
        protected Logger getLogger() {
            return logger;
        }
    };

    @Test
    void shouldLogSuccessWhenReceivedResponse() throws InterruptedException {
        when(httpClientService.getResponseFromPath(anyString())).thenReturn(Optional.of(httpResponse));
        CountDownLatch countDownLatch = new CountDownLatch(1);
        doAnswer(answer -> {
            countDownLatch.countDown();
            return null;
        }).when(logger).info(anyString());

        underTest.logCountry("8.8.8.8");

        countDownLatch.await();
        verify(logger, times(1)).info(loggerArgumentCaptor.capture());
        assertTrue(loggerArgumentCaptor.getValue().contains("Client country is"));
    }

    @Test
    void shouldLogEmptyWhenResponseNotReceived() throws InterruptedException {
        when(httpClientService.getResponseFromPath(anyString())).thenReturn(Optional.empty());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        doAnswer(answer -> {
            countDownLatch.countDown();
            return null;
        }).when(logger).info(anyString());

        underTest.logCountry("8.8.8.8");

        countDownLatch.await();
        verify(logger, times(1)).info(loggerArgumentCaptor.capture());
        assertTrue(loggerArgumentCaptor.getValue().contains("Unable to resolve client country"));
    }

}