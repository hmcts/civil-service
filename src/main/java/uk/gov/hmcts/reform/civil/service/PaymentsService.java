package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.request.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.HashMap;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class PaymentsService {

    private final PaymentsClient paymentsClient;
    private final PaymentsConfiguration paymentsConfiguration;
    private final OrganisationService organisationService;

    public PaymentDto createCreditAccountPayment(CaseData caseData, String authToken) throws FeignException {
        Request request = Request.create(Request.HttpMethod.GET, "url", new HashMap<>(), null, new RequestTemplate());
        throw new FeignException.UnprocessableEntity("", request, null);

        //return paymentsClient.createCreditAccountPayment(authToken, buildRequest(caseData));
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
