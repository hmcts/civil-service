package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.ServiceRequestConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingFeeServiceRequestDetails;
import uk.gov.hmcts.reform.payments.client.InvalidPaymentRequestException;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.CasePaymentRequestDto;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.request.CreateServiceRequestDTO;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;

import static org.apache.commons.lang.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final PaymentsClient paymentsClient;
    private final ServiceRequestConfiguration serviceRequestConfiguration;
    public static final String PAYMENT_ACTION = "Case Submit";

    @Value("${serviceRequest.api.callback-url}")
    String callBackUrl;

    public PaymentServiceResponse createPaymentServiceRequest(CaseData caseData, String authToken)
        throws FeignException {
        return paymentsClient.createServiceRequest(authToken, buildRequest(caseData));
    }

    private CreateServiceRequestDTO buildRequest(CaseData caseData) {

        //Fee wouldbe set to this field in case data as per Miguel
        FeeDto hearingFee = caseData.getHearingFee().toFeeDto();

        return CreateServiceRequestDTO.builder()
            .callBackUrl(callBackUrl)
            .casePaymentRequest(CasePaymentRequestDto.builder()
                                    .action(PAYMENT_ACTION)
                                    .responsibleParty(caseData.getApplicant1().getPartyName()).build())
            .caseReference(caseData.getCcdCaseReference().toString())
            .ccdCaseNumber(caseData.getCcdCaseReference().toString())
            .fees(new FeeDto[] { (FeeDto.builder()
                .calculatedAmount(hearingFee.getCalculatedAmount())
                .code(hearingFee.getCode())
                .version(hearingFee.getVersion())
                .volume(1).build())})
            .hmctsOrgId(serviceRequestConfiguration.getSiteId()).build();
    }

    public void validateRequest(CaseData caseData) {
        String error = null;
        HearingFeeServiceRequestDetails serviceRequestDetails = caseData.getHearingFeeServiceRequestDetails();
        if (serviceRequestDetails == null) {
            error = "Service Request details not received.";
        } else if (serviceRequestDetails.getFee() == null
            || serviceRequestDetails.getFee().getCalculatedAmountInPence() == null
            || isBlank(serviceRequestDetails.getFee().getVersion())
            || isBlank(serviceRequestDetails.getFee().getCode())) {
            error = "Fees are not set correctly.";
        }
        if (!isBlank(error)) {
            throw new InvalidPaymentRequestException(error);
        }

    }

}
