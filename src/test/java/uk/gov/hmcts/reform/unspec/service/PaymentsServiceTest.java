package uk.gov.hmcts.reform.unspec.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.client.request.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.unspec.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ClaimValue;
import uk.gov.hmcts.reform.unspec.request.RequestData;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.unspec.enums.PbaNumber.PBA0077597;

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
            feesService,
            paymentsClient,
            requestData,
            paymentsConfiguration
        );
    }

    @Test
    void shouldCreateCreditAccountPayment_whenValidCaseDetails() {
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("000LR001")
            .ccdCaseReference(12345L)
            .pbaNumber(PBA0077597)
            .build();
        var expectedCreditAccountPaymentRequest = CreditAccountPaymentRequest.builder()
            .accountNumber("PBA0077597")
            .amount(FEE_DATA.getCalculatedAmount())
            .caseReference("000LR001")
            .ccdCaseNumber("12345")
            .customerReference("Test Customer Reference")
            .description("Claim issue payment")
            .organisationName("Test Organisation Name")
            .service(SERVICE)
            .siteId(SITE_ID)
            .fees(new FeeDto[]{FEE_DATA})
            .build();
        var expectedClaimValue = ClaimValue.builder()
            .statementOfValueInPennies(BigDecimal.valueOf(10000))
            .build();

        PaymentDto paymentResponse = paymentsService.createCreditAccountPayment(caseData);

        verify(feesService).getFeeDataByClaimValue(expectedClaimValue);
        verify(paymentsClient).createCreditAccountPayment(AUTH_TOKEN, expectedCreditAccountPaymentRequest);
        assertThat(paymentResponse).isEqualTo(PAYMENT_DTO);
    }
}
