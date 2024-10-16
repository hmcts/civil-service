package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@ExtendWith(MockitoExtension.class)
class PaymentRequestUpdateCallbackServiceTest {

    private static final String PAID = "Paid";
    private static final String NOT_PAID = "NotPaid";
    private static final Long CASE_ID = 1594901956117591L;
    private static final String REFERENCE = "123445";
    private static final String ACCOUNT_NUMBER = "123445555";
    private static final String TOKEN = "1234";
    private static final String BUSINESS_PROCESS = "JUDICIAL_REFERRAL";

    @InjectMocks
    private PaymentRequestUpdateCallbackService paymentRequestUpdateCallbackService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private UpdatePaymentStatusService updatePaymentStatusService;

    private CaseData buildCaseData(CaseState state, BusinessProcessStatus businessProcessStatus, String camundaEvent, PaymentDetails paymentDetails) {
        return CaseDataBuilder.builder().receiveUpdatePaymentRequest().build().toBuilder()
            .ccdState(state)
            .businessProcess(BusinessProcess.builder()
                                 .status(businessProcessStatus)
                                 .camundaEvent(camundaEvent)
                                 .build())
            .hearingFeePaymentDetails(paymentDetails)
            .build();
    }

    private CaseDetails buildCaseDetails(CaseData caseData) {
        return CaseDetails.builder()
            .data(objectMapper.convertValue(caseData, new TypeReference<Map<String, Object>>() {}))
            .id(CASE_ID)
            .build();
    }

    private ServiceRequestUpdateDto buildServiceDto(String status) {
        return ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(CASE_ID.toString())
            .serviceRequestStatus(status)
            .payment(PaymentDto.builder()
                         .amount(new BigDecimal(167))
                         .paymentReference(REFERENCE)
                         .caseReference(REFERENCE)
                         .accountNumber(ACCOUNT_NUMBER)
                         .build())
            .build();
    }

    private StartEventResponse startEventResponse(CaseDetails caseDetails, CaseEvent event) {
        return StartEventResponse.builder()
            .token(TOKEN)
            .eventId(event.name())
            .caseDetails(caseDetails)
            .build();
    }

