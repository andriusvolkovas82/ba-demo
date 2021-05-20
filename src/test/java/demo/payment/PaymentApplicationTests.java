package demo.payment;

import demo.payment.controller.PaymentController;
import demo.payment.dto.PaymentDto;
import demo.payment.dto.PaymentMinimalDto;
import demo.payment.model.CurrencyEnum;
import demo.payment.model.Payment;
import demo.payment.model.PaymentStatusEnum;
import demo.payment.model.PaymentTypeEnum;
import demo.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static demo.payment.TestHelper.getValidPaymentDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PaymentApplicationTests {

    @Autowired
    private PaymentController paymentController;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private HttpServletRequest request;

    @Test
    void shouldCreateValidPayment() {
        paymentController.createPayment(getValidPaymentDto(PaymentTypeEnum.TYPE1));

        List<Payment> payments = paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.ACTIVE, new BigDecimal(10));

        assertEquals(1, payments.size());
        Payment payment = payments.get(0);
        assertEquals(PaymentStatusEnum.ACTIVE, payment.getStatus());
        assertEquals(new BigDecimal(10).setScale(2, RoundingMode.FLOOR), payment.getAmount());
        assertEquals("BIC", payment.getBic());
        assertEquals("CRIBAN", payment.getCreditorIban());
        assertEquals("DBIBAN", payment.getDebtorIban());
        assertEquals("details", payment.getDetails());
        assertNull(payment.getCancellationFee());

        assertEquals(PaymentTypeEnum.TYPE1, payment.getType());
        assertEquals(CurrencyEnum.EUR, payment.getCurrency());
    }

    @Test
    void shouldNotCreateInvalidPayment() {
        PaymentDto payment = getValidPaymentDto(PaymentTypeEnum.TYPE1);
        payment.setType(PaymentTypeEnum.TYPE2);
        paymentController.createPayment(payment);

        List<Payment> payments = paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.ACTIVE, new BigDecimal(10));

        assertTrue(payments.isEmpty());
    }

    @Test
    void shouldGetCorrectCountOfActiveAndInactivePayments() {
        paymentController.createPayment(getValidPaymentDto(PaymentTypeEnum.TYPE1));
        paymentController.createPayment(getValidPaymentDto(PaymentTypeEnum.TYPE2));
        paymentController.createPayment(getValidPaymentDto(PaymentTypeEnum.TYPE3));

        assertEquals(2, paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.ACTIVE, new BigDecimal(10)).size());
        assertEquals(1, paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.ACTIVE, new BigDecimal(99)).size());
        assertEquals(3, paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.ACTIVE, null).size());
        assertTrue(paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.CANCELLED, new BigDecimal(10)).isEmpty());
        assertTrue(paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.CANCELLED, null).isEmpty());

        assertEquals(2, paymentController.getActivePaymentIdsForAmount(new BigDecimal(10)).size());
        assertEquals(1, paymentController.getActivePaymentIdsForAmount(new BigDecimal(99)).size());
    }

    @Test
    void shouldCreateAndCancelPayment() {
        paymentController.createPayment(getValidPaymentDto(PaymentTypeEnum.TYPE1));
        paymentController.createPayment(getValidPaymentDto(PaymentTypeEnum.TYPE1));

        List<Payment> activePayments = paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.ACTIVE, null);

        assertEquals(2, activePayments.size());
        Payment payment = activePayments.get(0);
        Long idToCancel = payment.getId();

        paymentController.cancelPayment(idToCancel);

        assertEquals(1, paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.ACTIVE, new BigDecimal(10)).size());
        assertEquals(1, paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.CANCELLED, new BigDecimal(10)).size());

        PaymentMinimalDto paymentMinimal = paymentController.getPaymentById(idToCancel, request);
        assertEquals(0.0, paymentMinimal.getCancellationFee());
    }

}
