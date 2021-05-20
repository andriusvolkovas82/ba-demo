package demo.payment.service;

import demo.payment.dto.PaymentDto;
import demo.payment.dto.PaymentMinimalDto;
import demo.payment.model.Payment;
import demo.payment.model.PaymentStatusEnum;
import demo.payment.repository.PaymentRepository;
import demo.payment.validation.PaymentValidator;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentValidator paymentValidator;

    private final ModelMapper modelMapper;

    private final PaymentRepository paymentRepository;

    private final NotificationService notificationService;

    public PaymentServiceImpl(PaymentValidator paymentValidator, ModelMapper modelMapper, PaymentRepository paymentRepository, NotificationService notificationService) {
        this.paymentValidator = paymentValidator;
        this.modelMapper = modelMapper;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<String> create(PaymentDto paymentDto) {
        List<String> validationMessages = paymentValidator.validate(paymentDto);
        if (validationMessages.isEmpty()) {
            Payment payment = modelMapper.map(paymentDto, Payment.class);
            payment.setStatus(PaymentStatusEnum.ACTIVE);
            paymentRepository.save(payment);
            notificationService.paymentSaved(payment);
        }
        return validationMessages;
    }

    @Override
    public List<Long> getActivePaymentIds(BigDecimal amount) {
        List<Payment> payments = paymentRepository.findAllByStatusAndAmount(PaymentStatusEnum.ACTIVE, amount);
        return payments.stream().map(Payment::getId).collect(Collectors.toList());
    }

    @Override
    public List<Payment> getAll() {
        return StreamSupport.stream(paymentRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public void cancel(Long id) {
        paymentRepository.findById(id).ifPresent(payment -> {
            if (isCancellable(payment)) {
                payment.setCancellationFee(calculateCancellationFee(payment));
                payment.setStatus(PaymentStatusEnum.CANCELLED);
                paymentRepository.save(payment);
            }
        });
    }

    @Override
    public PaymentMinimalDto getMinimal(Long id) {
        PaymentMinimalDto paymentDto = new PaymentMinimalDto();
        paymentRepository.findById(id).ifPresent(payment -> {
            paymentDto.setId(payment.getId());
            paymentDto.setCancellationFee(payment.getCancellationFee());
        });
        return paymentDto;
    }

    private double calculateCancellationFee(Payment payment) {
        LocalDateTime createdAt = payment.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        Duration duration = Duration.between(LocalDateTime.now(), createdAt);
        long difference = Math.abs(duration.toHours());

        return difference * payment.getType().getCancellationCoefficient();
    }

    private boolean isCancellable(Payment payment) {
        return isDateToday(payment.getCreatedAt()) && PaymentStatusEnum.ACTIVE == payment.getStatus();
    }

    private boolean isDateToday(Date date) {
        if (date == null) {
            return false;
        }
        return Instant.now().truncatedTo(ChronoUnit.DAYS)
                .equals(date.toInstant().truncatedTo(ChronoUnit.DAYS));
    }

}
