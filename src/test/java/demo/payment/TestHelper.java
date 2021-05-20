package demo.payment;

import demo.payment.dto.PaymentDto;
import demo.payment.model.CurrencyEnum;
import demo.payment.model.PaymentTypeEnum;

import java.math.BigDecimal;

public class TestHelper {

    public static PaymentDto getValidPaymentDto(PaymentTypeEnum type) {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setCurrency(PaymentTypeEnum.TYPE1 == type ? CurrencyEnum.EUR : CurrencyEnum.USD);
        paymentDto.setAmount(PaymentTypeEnum.TYPE3 == type ? new BigDecimal(99) : new BigDecimal(10));
        paymentDto.setType(type);
        paymentDto.setBic("BIC");
        paymentDto.setCreditorIban("CRIBAN");
        paymentDto.setDebtorIban("DBIBAN");
        paymentDto.setDetails("details");
        return paymentDto;
    }

}
