package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.client.request.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.prd.model.Organisation;
import uk.gov.hmcts.reform.unspec.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.PaymentDetails;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class PaymentsService {

    private final PaymentsClient paymentsClient;
    private final PaymentsConfiguration paymentsConfiguration;
    private final OrganisationService organisationService;

    public PaymentDto createCreditAccountPayment(CaseData caseData, String authToken) {
        return paymentsClient.createCreditAccountPayment(authToken, buildRequest(caseData));
    }

    private CreditAccountPaymentRequest buildRequest(CaseData caseData) {
        FeeDto claimFee = caseData.getClaimFee().toFeeDto();
        var organisationId = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        var organisationName = organisationService.findOrganisationById(organisationId)
            .map(Organisation::getName)
            .orElseThrow(RuntimeException::new);

        String customerReference = ofNullable(caseData.getClaimIssuedPaymentDetails())
            .map(PaymentDetails::getCustomerReference)
            .orElse(caseData.getPaymentReference());

        return CreditAccountPaymentRequest.builder()
            .accountNumber(caseData.getApplicantSolicitor1PbaAccounts().getValue().getLabel())
            .amount(claimFee.getCalculatedAmount())
            .caseReference(caseData.getLegacyCaseReference())
            .ccdCaseNumber(caseData.getCcdCaseReference().toString())
            .customerReference(customerReference)
            .description("Claim issue payment")
            .organisationName(organisationName)
            .service(paymentsConfiguration.getService())
            .siteId(paymentsConfiguration.getSiteId())
            .fees(new FeeDto[]{claimFee})
            .build();
    }
}
