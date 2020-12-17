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
import uk.gov.hmcts.reform.unspec.model.Fee;
import uk.gov.hmcts.reform.unspec.model.common.DynamicList;
import uk.gov.hmcts.reform.unspec.model.common.DynamicListElement;

import java.math.BigDecimal;

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
    private static final Fee FEE_DATA = Fee.builder()
        .version("1")
        .code("CODE")
        .calculatedAmountInPence(BigDecimal.valueOf(100))
        .build();
    private static final PaymentDto PAYMENT_DTO = PaymentDto.builder().reference("RC-1234-1234-1234-1234").build();
    private static final String SUCCESSFUL_PBA = "PBA0077597";

    @Mock
    private PaymentsClient paymentsClient;

    @Mock
    private PaymentsConfiguration paymentsConfiguration;

    @InjectMocks
    private PaymentsService paymentsService;

    @BeforeEach
    void setUp() {
        given(paymentsClient.createCreditAccountPayment(any(), any())).willReturn(PAYMENT_DTO);
        given(paymentsConfiguration.getService()).willReturn(SERVICE);
        given(paymentsConfiguration.getSiteId()).willReturn(SITE_ID);
    }

    @Test
    void shouldCreateCreditAccountPayment_whenValidCaseDetails() {
        DynamicList pbaAccounts = DynamicList.builder()
            .value(DynamicListElement.builder().label(SUCCESSFUL_PBA).build())
            .build();

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("000LR001")
            .ccdCaseReference(12345L)
            .applicantSolicitor1PbaAccounts(pbaAccounts)
            .claimFee(FEE_DATA)
            .build();
        var expectedCreditAccountPaymentRequest = CreditAccountPaymentRequest.builder()
            .accountNumber("PBA0077597")
            .amount(FEE_DATA.toFeeDto().getCalculatedAmount())
            .caseReference("000LR001")
            .ccdCaseNumber("12345")
            .customerReference("Test Customer Reference")
            .description("Claim issue payment")
            .organisationName("Test Organisation Name")
            .service(SERVICE)
            .siteId(SITE_ID)
            .fees(new FeeDto[]{FEE_DATA.toFeeDto()})
            .build();

        PaymentDto paymentResponse = paymentsService.createCreditAccountPayment(caseData, AUTH_TOKEN);

        verify(paymentsClient).createCreditAccountPayment(AUTH_TOKEN, expectedCreditAccountPaymentRequest);
        assertThat(paymentResponse).isEqualTo(PAYMENT_DTO);
    }
}
