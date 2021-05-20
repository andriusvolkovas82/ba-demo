package demo.payment.repository;

import demo.payment.model.Payment;
import demo.payment.model.PaymentStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PaymentRepositoryCustomImplTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatusEnum.ACTIVE);
        payment.setAmount(new BigDecimal(1));
        paymentRepository.save(payment);

        payment = new Payment();
        payment.setStatus(PaymentStatusEnum.ACTIVE);
        payment.setAmount(new BigDecimal(2));
        paymentRepository.save(payment);

        payment = new Payment();
        payment.setStatus(PaymentStatusEnum.ACTIVE);
        payment.setAmount(new BigDecimal(2));
        paymentRepository.save(payment);

        payment = new Payment();
        payment.setStatus(PaymentStatusEnum.CANCELLED);
        payment.setAmount(new BigDecimal(1));
        paymentRepository.save(payment);

        payment = new Payment();
        payment.setStatus(PaymentStatusEnum.CANCELLED);
        payment.setAmount(new BigDecimal(10));
        paymentRepository.save(payment);
    }

    @Test
    void shouldFindByStatus() {
        List<Payment> payments = paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.ACTIVE, null);
        assertEquals(3, payments.size());

        payments = paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.CANCELLED, null);
        assertEquals(2, payments.size());
    }

    @Test
    void shouldFindByStatusAndAmount() {
        List<Payment> payments = paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.ACTIVE, new BigDecimal(1));
        assertEquals(1, payments.size());

        payments = paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.ACTIVE, new BigDecimal(2));
        assertEquals(2, payments.size());

        payments = paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.ACTIVE, new BigDecimal(200));
        assertEquals(0, payments.size());
    }

}