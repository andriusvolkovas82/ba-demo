package demo.payment.service;

import demo.payment.dto.PaymentDto;
import demo.payment.dto.PaymentMinimalDto;
import demo.payment.model.CurrencyEnum;
import demo.payment.model.Payment;
import demo.payment.model.PaymentStatusEnum;
import demo.payment.model.PaymentTypeEnum;
import demo.payment.repository.PaymentRepository;
import demo.payment.validation.PaymentValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static demo.payment.TestHelper.getValidPaymentDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentValidator paymentValidator;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentServiceImpl underTest;

    @Captor
    private ArgumentCaptor<Payment> paymentArgumentCaptor;

    @Test
    void shouldCreateValidPayment() {
        PaymentDto paymentDto = getValidPaymentDto(PaymentTypeEnum.TYPE1);

        Payment payment = new Payment();
        payment.setAmount(new BigDecimal(10));
        payment.setType(PaymentTypeEnum.TYPE1);
        payment.setCurrency(CurrencyEnum.EUR);
        payment.setBic("BIC");
        payment.setCreditorIban("CRIBAN");
        payment.setDebtorIban("DBIBAN");
        payment.setDetails("details");

        when(modelMapper.map(paymentDto, Payment.class)).thenReturn(payment);
        when(paymentValidator.validate(paymentDto)).thenReturn(new ArrayList<>());

        underTest.create(paymentDto);

        verify(paymentValidator, times(1)).validate(any(PaymentDto.class));
        verify(paymentRepository, times(1)).save(paymentArgumentCaptor.capture());
        verify(notificationService, times(1)).paymentSaved(any(Payment.class));
        assertEquals(paymentDto.getAmount(), paymentArgumentCaptor.getValue().getAmount());
        assertEquals(paymentDto.getType(), paymentArgumentCaptor.getValue().getType());
        assertEquals(paymentDto.getCurrency(), paymentArgumentCaptor.getValue().getCurrency());
        assertEquals(PaymentStatusEnum.ACTIVE, paymentArgumentCaptor.getValue().getStatus());
        assertEquals(paymentDto.getBic(), paymentArgumentCaptor.getValue().getBic());
        assertEquals(paymentDto.getCreditorIban(), paymentArgumentCaptor.getValue().getCreditorIban());
        assertEquals(paymentDto.getDebtorIban(), paymentArgumentCaptor.getValue().getDebtorIban());
        assertEquals(paymentDto.getDetails(), paymentArgumentCaptor.getValue().getDetails());
        assertNull(paymentArgumentCaptor.getValue().getCreatedAt());
        assertNull(paymentArgumentCaptor.getValue().getCancellationFee());
        assertNull(paymentArgumentCaptor.getValue().getId());
    }

    @Test
    void shouldNotCreateInvalidPayment() {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setAmount(new BigDecimal(10));
        paymentDto.setType(PaymentTypeEnum.TYPE1);
        paymentDto.setCurrency(CurrencyEnum.USD);

        when(paymentValidator.validate(paymentDto)).thenReturn(Collections.singletonList("validation error message"));

        underTest.create(paymentDto);

        verify(paymentValidator, times(1)).validate(any(PaymentDto.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(modelMapper, never()).map(any(PaymentDto.class), any(Payment.class));

    }

    @Test
    void shouldGetActivePaymentIds() {
        List<Payment> payments = new ArrayList<>();
        Payment payment = new Payment();
        payment.setId(1L);
        payments.add(payment);
        payment = new Payment();
        payment.setId(55L);
        payments.add(payment);
        List<Long> expected = List.of(1L, 55L);

        when(paymentRepository.findAllByStatusAndAmount(eq(PaymentStatusEnum.ACTIVE), any(BigDecimal.class))).thenReturn(payments);

        List<Long> ids = underTest.getActivePaymentIds(new BigDecimal(1));

        verify(paymentRepository, times(1)).findAllByStatusAndAmount(eq(PaymentStatusEnum.ACTIVE), any(BigDecimal.class));
        assertEquals(expected, ids);
    }

    @Test
    void shouldCallRepositoryToGetAllPayments() {
        underTest.getAll();

        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    void shouldNotCancelPaymentIfPaymentNotFound() {
        when(paymentRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        underTest.cancel(1L);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldNotCancelPaymentIfCancelledAndTooLate() {
        Payment payment = new Payment();
        payment.setCreatedAt(getDateLaterByDays(2));
        payment.setStatus(PaymentStatusEnum.CANCELLED);
        when(paymentRepository.findById(any(Long.class))).thenReturn(Optional.of(payment));

        underTest.cancel(1L);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldNotCancelPaymentIfActiveAndTooLate() {
        Payment payment = new Payment();
        payment.setCreatedAt(getDateLaterByDays(100));
        payment.setStatus(PaymentStatusEnum.ACTIVE);
        when(paymentRepository.findById(any(Long.class))).thenReturn(Optional.of(payment));

        underTest.cancel(1L);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldNotCancelPaymentIfCancelledAndNotTooLate() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatusEnum.CANCELLED);
        payment.setCreatedAt(new Date());
        when(paymentRepository.findById(any(Long.class))).thenReturn(Optional.of(payment));

        underTest.cancel(1L);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldNotCancelPaymentIfCreatedAtIsNull() {
        Payment payment = new Payment();
        when(paymentRepository.findById(any(Long.class))).thenReturn(Optional.of(payment));

        underTest.cancel(1L);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldCancelPaymentAndCalculateCancellationFeeForType1() {
        int hoursOfPaymentInSystem = 5;
        double cancellationCoefficient = PaymentTypeEnum.TYPE1.getCancellationCoefficient();
        Payment payment = new Payment();
        payment.setStatus(PaymentStatusEnum.ACTIVE);
        payment.setType(PaymentTypeEnum.TYPE1);
        payment.setCreatedAt(getDateEarlierByHours(hoursOfPaymentInSystem));
        when(paymentRepository.findById(any(Long.class))).thenReturn(Optional.of(payment));

        underTest.cancel(1L);

        verify(paymentRepository, times(1)).save(paymentArgumentCaptor.capture());
        assertEquals(PaymentStatusEnum.CANCELLED, paymentArgumentCaptor.getValue().getStatus());
        assertEquals(cancellationCoefficient * hoursOfPaymentInSystem, paymentArgumentCaptor.getValue().getCancellationFee());
    }

    @Test
    void shouldCancelPaymentAndCalculateCancellationFeeForType2() {
        int hoursOfPaymentInSystem = 1;
        double cancellationCoefficient = PaymentTypeEnum.TYPE2.getCancellationCoefficient();
        Payment payment = new Payment();
        payment.setStatus(PaymentStatusEnum.ACTIVE);
        payment.setType(PaymentTypeEnum.TYPE2);
        payment.setCreatedAt(getDateEarlierByHours(hoursOfPaymentInSystem));
        when(paymentRepository.findById(any(Long.class))).thenReturn(Optional.of(payment));

        underTest.cancel(1L);

        verify(paymentRepository, times(1)).save(paymentArgumentCaptor.capture());
        assertEquals(PaymentStatusEnum.CANCELLED, paymentArgumentCaptor.getValue().getStatus());
        assertEquals(cancellationCoefficient * hoursOfPaymentInSystem, paymentArgumentCaptor.getValue().getCancellationFee());
    }

    @Test
    void shouldCancelPaymentAndCalculateCancellationFeeForType3() {
        int hoursOfPaymentInSystem = 0;
        double cancellationCoefficient = PaymentTypeEnum.TYPE3.getCancellationCoefficient();
        Payment payment = new Payment();
        payment.setStatus(PaymentStatusEnum.ACTIVE);
        payment.setType(PaymentTypeEnum.TYPE3);
        payment.setCreatedAt(getDateEarlierByHours(hoursOfPaymentInSystem));
        when(paymentRepository.findById(any(Long.class))).thenReturn(Optional.of(payment));

        underTest.cancel(1L);

        verify(paymentRepository, times(1)).save(paymentArgumentCaptor.capture());
        assertEquals(PaymentStatusEnum.CANCELLED, paymentArgumentCaptor.getValue().getStatus());
        assertEquals(cancellationCoefficient * hoursOfPaymentInSystem, paymentArgumentCaptor.getValue().getCancellationFee());
    }

    @Test
    void shouldGetPaymentById() {
        Payment payment = new Payment();
        payment.setId(10L);
        payment.setCancellationFee(2.5);

        when(paymentRepository.findById(any(Long.class))).thenReturn(Optional.of(payment));

        PaymentMinimalDto paymentDto = underTest.getMinimal(10L);

        assertEquals(payment.getId(), paymentDto.getId());
        assertEquals(payment.getCancellationFee(), paymentDto.getCancellationFee());
    }

    @Test
    void shouldReturnEmptyPaymentIdNotFound() {
        when(paymentRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        PaymentMinimalDto paymentDto = underTest.getMinimal(10L);

        assertNull(paymentDto.getId());
        assertNull(paymentDto.getCancellationFee());
    }

    private Date getDateLaterByDays(int days) {
        return Date.from(Instant.now().minus(days, ChronoUnit.DAYS));
    }

    private Date getDateEarlierByHours(int hours) {
        LocalDateTime earlierByHours = LocalDateTime.now().minusHours(hours);
        return Date.from(earlierByHours.atZone(ZoneId.systemDefault()).toInstant());
    }

}