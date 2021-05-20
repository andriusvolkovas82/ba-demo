package demo.payment.repository;

import demo.payment.model.Payment;
import demo.payment.model.PaymentStatusEnum;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepositoryCustom {

    List<Payment> findAllByStatusAndAmount(PaymentStatusEnum status, BigDecimal amount);

}
