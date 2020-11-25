package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.client.request.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.unspec.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ClaimValue;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentsService {

    private final FeesService feesService;
    private final PaymentsClient paymentsClient;
    private final PaymentsConfiguration paymentsConfiguration;

    public PaymentDto createCreditAccountPayment(CaseData caseData, String authToken) {
        //temporarily hardcoded
        ClaimValue claimValue = ClaimValue.builder().statementOfValueInPennies(BigDecimal.valueOf(10000)).build();
        FeeDto feeDto = feesService.getFeeDataByClaimValue(claimValue);

        return paymentsClient.createCreditAccountPayment(authToken, buildRequest(caseData, feeDto));
    }

    private CreditAccountPaymentRequest buildRequest(CaseData caseData, FeeDto feeDto) {
        return CreditAccountPaymentRequest.builder()
            .accountNumber(caseData.getPbaNumber().name())
            .amount(feeDto.getCalculatedAmount())
            .caseReference(caseData.getLegacyCaseReference())
            .ccdCaseNumber(caseData.getCcdCaseReference().toString())
            .customerReference("Test Customer Reference")
            .description("Claim issue payment")
            .organisationName("Test Organisation Name")
            .service(paymentsConfiguration.getService())
            .siteId(paymentsConfiguration.getSiteId())
            .fees(new FeeDto[]{feeDto})
            .build();
    }
}
