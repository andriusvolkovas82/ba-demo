package demo.payment.validation;

import demo.payment.dto.PaymentDto;
import demo.payment.model.CurrencyEnum;
import demo.payment.model.PaymentTypeEnum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static demo.payment.validation.PaymentValidatorImpl.MSG_TYPE1_CURRENCY;
import static demo.payment.validation.PaymentValidatorImpl.MSG_TYPE1_DETAILS;
import static demo.payment.validation.PaymentValidatorImpl.MSG_TYPE2_CURRENCY;
import static demo.payment.validation.PaymentValidatorImpl.MSG_TYPE3_BIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentValidatorImplTest {

    private final PaymentValidator underTest = new PaymentValidatorImpl();

    @Test
    void validateInvalidPaymentType1() {
        PaymentDto payment = new PaymentDto();
        payment.setType(PaymentTypeEnum.TYPE1);
        payment.setCurrency(CurrencyEnum.USD);

        List<String> validationMessages = underTest.validate(payment);

        assertEquals(2, validationMessages.size());
        assertTrue(validationMessages.contains(MSG_TYPE1_CURRENCY));
        assertTrue(validationMessages.contains(MSG_TYPE1_DETAILS));
    }

    @Test
    void validateValidPaymentType1() {
        PaymentDto payment = new PaymentDto();
        payment.setType(PaymentTypeEnum.TYPE1);
        payment.setCurrency(CurrencyEnum.EUR);
        payment.setDetails(".");

        List<String> validationMessages = underTest.validate(payment);

        assertTrue(validationMessages.isEmpty());
    }

    @Test
    void validateInvalidPaymentType2() {
        PaymentDto payment = new PaymentDto();
        payment.setType(PaymentTypeEnum.TYPE2);
        payment.setCurrency(CurrencyEnum.EUR);

        List<String> validationMessages = underTest.validate(payment);

        assertEquals(1, validationMessages.size());
        assertTrue(validationMessages.contains(MSG_TYPE2_CURRENCY));
    }

    @Test
    void validateValidPaymentType2() {
        PaymentDto payment = new PaymentDto();
        payment.setType(PaymentTypeEnum.TYPE2);
        payment.setCurrency(CurrencyEnum.USD);

        List<String> validationMessages = underTest.validate(payment);

        assertTrue(validationMessages.isEmpty());
    }

    @Test
    void validateInvalidPaymentType3() {
        PaymentDto payment = new PaymentDto();
        payment.setType(PaymentTypeEnum.TYPE3);

        List<String> validationMessages = underTest.validate(payment);

        assertEquals(1, validationMessages.size());
        assertTrue(validationMessages.contains(MSG_TYPE3_BIC));
    }

    @Test
    void validateValidPaymentType3() {
        PaymentDto payment = new PaymentDto();
        payment.setType(PaymentTypeEnum.TYPE3);
        payment.setBic(".");

        List<String> validationMessages = underTest.validate(payment);

        assertTrue(validationMessages.isEmpty());
    }


}