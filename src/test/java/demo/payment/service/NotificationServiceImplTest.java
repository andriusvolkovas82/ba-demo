package demo.payment.service;

import demo.payment.model.Payment;
import demo.payment.model.PaymentTypeEnum;
import demo.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private HttpResponse<String> httpResponse;

    @Mock
    private HttpClientService httpClientService;

    @Mock
    private PaymentRepository paymentRepository;

    @Captor
    private ArgumentCaptor<Payment> paymentArgumentCaptor;

    @InjectMocks
    private NotificationServiceImpl underTest;

    @Test
    void shouldSaveCorrectNotificationStatusWhenNotificationIsSuccessful() throws InterruptedException {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpClientService.getResponseFromPath(anyString())).thenReturn(Optional.of(httpResponse));
        CountDownLatch countDownLatch = new CountDownLatch(1);
        doAnswer(answer -> {
            countDownLatch.countDown();
            return null;
        }).when(paymentRepository).save(any(Payment.class));
        Payment payment = new Payment();
        payment.setType(PaymentTypeEnum.TYPE1);

        underTest.paymentSaved(payment);

        countDownLatch.await();
        verify(paymentRepository, times(1)).save(paymentArgumentCaptor.capture());
        assertFalse(paymentArgumentCaptor.getValue().getNotificationFailed());
    }

    @Test
    void shouldSaveCorrectNotificationStatusWhenNotificationIsNotSuccessful() throws InterruptedException {
        when(httpResponse.statusCode()).thenReturn(500);
        when(httpClientService.getResponseFromPath(anyString())).thenReturn(Optional.of(httpResponse));
        CountDownLatch countDownLatch = new CountDownLatch(1);
        doAnswer(answer -> {
            countDownLatch.countDown();
            return null;
        }).when(paymentRepository).save(any(Payment.class));
        Payment payment = new Payment();
        payment.setType(PaymentTypeEnum.TYPE1);

        underTest.paymentSaved(payment);

        countDownLatch.await();
        verify(paymentRepository, times(1)).save(paymentArgumentCaptor.capture());
        assertTrue(paymentArgumentCaptor.getValue().getNotificationFailed());
    }

    @Test
    void shouldNotNotifyWhenNotificationNotEnabled() throws InterruptedException {
        Payment payment = new Payment();
        payment.setType(PaymentTypeEnum.TYPE3);

        underTest.paymentSaved(payment);
        Thread.sleep(100);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldNotNotifyWhenNoResponse() throws InterruptedException {
        when(httpClientService.getResponseFromPath(anyString())).thenReturn(Optional.empty());
        Payment payment = new Payment();
        payment.setType(PaymentTypeEnum.TYPE1);

        underTest.paymentSaved(payment);
        Thread.sleep(100);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

}