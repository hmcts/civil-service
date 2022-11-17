package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
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
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;

@Service
@RequiredArgsConstructor
public class PaymentsService {

    private static final String PAYMENT_ACTION = "Case Submit";
    private final PaymentsClient paymentsClient;
    private final PaymentsConfiguration paymentsConfiguration;
    private final OrganisationService organisationService;

    @Value("${serviceRequest.api.callback-url}")
    String callBackUrl;

    public void validateRequest(CaseData caseData) {
        String error = null;
        SRPbaDetails serviceRequestPBADetails = caseData.getServiceRequestPBADetails();
        if (serviceRequestPBADetails == null) {
            error = "Fee details not received.";
        } else if (serviceRequestPBADetails.getFee() == null
            || serviceRequestPBADetails.getFee().getCalculatedAmountInPence() == null
            || isBlank(serviceRequestPBADetails.getFee().getVersion())
            || isBlank(serviceRequestPBADetails.getFee().getCode())) {
            error = "Fees are not set correctly.";
        }
        if (!isBlank(error)) {
            throw new InvalidPaymentRequestException(error);
        }
    }

    private PBAServiceRequestDTO buildRequest(CaseData caseData) {
        SRPbaDetails serviceRequestPBADetails = caseData.getServiceRequestPBADetails();
        FeeDto claimFee = caseData.getClaimFee().toFeeDto();
        var organisationId = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        var organisationName = organisationService.findOrganisationById(organisationId)
            .map(Organisation::getName)
            .orElseThrow(RuntimeException::new);
        PBAServiceRequestDTO pbaServiceRequestDTO = null;
        pbaServiceRequestDTO = PBAServiceRequestDTO.builder()
                .accountNumber(serviceRequestPBADetails.getApplicantsPbaAccounts()
                                   .getValue().getLabel())
                .amount(claimFee.getCalculatedAmount())
                .customerReference(serviceRequestPBADetails.getPbaReference())
                .organisationName(organisationName)
                .idempotencyKey(String.valueOf(UUID.randomUUID()))
                .build();
        return pbaServiceRequestDTO;
    }

    public PBAServiceRequestResponse createCreditAccountPayment(CaseData caseData, String authToken) {
        String serviceReqReference = caseData.getServiceRequestPBADetails().getServiceReqReference();
        return paymentsClient.createPbaPayment(serviceReqReference, authToken, buildRequest(caseData));
    }

    public PaymentServiceResponse createServiceRequest(CaseData caseData, String authToken) {
        return paymentsClient.createServiceRequest(authToken, buildServiceRequest(caseData));
    }

    private CreateServiceRequestDTO buildServiceRequest(CaseData caseData) {
        SRPbaDetails serviceRequestPBADetails = caseData.getServiceRequestPBADetails();
        FeeDto feeResponse = serviceRequestPBADetails.getFee().toFeeDto();
        String siteId = caseData.getSuperClaimType().equals(SPEC_CLAIM)
            ? paymentsConfiguration.getSpecSiteId() : paymentsConfiguration.getSiteId();
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
