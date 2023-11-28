package uk.gov.hmcts.reform.civil.controllers.fees;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.client.models.StatusHistoryDto;
import uk.gov.hmcts.reform.payments.request.CardPaymentServiceRequestDTO;
import uk.gov.hmcts.reform.payments.response.CardPaymentServiceRequestResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.controllers.fees.FeesPaymentController.FEES_PAYMENT_REQUEST_URL;
import static uk.gov.hmcts.reform.civil.controllers.fees.FeesPaymentController.FEES_PAYMENT_STATUS_URL;
import static uk.gov.hmcts.reform.civil.enums.FeeType.HEARING;

public class FeesPaymentControllerTest extends BaseIntegrationTest {

    private static final CardPaymentServiceRequestDTO CARD_PAYMENT_SERVICE_REQUEST = CardPaymentServiceRequestDTO.builder()
        .returnUrl("return-url").language("English").amount(new BigDecimal("232.00")).currency("GBP").build();

    @MockBean
    private PaymentsClient paymentsClient;

    @Test
    @SneakyThrows
    public void shouldCreateGovPayPaymentUrlForServiceRequestPayment() {
        CardPaymentServiceRequestResponse response = buildServiceRequestResponse();

        when(paymentsClient
                 .createGovPayCardPaymentRequest("2023-1701090705688", BEARER_TOKEN, CARD_PAYMENT_SERVICE_REQUEST))
            .thenReturn(response);
        doPost(BEARER_TOKEN, "", FEES_PAYMENT_REQUEST_URL, HEARING.name(), "1701090368574910")
            .andExpect(content().json(toJson(response)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void shouldReturnServiceRequestPaymentStatus() {
        PaymentDto response = buildGovPayCardPaymentStatusResponse("Success");
        when(paymentsClient.getGovPayCardPaymentStatus("RC-1701-0909-0602-0418", BEARER_TOKEN))
            .thenReturn(response);
        doGet(BEARER_TOKEN, FEES_PAYMENT_STATUS_URL, HEARING.name(), "RC-1701-0909-0602-0418")
            .andExpect(content().json(toJson(response)))
            .andExpect(status().isOk());
    }

    private PaymentDto buildGovPayCardPaymentStatusResponse(String status) {
        return PaymentDto.builder()
            .externalReference("lbh2ogknloh9p3b4lchngdfg63")
            .paymentReference("RC-1701-0909-0602-0418")
            .status(status)
            .currency("GBP")
            .dateCreated(OffsetDateTime.parse("2023-11-27T13:15:06.313+00:00"))
            .statusHistories(getStatusHistories(status))
            .build();
    }

    private StatusHistoryDto[] getStatusHistories(String status) {

        StatusHistoryDto initiatedHistory = StatusHistoryDto.builder().status("Initiated").build();
        StatusHistoryDto successHistory = StatusHistoryDto.builder().status("Success").build();
        StatusHistoryDto failedHistory = StatusHistoryDto.builder().status("Failed").build();
        List<StatusHistoryDto> histories = new ArrayList<>();
        if (status.equals("Success")) {
            histories.add(initiatedHistory);
            histories.add(successHistory);
        }

        if (status.equals("Failed")) {
            histories.add(initiatedHistory);
            histories.add(failedHistory);
        }
        StatusHistoryDto[] result = new StatusHistoryDto[histories.size()];
        return histories.toArray(result);
    }

    private CardPaymentServiceRequestResponse buildServiceRequestResponse() {
        return new CardPaymentServiceRequestResponse(
            "lbh2ogknloh9p3b4lchngdfg63",
            "RC-1701-0909-0602-0418",
            "Initiated",
            "https://card.payments.service.gov.uk/secure/7b0716b2-40c4-413e-b62e-72c599c91960",
            OffsetDateTime.parse("2023-11-27T13:15:06.313+00:00")
        );
    }

}
