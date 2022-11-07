package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearing.HFPbaDetails;
import uk.gov.hmcts.reform.payments.client.InvalidPaymentRequestException;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.CasePaymentRequestDto;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.request.CreateServiceRequestDTO;
import uk.gov.hmcts.reform.payments.request.PBAServiceRequestDTO;
import uk.gov.hmcts.reform.payments.response.PBAServiceRequestResponse;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.UUID;

import static org.apache.commons.lang.StringUtils.isBlank;
import static uk.gov.hmcts.reform.civil.utils.CaseCategoryUtils.isSpecCaseCategory;

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
        HFPbaDetails hearingFeePBADetails = caseData.getHearingFeePBADetails();
        if (hearingFeePBADetails == null) {
            error = "Hearing Fee details not received.";
        } else if (hearingFeePBADetails.getFee() == null
            || hearingFeePBADetails.getFee().getCalculatedAmountInPence() == null
            || isBlank(hearingFeePBADetails.getFee().getVersion())
            || isBlank(hearingFeePBADetails.getFee().getCode())) {
            error = "Fees are not set correctly.";
        }
        if (!isBlank(error)) {
            throw new InvalidPaymentRequestException(error);
        }
    }

    private PBAServiceRequestDTO buildRequest(CaseData caseData) {
        HFPbaDetails hearingFeePBADetails = caseData.getHearingFeePBADetails();
        FeeDto claimFee = caseData.getClaimFee().toFeeDto();
        var organisationId = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        var organisationName = organisationService.findOrganisationById(organisationId)
            .map(Organisation::getName)
            .orElseThrow(RuntimeException::new);
        PBAServiceRequestDTO pbaServiceRequestDTO = null;
        if (!isSpecCaseCategory(caseData, featureToggleService.isAccessProfilesEnabled()))  {
            pbaServiceRequestDTO = PBAServiceRequestDTO.builder()
                .accountNumber(hearingFeePBADetails.getApplicantsPbaAccounts()
                                   .getValue().getLabel())
                .amount(claimFee.getCalculatedAmount())
                .customerReference(hearingFeePBADetails.getPbaReference())
                .organisationName(organisationName)
                .idempotencyKey(String.valueOf(UUID.randomUUID()))
                .build();
        } else if (isSpecCaseCategory(caseData, featureToggleService.isAccessProfilesEnabled())) {
            pbaServiceRequestDTO = PBAServiceRequestDTO.builder()
                .accountNumber(hearingFeePBADetails.getApplicantsPbaAccounts()
                                   .getValue().getLabel())
                .amount(claimFee.getCalculatedAmount())
                .customerReference(hearingFeePBADetails.getPbaReference())
                .organisationName(organisationName)
                .idempotencyKey(String.valueOf(UUID.randomUUID()))
                .build();
        }
        return pbaServiceRequestDTO;
    }

    public PBAServiceRequestResponse createCreditAccountPayment(CaseData caseData, String authToken) {
        String serviceReqReference = caseData.getHearingFeePBADetails().getServiceReqReference();
        return paymentsClient.createPbaPayment(serviceReqReference, authToken, buildRequest(caseData));
    }

    public PaymentServiceResponse createServiceRequest(CaseData caseData, String authToken) {
        return paymentsClient.createServiceRequest(authToken, buildServiceRequest(caseData));
    }

    private CreateServiceRequestDTO buildServiceRequest(CaseData caseData) {
        HFPbaDetails hearingFeePBADetails = caseData.getHearingFeePBADetails();
        FeeDto feeResponse = hearingFeePBADetails.getFee().toFeeDto();
        String siteId = paymentsConfiguration.getSpecSiteId();
        return CreateServiceRequestDTO.builder()
            .caseReference(caseData.getLegacyCaseReference())
            .ccdCaseNumber(caseData.getCcdCaseReference().toString())
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
