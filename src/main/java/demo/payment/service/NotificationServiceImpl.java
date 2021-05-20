package demo.payment.service;

import demo.payment.model.Payment;
import demo.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final PaymentRepository paymentRepository;

    private HttpClientService httpClientService;

    public NotificationServiceImpl(PaymentRepository paymentRepository, HttpClientService httpClientService) {
        this.paymentRepository = paymentRepository;
        this.httpClientService = httpClientService;
    }

    @Override
    public void paymentSaved(Payment payment) {
        if (payment.getType().isNotificationEnabled()) {
            CompletableFuture.runAsync(() -> {
                Optional<HttpResponse<String>> response = httpClientService.getResponseFromPath(payment.getType().getNotificationUri());
                response.ifPresent(httpResponse -> {
                    payment.setNotificationFailed(httpResponse.statusCode() / 100 != 2);
                    log.debug("Saving payment with notificationStatus={} ", payment.getNotificationFailed());
                    paymentRepository.save(payment);
                });
            }).orTimeout(10, TimeUnit.SECONDS);
        }
    }

}
