package demo.payment.service;

import demo.payment.dto.PaymentDto;
import demo.payment.dto.PaymentMinimalDto;
import demo.payment.model.Payment;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

    List<String> create(PaymentDto paymentDto);

    List<Long> getActivePaymentIds(BigDecimal amount);

    List<Payment> getAll();

    void cancel(Long id);

    PaymentMinimalDto getMinimal(Long id);

}
