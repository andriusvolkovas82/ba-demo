package demo.payment.service;

import demo.payment.model.Payment;

public interface NotificationService {

    void paymentSaved(Payment payment);

}
