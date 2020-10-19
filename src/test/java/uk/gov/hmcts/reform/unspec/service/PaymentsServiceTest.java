package uk.gov.hmcts.reform.unspec.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.client.request.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.unspec.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.unspec.model.ClaimValue;
import uk.gov.hmcts.reform.unspec.request.RequestData;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class PaymentsServiceTest {

    private static final String SERVICE = "service";
    private static final String SITE_ID = "site_id";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final FeeDto FEE_DATA = FeeDto.builder()
        .version("1")
        .code("CODE")
        .calculatedAmount(BigDecimal.ONE)
        .build();
    private static final PaymentDto PAYMENT_DTO = PaymentDto.builder().reference("RC-1234-1234-1234-1234").build();

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private FeesService feesService;

    @Mock
    private PaymentsClient paymentsClient;

    @Mock
    private RequestData requestData;

    @Mock
    private PaymentsConfiguration paymentsConfiguration;

    @InjectMocks
    private PaymentsService paymentsService;

    @BeforeEach
    void setUp() {
        given(feesService.getFeeDataByClaimValue(any())).willReturn(FEE_DATA);
        given(paymentsClient.createCreditAccountPayment(any(), any())).willReturn(PAYMENT_DTO);
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);
        given(paymentsConfiguration.getService()).willReturn(SERVICE);
        given(paymentsConfiguration.getSiteId()).willReturn(SITE_ID);

        paymentsService = new PaymentsService(
            objectMapper,
            feesService,
            paymentsClient,
            requestData,
            paymentsConfiguration
        );
    }

    @Test
    void shouldCreateCreditAccountPayment_whenValidCaseDetails() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1L)
            .data(Map.of(
                "claimValue", Map.of("statementOfValueInPennies", "500"),
                "pbaNumber", "PBA1234567",
                "caseReference", "case reference",
                "customerReference", "customer reference",
                "description", "description",
                "organisationName", "organisation name"
            ))
            .build();
        var expectedCreditAccountPaymentRequest = CreditAccountPaymentRequest.builder()
            .accountNumber("PBA1234567")
            .amount(FEE_DATA.getCalculatedAmount())
            .caseReference("case reference")
            .ccdCaseNumber("1")
            .customerReference("customer reference")
            .description("description")
            .organisationName("organisation name")
            .service(paymentsConfiguration.getService())
            .siteId(paymentsConfiguration.getSiteId())
            .fees(new FeeDto[]{FEE_DATA})
            .build();
        var expectedClaimValue = ClaimValue.builder()
            .statementOfValueInPennies(BigDecimal.valueOf(500))
            .build();

        PaymentDto paymentResponse = paymentsService.createCreditAccountPayment(caseDetails);

        verify(feesService).getFeeDataByClaimValue(expectedClaimValue);
        verify(paymentsClient).createCreditAccountPayment(AUTH_TOKEN, expectedCreditAccountPaymentRequest);
        assertThat(paymentResponse).isEqualTo(PAYMENT_DTO);
    }
}
