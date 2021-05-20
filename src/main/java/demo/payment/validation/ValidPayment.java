package demo.payment.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = PaymentValidatorImplicit.class)
@Target({METHOD, PARAMETER})
@Retention(RUNTIME)
public @interface ValidPayment {
    String message() default "Payment must be valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