    private void setupMocks(CaseDetails caseDetails, CaseData caseData, CaseEvent caseEvent) {
        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(caseDetails, caseEvent));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);
    }

    @Test
    void shouldStartAndSubmitEventWithCaseDetails_Hearing() {
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY, BUSINESS_PROCESS, null);
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
            caseDetails,
            CaseEvent.SERVICE_REQUEST_RECEIVED
        ));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.HEARING.name());

        verify(coreCaseDataService).getCase(CASE_ID);
        verify(coreCaseDataService).startUpdate(any(), any());
        verify(coreCaseDataService).submitUpdate(any(), any());
    }

    @Test
    void shouldStartAndSubmitEventWithCaseDetailsForHearingAndPreviousFail() {
        CaseData caseData = buildCaseData(CaseState.PENDING_CASE_ISSUED, null, null, null).toBuilder()
            .claimIssuedPaymentDetails(PaymentDetails.builder().status(PaymentStatus.FAILED).build()).caseAccessCategory(SPEC_CLAIM).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
            caseDetails,
            CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT
        ));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.CLAIMISSUED.name());

        verify(coreCaseDataService).getCase(CASE_ID);
        verify(coreCaseDataService).startUpdate(any(), any());
        verify(coreCaseDataService).submitUpdate(any(), any());
    }

    @Test
    void shouldStartAndSubmitEventWithCaseDetailsForClaimIssued() {
        CaseData caseData = buildCaseData(CaseState.PENDING_CASE_ISSUED, null, null, null).toBuilder().caseAccessCategory(SPEC_CLAIM).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
            caseDetails,
            CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT
        ));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.CLAIMISSUED.name());

        verify(coreCaseDataService).getCase(CASE_ID);
        verify(coreCaseDataService).startUpdate(any(), any());
        verify(coreCaseDataService).submitUpdate(any(), any());
    }

    @Test
    void shouldProceed_WhenAdditionalPaymentExist_WithPaymentFail() {
        PaymentDetails paymentDetails = PaymentDetails.builder().status(uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED).reference(REFERENCE).build();
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY, BUSINESS_PROCESS, paymentDetails);
        caseData = caseData.toBuilder().applicant1Represented(YesOrNo.NO).respondent1Represented(YesOrNo.NO)
            .claimIssuedPaymentDetails(paymentDetails).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.CLAIMISSUED.name());

        verify(coreCaseDataService).getCase(CASE_ID);
        verify(updatePaymentStatusService).updatePaymentStatus(any(), any(), any());
    }

    @Test
    void shouldProceed_WhenAdditionalPaymentExist_WithPayment() {
        PaymentDetails paymentDetails = PaymentDetails.builder().status(uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS).reference(REFERENCE).build();
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY, BUSINESS_PROCESS, paymentDetails);
        caseData = caseData.toBuilder().applicant1Represented(YesOrNo.NO).respondent1Represented(YesOrNo.NO).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.CLAIMISSUED.name());

        verify(coreCaseDataService).getCase(CASE_ID);
        verify(coreCaseDataService, never()).startUpdate(any(), any());
        verify(coreCaseDataService, never()).submitUpdate(any(), any());
        verify(updatePaymentStatusService, never()).updatePaymentStatus(any(), any(), any());
    }

    @Test
    void shouldProceed_WhenClaimIssue_PaymentNull() {
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY, BUSINESS_PROCESS, null)
            .toBuilder().claimIssuedPaymentDetails(null).build();
        caseData = caseData.toBuilder().applicant1Represented(YesOrNo.NO).respondent1Represented(YesOrNo.NO).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.CLAIMISSUED.name());

        verify(coreCaseDataService).getCase(CASE_ID);
        verify(updatePaymentStatusService).updatePaymentStatus(any(), any(), any());
    }

    @Test
    void shouldProceed_WhenAdditionalPaymentExist_WithPaymentFailForClaimIssued() {
        PaymentDetails paymentDetails = PaymentDetails.builder().status(uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED).reference(REFERENCE).build();
        CaseData caseData = buildCaseData(CaseState.PENDING_CASE_ISSUED, null, null, paymentDetails).toBuilder().caseAccessCategory(SPEC_CLAIM).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
            caseDetails,
            CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT
        ));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.CLAIMISSUED.name());

        verify(coreCaseDataService).getCase(CASE_ID);
        verify(coreCaseDataService).startUpdate(any(), any());
        verify(coreCaseDataService).submitUpdate(any(), any());
    }

    @Test
    void shouldCallUpdatePaymentServiceWhenLipVLipAndHearingFeeDetailsAreNull() {
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY,
                                          BUSINESS_PROCESS, null).toBuilder().applicant1Represented(YesOrNo.NO).respondent1Represented(YesOrNo.NO).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.HEARING.name());

        verify(coreCaseDataService).getCase(CASE_ID);
        verify(coreCaseDataService, never()).startUpdate(any(), any());
        verify(coreCaseDataService, never()).submitUpdate(any(), any());
        verify(updatePaymentStatusService).updatePaymentStatus(any(), any(), any());
    }

    @Test
    void shouldNotCallUpdatePaymentServiceWhenLipVLipAndHearingFeeDetailsAreNotNull() {
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY,
                                          BUSINESS_PROCESS,
                                          PaymentDetails.builder().status(PaymentStatus.SUCCESS).build())
            .toBuilder().applicant1Represented(YesOrNo.NO).respondent1Represented(YesOrNo.NO).build();

        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.HEARING.name());

        verify(coreCaseDataService).getCase(CASE_ID);
        verify(coreCaseDataService, never()).startUpdate(any(), any());
        verify(coreCaseDataService, never()).submitUpdate(any(), any());
        verify(updatePaymentStatusService, never()).updatePaymentStatus(any(), any(), any());
    }

    @Test
    void shouldNotCallUpdatePaymentServiceWhenLRvLR() {
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY,
                                          BUSINESS_PROCESS, null).toBuilder().applicant1Represented(YesOrNo.YES).respondent1Represented(YesOrNo.YES).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
            caseDetails,
            CaseEvent.SERVICE_REQUEST_RECEIVED
        ));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.HEARING.name());

        verify(coreCaseDataService).getCase(CASE_ID);
        verify(coreCaseDataService).startUpdate(any(), any());
        verify(coreCaseDataService).submitUpdate(any(), any());
        verify(updatePaymentStatusService, never()).updatePaymentStatus(any(), any(), any());
    }

    @Test
    void shouldNotProceed_WhenPaymentFailed() {
        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(NOT_PAID), FeeType.HEARING.name());

        verify(coreCaseDataService, never()).getCase(CASE_ID);
        verify(coreCaseDataService, never()).startUpdate(any(), any());
        verify(coreCaseDataService, never()).submitUpdate(any(), any());
    }

    @Test
    void shouldNotProceed_WhenPaymentFailedForClaimIssued() {
        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(NOT_PAID), FeeType.CLAIMISSUED.name());

        verify(coreCaseDataService, never()).getCase(CASE_ID);
        verify(coreCaseDataService, never()).startUpdate(any(), any());
        verify(coreCaseDataService, never()).submitUpdate(any(), any());
    }
}
