package demo.payment.validation;

import demo.payment.dto.PaymentDto;
import demo.payment.model.CurrencyEnum;
import demo.payment.model.PaymentTypeEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PaymentValidatorImpl implements PaymentValidator {

    protected static final String MSG_TYPE1_CURRENCY = "TYPE1 payment must use currency EUR";
    protected static final String MSG_TYPE1_DETAILS = "TYPE1 payment must have details";
    protected static final String MSG_TYPE2_CURRENCY = "TYPE2 payment must use currency USD";
    protected static final String MSG_TYPE3_BIC = "TYPE3 payment must have BIC";

    @Override
    public List<String> validate(PaymentDto payment) {
        List<String> messages = new ArrayList<>();

        if (PaymentTypeEnum.TYPE1 == payment.getType()) {
            if (CurrencyEnum.EUR != payment.getCurrency()) {
                messages.add(MSG_TYPE1_CURRENCY);
            }
            if (payment.getDetails() == null) {
                messages.add(MSG_TYPE1_DETAILS);
            }
        }

        if (PaymentTypeEnum.TYPE2 == payment.getType()) {
            if (CurrencyEnum.USD != payment.getCurrency()) {
                messages.add(MSG_TYPE2_CURRENCY);
            }
        }

        if (PaymentTypeEnum.TYPE3 == payment.getType()) {
            if (payment.getBic() == null) {
                messages.add(MSG_TYPE3_BIC);
            }
        }

        return messages;
    }
}
