package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.hearing.HearingFeeServiceRequestDetails;
import uk.gov.hmcts.reform.payments.client.InvalidPaymentRequestException;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.CasePaymentRequestDto;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.request.CreateServiceRequestDTO;
import uk.gov.hmcts.reform.payments.request.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;
import uk.gov.hmcts.reform.prd.model.Organisation;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.utils.CaseCategoryUtils.isSpecCaseCategory;

import static org.apache.commons.lang.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class PaymentsService {

    private static final String PAYMENT_ACTION = "Case Submit";
    private final PaymentsClient paymentsClient;
    private final PaymentsConfiguration paymentsConfiguration;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;

    @Value("${serviceRequest.api.callback-url}")
    String callBackUrl;

    public void validateRequest(CaseData caseData) {
        String error = null;
        HearingFeeServiceRequestDetails hearingFeeServiceRequestDetails = caseData.getHearingFeeServiceRequestDetails();
        if (hearingFeeServiceRequestDetails == null) {
            error = "Hearing Fee details not received.";
        } else if (hearingFeeServiceRequestDetails.getFee() == null
            || hearingFeeServiceRequestDetails.getFee().getCalculatedAmountInPence() == null
            || isBlank(hearingFeeServiceRequestDetails.getFee().getVersion())
            || isBlank(hearingFeeServiceRequestDetails.getFee().getCode())) {
            error = "Fees are not set correctly.";
        }
        if (!isBlank(error)) {
            throw new InvalidPaymentRequestException(error);
        }
    }

    public PaymentDto createCreditAccountPayment(CaseData caseData, String authToken) throws FeignException {
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
        CreditAccountPaymentRequest creditAccountPaymentRequest = null;

        if (!isSpecCaseCategory(caseData, featureToggleService.isAccessProfilesEnabled()))  {
            creditAccountPaymentRequest = CreditAccountPaymentRequest.builder()
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
        } else if (isSpecCaseCategory(caseData, featureToggleService.isAccessProfilesEnabled())) {
            creditAccountPaymentRequest = CreditAccountPaymentRequest.builder()
                .accountNumber(caseData.getApplicantSolicitor1PbaAccounts().getValue().getLabel())
                .amount(claimFee.getCalculatedAmount())
                .caseReference(caseData.getLegacyCaseReference())
                .ccdCaseNumber(caseData.getCcdCaseReference().toString())
                .customerReference(customerReference)
                .description("Claim issue payment")
                .organisationName(organisationName)
                .service(paymentsConfiguration.getSpecService())
                .siteId(paymentsConfiguration.getSpecSiteId())
                .fees(new FeeDto[]{claimFee})
                .build();
        }
        return creditAccountPaymentRequest;
    }

    public PaymentServiceResponse createServiceRequest(CaseData caseData, String authToken) {
        return paymentsClient.createServiceRequest(authToken, buildServiceRequest(caseData));
    }

    private CreateServiceRequestDTO buildServiceRequest(CaseData caseData) {
        HearingFeeServiceRequestDetails hearingFeeServiceRequestDetails = caseData.getHearingFeeServiceRequestDetails();
        FeeDto feeResponse = hearingFeeServiceRequestDetails.getFee().toFeeDto();
        String siteId = paymentsConfiguration.getSpecSiteId();
        return CreateServiceRequestDTO.builder()
            .ccdCaseNumber(caseData.getCcdCaseReference().toString())
            .caseReference(caseData.getCcdCaseReference().toString())
            .hmctsOrgId(siteId)
            .callBackUrl(callBackUrl)
            .fees(new FeeDto[] { (FeeDto.builder()
                .calculatedAmount(feeResponse.getCalculatedAmount())
                .code(feeResponse.getCode())
                .version(feeResponse.getVersion())
                .volume(1).build())})
            .casePaymentRequest(CasePaymentRequestDto.builder()
                                    .action(PAYMENT_ACTION)
                                    .responsibleParty(caseData.getApplicantPartyName()).build())
            .build();
    }
}
