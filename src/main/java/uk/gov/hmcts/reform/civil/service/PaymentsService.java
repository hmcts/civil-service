package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.utils.HearingFeeUtils;
import uk.gov.hmcts.reform.payments.client.InvalidPaymentRequestException;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.CasePaymentRequestDto;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.request.CreateServiceRequestDTO;
import uk.gov.hmcts.reform.payments.request.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.payments.request.PBAServiceRequestDTO;
import uk.gov.hmcts.reform.payments.response.PBAServiceRequestResponse;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;

import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.StringUtils.isBlank;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentsService {

    private static final String PAYMENT_ACTION = "Case Submit";
    protected static final String CLAIM_ISSUE_PAYMENT = "Claim issue payment";
    private final PaymentsClient paymentsClient;
    private final PaymentsConfiguration paymentsConfiguration;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;
    private final HearingFeesService hearingFeesService;

    @Value("${serviceRequest.api.callback-url}")
    String callBackUrl;
    @Value("${serviceRequestClaimIssued.api.callback-url}")
    String callBackUrlClaimIssued;

    public PaymentDto createCreditAccountPayment(CaseData caseData, String authToken) throws FeignException {
        return paymentsClient.createCreditAccountPayment(authToken, buildRequest(caseData));
    }

    public CreditAccountPaymentRequest buildRequest(CaseData caseData) {
        FeeDto claimFee = caseData.getClaimFee().toFeeDto();
        var organisationId = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        var organisationName = organisationService.findOrganisationById(organisationId)
            .map(Organisation::getName)
            .orElseThrow(RuntimeException::new);

        String customerReference = ofNullable(caseData.getClaimIssuedPaymentDetails())
            .map(PaymentDetails::getCustomerReference)
            .orElse(caseData.getPaymentReference());

        return getCreditAccountPaymentRequest(caseData, claimFee, customerReference, organisationName);
    }

    private CreditAccountPaymentRequest getCreditAccountPaymentRequest(CaseData caseData, FeeDto claimFee, String customerReference,
                                                                       String organisationName) {
        CreditAccountPaymentRequest creditAccountPaymentRequest;
        creditAccountPaymentRequest = CreditAccountPaymentRequest.builder()
            .accountNumber(caseData.getApplicantSolicitor1PbaAccounts().getValue().getLabel())
            .amount(claimFee.getCalculatedAmount())
            .caseReference(caseData.getLegacyCaseReference())
            .ccdCaseNumber(caseData.getCcdCaseReference().toString())
            .customerReference(customerReference)
            .description(CLAIM_ISSUE_PAYMENT)
            .organisationName(organisationName)
            .service(
                SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) ? paymentsConfiguration.getSpecService() : paymentsConfiguration.getService())
            .siteId(SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) ? paymentsConfiguration.getSpecSiteId() : paymentsConfiguration.getSiteId())
            .fees(new FeeDto[] {claimFee})
            .build();
        return creditAccountPaymentRequest;
    }

    public void validateRequest(CaseData caseData) {
        String error = null;
        SRPbaDetails serviceRequestPBADetails = null;

        if (caseData.getHearingDate() == null) {
            serviceRequestPBADetails = caseData.getClaimIssuedPBADetails();
        } else {
            serviceRequestPBADetails = caseData.getHearingFeePBADetails();
        }

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

    private PBAServiceRequestDTO buildPbaPaymentRequestBulkClaim(CaseData caseData) {
        SRPbaDetails serviceRequestPBADetails = null;
        FeeDto srFee = null;
        serviceRequestPBADetails = caseData.getClaimIssuedPBADetails();
        srFee = caseData.getClaimFee().toFeeDto();

        var organisationId = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        var organisationName = organisationService.findOrganisationById(organisationId)
            .map(Organisation::getName)
            .orElseThrow(RuntimeException::new);
        PBAServiceRequestDTO pbaServiceRequestDTO = null;
        if (serviceRequestPBADetails != null) {
            pbaServiceRequestDTO = PBAServiceRequestDTO.builder()
                .accountNumber(serviceRequestPBADetails.getApplicantsPbaAccounts()
                    .getValue().getLabel())
                .amount(srFee.getCalculatedAmount())
                .customerReference("bulk claim issuer")
                .organisationName(organisationName)
                .idempotencyKey(String.valueOf(UUID.randomUUID()))
                .build();
            log.info(pbaServiceRequestDTO.getCustomerReference());
            return pbaServiceRequestDTO;

        } else {
            throw new RuntimeException("Invalid Case State" + caseData.getCcdCaseReference());
        }

    }

    public PBAServiceRequestResponse createPbaPayment(CaseData caseData, String authToken) {
        String serviceReqReference = null;
        if (caseData.getHearingDate() == null) {
            serviceReqReference = caseData.getClaimIssuedPBADetails().getServiceReqReference();
        } else {
            serviceReqReference = caseData.getHearingFeePBADetails().getServiceReqReference();
        }
        return paymentsClient.createPbaPayment(
            serviceReqReference,
            authToken,
            buildPbaPaymentRequestBulkClaim(caseData)
        );
    }

    public PaymentServiceResponse createServiceRequest(CaseData caseData, String authToken) {
        CreateServiceRequestDTO paymentRequest = buildServiceRequest(caseData);
        log.info("Calling payment service request for case {} and callbackUrl {} ", caseData.getCcdCaseReference(), paymentRequest.getCallBackUrl());
        return paymentsClient.createServiceRequest(authToken, paymentRequest);
    }

    private CreateServiceRequestDTO buildServiceRequest(CaseData caseData) {
        String siteId = null;

        if (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            siteId = paymentsConfiguration.getSiteId();
        } else if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            siteId = paymentsConfiguration.getSpecSiteId();
        }
        String callbackURLUsed = null;
        FeeDto feeResponse = null;

        if (caseData.getHearingDueDate() == null) {
            callbackURLUsed = callBackUrlClaimIssued;
            feeResponse = caseData.getClaimFee().toFeeDto();
        } else {
            callbackURLUsed = callBackUrl;
            if (caseData.getHearingFee() != null) {
                feeResponse = caseData.getHearingFee().toFeeDto();
            } else {
                feeResponse = HearingFeeUtils.calculateAndApplyFee(
                    hearingFeesService, caseData, caseData.getAssignedTrack()).toFeeDto();
            }
        }
        log.info("Payment callbackURLUsed: {}, siteId{} and  for caseId {} ", callbackURLUsed, siteId, caseData.getCcdCaseReference());

        if (callbackURLUsed != null) {
            return CreateServiceRequestDTO.builder()
                .caseReference(caseData.getLegacyCaseReference())
                .ccdCaseNumber(caseData.getCcdCaseReference().toString())
                .hmctsOrgId(siteId)
                .callBackUrl(callbackURLUsed)
                .fees(new FeeDto[] {(FeeDto.builder()
                    .calculatedAmount(feeResponse.getCalculatedAmount())
                    .code(feeResponse.getCode())
                    .version(feeResponse.getVersion())
                    .volume(1).build())})
                .casePaymentRequest(CasePaymentRequestDto.builder()
                    .action(PAYMENT_ACTION)
                    .responsibleParty(caseData.getApplicant1().getPartyName()).build())
                .build();

        } else {
            throw new RuntimeException("Invalid Case State" + caseData.getCcdCaseReference());
        }
    }
}

