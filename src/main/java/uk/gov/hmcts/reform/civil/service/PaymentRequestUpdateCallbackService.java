package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestUpdateCallbackService {

    public static final String PAID = "Paid";
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto) {
        log.info("Processing the callback for the caseId {} with status {}", serviceRequestUpdateDto.getCcdCaseNumber(),
                 serviceRequestUpdateDto.getServiceRequestStatus());

        if (serviceRequestUpdateDto.getServiceRequestStatus().equalsIgnoreCase(PAID)) {

            log.info("Fetching the Case details based on caseId {}", serviceRequestUpdateDto.getCcdCaseNumber());
            CaseDetails caseDetails = coreCaseDataService.getCase(Long.valueOf(serviceRequestUpdateDto
                                                                                   .getCcdCaseNumber()));
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
            updateCaseDataWithStateAndPaymentDetails(serviceRequestUpdateDto, caseData);
        } else {

            log.error("Case id {} not present", serviceRequestUpdateDto.getCcdCaseNumber());
        }
    }

    private CaseData updateCaseDataWithStateAndPaymentDetails(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                              CaseData caseData) {

        PaymentDetails pbaDetails = caseData.getHearingFeePaymentDetails();
        String customerReference = ofNullable(serviceRequestUpdateDto.getPayment())
            .map(PaymentDto::getCustomerReference)
            .orElse(pbaDetails.getCustomerReference());

        PaymentDetails paymentDetails = ofNullable(pbaDetails)
            .map(PaymentDetails::toBuilder)
            .orElse(PaymentDetails.builder())
            .status(SUCCESS)
            .customerReference(customerReference)
            .reference(serviceRequestUpdateDto.getPayment().getPaymentReference())
            .errorCode(null)
            .errorMessage(null)
            .build();

        caseData = caseData.toBuilder()
            .hearingFeePaymentDetails(paymentDetails)
            .build();

        return caseData;
    }

}
