package demo.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.payment.dto.PaymentDto;
import demo.payment.dto.PaymentMinimalDto;
import demo.payment.model.CurrencyEnum;
import demo.payment.model.Payment;
import demo.payment.model.PaymentTypeEnum;
import demo.payment.service.LocationService;
import demo.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static demo.payment.TestHelper.getValidPaymentDto;
import static demo.payment.controller.PaymentController.URI_ACTIVE_IDS;
import static demo.payment.controller.PaymentController.URI_ALL;
import static demo.payment.controller.PaymentController.URI_CANCEL;
import static demo.payment.controller.PaymentController.URI_GET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PaymentController.class)
class PaymentControllerTest {

    private static final String URI_ROOT = "/payment";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private LocationService locationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateValidPaymentAndLogCountry() throws Exception {
        mockMvc.perform(post(URI_ROOT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getValidPaymentDto(PaymentTypeEnum.TYPE1))))
                .andExpect(status().isCreated());
        verify(paymentService, times(1)).create(any(PaymentDto.class));
    }

    @Test
    void shouldFailBasicValidationForEmptyPayment() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(URI_ROOT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PaymentDto())))
                .andExpect(status().isBadRequest())
                .andReturn();

        String validationErrors = mvcResult.getResponse().getContentAsString();

        assertTrue(validationErrors.contains("Amount is mandatory"));
        assertTrue(validationErrors.contains("Creditor IBAN is mandatory"));
        assertTrue(validationErrors.contains("Debtor IBAN is mandatory"));
        assertTrue(validationErrors.contains("Type is mandatory"));
        assertTrue(validationErrors.contains("Currency is mandatory"));
    }

    @Test
    void shouldFailBasicValidationForAlmostEmptyPayment() throws Exception {
        PaymentDto payment = new PaymentDto();
        payment.setAmount(new BigDecimal(10));
        MvcResult mvcResult = mockMvc.perform(post(URI_ROOT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String validationErrors = mvcResult.getResponse().getContentAsString();

        assertTrue(validationErrors.contains("Creditor IBAN is mandatory"));
        assertTrue(validationErrors.contains("Debtor IBAN is mandatory"));
        assertTrue(validationErrors.contains("Type is mandatory"));
        assertTrue(validationErrors.contains("Currency is mandatory"));
    }

    @Test
    void shouldFailCustomValidation() throws Exception {
        when(paymentService.create(any(PaymentDto.class))).thenReturn(Collections.singletonList("TYPE1 payment must use currency EUR"));

        PaymentDto payment = getValidPaymentDto(PaymentTypeEnum.TYPE1);
        payment.setCurrency(CurrencyEnum.USD);
        MvcResult mvcResult = mockMvc.perform(post(URI_ROOT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals("TYPE1 payment must use currency EUR", mvcResult.getResponse().getContentAsString());
    }

    @Test
    void shouldReturnActivePaymentIds() throws Exception {
        when(paymentService.getActivePaymentIds(any(BigDecimal.class))).thenReturn(Arrays.asList(1L, 2L, 100L));

        MvcResult mvcResult = mockMvc.perform(get(URI_ROOT + URI_ACTIVE_IDS)
                .param("forAmount", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("[1,2,100]", mvcResult.getResponse().getContentAsString());
    }

    @Test
    void shouldCancelPayment() throws Exception {
        mockMvc.perform(put(URI_ROOT + URI_CANCEL, "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(paymentService, times(1)).cancel(1L);
    }

    @Test
    void shouldReturnPayment() throws Exception {
        PaymentMinimalDto expected = new PaymentMinimalDto(5L, 3.5);
        when(paymentService.getMinimal(any(Long.class))).thenReturn(expected);

        MvcResult mvcResult = mockMvc.perform(get(URI_ROOT + URI_GET, "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        PaymentMinimalDto actual = objectMapper.readValue(response, PaymentMinimalDto.class);
        assertEquals(expected, actual);
        verify(locationService, times(1)).logCountry(anyString());
    }

    @Test
    void shouldReturnAllPayments() throws Exception {
        List<Payment> payments = new ArrayList<>();
        Payment payment = new Payment();
        payment.setCreditorIban("IBAN");
        payments.add(payment);
        payment = new Payment();
        payment.setCreditorIban("IBAN2");
        payments.add(payment);

        when(paymentService.getAll()).thenReturn(payments);

        MvcResult mvcResult = mockMvc.perform(get(URI_ROOT + URI_ALL)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        assertTrue(response.contains("\"creditorIban\":\"IBAN\""));
        assertTrue(response.contains("\"creditorIban\":\"IBAN2\""));
    }

}