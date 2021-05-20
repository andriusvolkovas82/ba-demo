package demo.payment.controller;

import demo.payment.dto.PaymentDto;
import demo.payment.dto.PaymentMinimalDto;
import demo.payment.model.Payment;
import demo.payment.service.LocationService;
import demo.payment.service.PaymentService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("payment")
@RestController
public class PaymentController {

    protected static final String URI_ACTIVE_IDS = "/activeIds";
    protected static final String URI_ALL = "/all";
    protected static final String URI_CANCEL = "/cancel/{id}";
    protected static final String URI_GET = "/{id}";

    private final PaymentService paymentService;

    private final LocationService locationService;

    public PaymentController(PaymentService paymentService, LocationService locationService) {
        this.paymentService = paymentService;
        this.locationService = locationService;
    }

    @PostMapping()
    public ResponseEntity<String> createPayment(@Valid @RequestBody PaymentDto payment) {
        List<String> validationMessages = paymentService.create(payment);
        if (validationMessages.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(validationMessages.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }
    }

    @GetMapping(URI_ACTIVE_IDS)
    public List<Long> getActivePaymentIdsForAmount(@RequestParam(required = false) BigDecimal forAmount) {
        return paymentService.getActivePaymentIds(forAmount);
    }

    @PutMapping(URI_CANCEL)
    public void cancelPayment(@PathVariable Long id) {
        paymentService.cancel(id);
    }

    @GetMapping(URI_GET)
    public PaymentMinimalDto getPaymentById(@PathVariable Long id, HttpServletRequest request) {
        locationService.logCountry(request.getRemoteAddr());
        return paymentService.getMinimal(id);
    }

    @GetMapping(URI_ALL)
    public List<Payment> getAllPayments() {
        return paymentService.getAll();
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> handleSimpleValidationExceptions(BindException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(", ")));
    }

}
