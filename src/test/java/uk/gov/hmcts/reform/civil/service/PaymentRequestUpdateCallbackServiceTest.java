package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private static final String BUSINESS_PROCESS = "JUDICIAL_REFERRAL";

    @InjectMocks
    private PaymentRequestUpdateCallbackService paymentRequestUpdateCallbackService;

    @Mock
    private PaymentProcessingHelper paymentProcessingHelper;

    @Mock
    private UpdatePaymentStatusService updatePaymentStatusService;

    private CaseData buildCaseData(CaseState state, BusinessProcessStatus businessProcessStatus,
                                   String camundaEvent, PaymentDetails paymentDetails) {
        return CaseDataBuilder.builder().receiveUpdatePaymentRequest().build().toBuilder()
            .ccdState(state)
            .businessProcess(BusinessProcess.builder()
                                 .status(businessProcessStatus)
                                 .camundaEvent(camundaEvent)
                                 .build())
            .hearingFeePaymentDetails(paymentDetails)
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

    @Test
    void shouldStartAndSubmitEventWithCaseDetails_Hearing() {
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY, BUSINESS_PROCESS, null);
        String caseId = CASE_ID.toString();

        when(paymentProcessingHelper.getCaseData(caseId)).thenReturn(caseData);
        when(paymentProcessingHelper.retrievePaymentDetails(FeeType.HEARING.name(), caseData)).thenReturn(null);
        when(paymentProcessingHelper.updateCaseDataWithPaymentDetails(eq(FeeType.HEARING.name()), eq(caseData), any(PaymentDetails.class)))
            .thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.HEARING.name());

        verify(paymentProcessingHelper).getCaseData(caseId);
        verify(paymentProcessingHelper).retrievePaymentDetails(FeeType.HEARING.name(), caseData);
        verify(paymentProcessingHelper).updateCaseDataWithPaymentDetails(eq(FeeType.HEARING.name()), eq(caseData), any(PaymentDetails.class));
        verify(paymentProcessingHelper).createAndSubmitEvent(eq(caseData), eq(caseId), eq(FeeType.HEARING.name()), eq("PaymentRequestUpdate"));
        verify(updatePaymentStatusService).updatePaymentStatus(eq(FeeType.HEARING), eq(caseId), any(CardPaymentStatusResponse.class));
    }

    @Test
    void shouldStartAndSubmitEventWithCaseDetailsForClaimIssued() {
        CaseData caseData = buildCaseData(CaseState.PENDING_CASE_ISSUED, null, null, null)
            .toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .build();
        String caseId = CASE_ID.toString();

        when(paymentProcessingHelper.getCaseData(caseId)).thenReturn(caseData);
        when(paymentProcessingHelper.retrievePaymentDetails(FeeType.CLAIMISSUED.name(), caseData)).thenReturn(null);
        when(paymentProcessingHelper.updateCaseDataWithPaymentDetails(eq(FeeType.CLAIMISSUED.name()), eq(caseData), any(PaymentDetails.class)))
            .thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.CLAIMISSUED.name());

        verify(paymentProcessingHelper).getCaseData(caseId);
        verify(paymentProcessingHelper).retrievePaymentDetails(FeeType.CLAIMISSUED.name(), caseData);
        verify(paymentProcessingHelper).updateCaseDataWithPaymentDetails(eq(FeeType.CLAIMISSUED.name()), eq(caseData), any(PaymentDetails.class));
        verify(paymentProcessingHelper).createAndSubmitEvent(eq(caseData), eq(caseId), eq(FeeType.CLAIMISSUED.name()), eq("PaymentRequestUpdate"));
        verify(updatePaymentStatusService).updatePaymentStatus(eq(FeeType.CLAIMISSUED), eq(caseId), any(CardPaymentStatusResponse.class));
    }

    @Test
    void shouldUpdatePaymentStatusForLiPCaseWithFailedPayment() {
        PaymentDetails failedPaymentDetails = PaymentDetails.builder()
            .status(PaymentStatus.FAILED)
            .reference(REFERENCE)
            .build();
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY, BUSINESS_PROCESS, failedPaymentDetails)
            .toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .build();
        String caseId = CASE_ID.toString();

        when(paymentProcessingHelper.getCaseData(caseId)).thenReturn(caseData);
        when(paymentProcessingHelper.isValidPaymentUpdateHearing(FeeType.HEARING.name(), caseData)).thenReturn(true);
        when(paymentProcessingHelper.retrievePaymentDetails(FeeType.HEARING.name(), caseData)).thenReturn(failedPaymentDetails);
        when(paymentProcessingHelper.updateCaseDataWithPaymentDetails(eq(FeeType.HEARING.name()), eq(caseData), any(PaymentDetails.class)))
            .thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.HEARING.name());

        verify(paymentProcessingHelper).getCaseData(caseId);
        verify(paymentProcessingHelper).isValidPaymentUpdateHearing(FeeType.HEARING.name(), caseData);
        verify(paymentProcessingHelper).retrievePaymentDetails(FeeType.HEARING.name(), caseData);
        verify(paymentProcessingHelper).updateCaseDataWithPaymentDetails(eq(FeeType.HEARING.name()), eq(caseData), any(PaymentDetails.class));
        verify(paymentProcessingHelper).submitCaseDataWithoutEvent(caseData, caseId);
        verify(updatePaymentStatusService).updatePaymentStatus(eq(FeeType.HEARING), eq(caseId), any(CardPaymentStatusResponse.class));
    }

    @Test
    void shouldNotProceedWhenPaymentStatusIsNotPaid() {
        ServiceRequestUpdateDto serviceRequestUpdateDto = buildServiceDto(NOT_PAID);
        String feeType = FeeType.HEARING.name();

        paymentRequestUpdateCallbackService.processCallback(serviceRequestUpdateDto, feeType);

        verify(paymentProcessingHelper, never()).getCaseData(anyString());
        verify(paymentProcessingHelper, never()).retrievePaymentDetails(anyString(), any(CaseData.class));
        verify(updatePaymentStatusService, never()).updatePaymentStatus(any(), anyString(), any(CardPaymentStatusResponse.class));
    }

    @Test
    void shouldNotProceedWhenFeeTypeIsInvalid() {
        ServiceRequestUpdateDto serviceRequestUpdateDto = buildServiceDto(PAID);
        String invalidFeeType = "INVALID_FEE_TYPE";

        paymentRequestUpdateCallbackService.processCallback(serviceRequestUpdateDto, invalidFeeType);

        verify(paymentProcessingHelper, never()).getCaseData(anyString());
        verify(updatePaymentStatusService, never()).updatePaymentStatus(any(), anyString(), any(CardPaymentStatusResponse.class));
    }

    @Test
    void shouldNotUpdatePaymentWhenAlreadySuccessForNonLiPCase() {
        PaymentDetails successfulPaymentDetails = PaymentDetails.builder()
            .status(PaymentStatus.SUCCESS)
            .reference(REFERENCE)
            .build();
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY, BUSINESS_PROCESS, successfulPaymentDetails);
        String caseId = CASE_ID.toString();

        when(paymentProcessingHelper.getCaseData(caseId)).thenReturn(caseData);
        when(paymentProcessingHelper.retrievePaymentDetails(FeeType.HEARING.name(), caseData)).thenReturn(successfulPaymentDetails);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.HEARING.name());

        verify(paymentProcessingHelper).getCaseData(caseId);
        verify(paymentProcessingHelper).retrievePaymentDetails(FeeType.HEARING.name(), caseData);
        verify(paymentProcessingHelper, never()).updateCaseDataWithPaymentDetails(anyString(), any(CaseData.class), any(PaymentDetails.class));
        verify(paymentProcessingHelper, never()).createAndSubmitEvent(any(CaseData.class), anyString(), anyString(), anyString());
        verify(updatePaymentStatusService, never()).updatePaymentStatus(any(), anyString(), any(CardPaymentStatusResponse.class));
    }

    @Test
    void shouldUpdatePaymentStatusForLiPCaseWhenPaymentDetailsAreNull() {
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY, BUSINESS_PROCESS, null)
            .toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .build();
        String caseId = CASE_ID.toString();

        when(paymentProcessingHelper.getCaseData(caseId)).thenReturn(caseData);
        when(paymentProcessingHelper.isValidPaymentUpdateHearing(FeeType.HEARING.name(), caseData)).thenReturn(true);
        when(paymentProcessingHelper.retrievePaymentDetails(FeeType.HEARING.name(), caseData)).thenReturn(null);
        when(paymentProcessingHelper.updateCaseDataWithPaymentDetails(eq(FeeType.HEARING.name()), eq(caseData), any(PaymentDetails.class)))
            .thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.HEARING.name());

        verify(paymentProcessingHelper).getCaseData(caseId);
        verify(paymentProcessingHelper).isValidPaymentUpdateHearing(FeeType.HEARING.name(), caseData);
        verify(paymentProcessingHelper).retrievePaymentDetails(FeeType.HEARING.name(), caseData);
        verify(paymentProcessingHelper).updateCaseDataWithPaymentDetails(eq(FeeType.HEARING.name()), eq(caseData), any(PaymentDetails.class));
        verify(paymentProcessingHelper).submitCaseDataWithoutEvent(caseData, caseId);
        verify(updatePaymentStatusService).updatePaymentStatus(eq(FeeType.HEARING), eq(caseId), any(CardPaymentStatusResponse.class));
    }

    @Test
    void shouldNotProceedWhenLiPCaseAndPaymentAlreadySuccess() {
        PaymentDetails successfulPaymentDetails = PaymentDetails.builder()
            .status(PaymentStatus.SUCCESS)
            .reference(REFERENCE)
            .build();
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY, BUSINESS_PROCESS, successfulPaymentDetails)
            .toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .build();
        String caseId = CASE_ID.toString();

        when(paymentProcessingHelper.getCaseData(caseId)).thenReturn(caseData);
        when(paymentProcessingHelper.isValidPaymentUpdateHearing(FeeType.HEARING.name(), caseData)).thenReturn(false);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.HEARING.name());

        verify(paymentProcessingHelper).getCaseData(caseId);
        verify(paymentProcessingHelper).isValidPaymentUpdateHearing(FeeType.HEARING.name(), caseData);
        verify(paymentProcessingHelper, never()).updateCaseDataWithPaymentDetails(anyString(), any(CaseData.class), any(PaymentDetails.class));
        verify(updatePaymentStatusService, never()).updatePaymentStatus(any(), anyString(), any(CardPaymentStatusResponse.class));
    }

    @Test
    void shouldNotProceedWhenCaseIsNotLiPAndPaymentAlreadySuccess() {
        PaymentDetails successfulPaymentDetails = PaymentDetails.builder()
            .status(PaymentStatus.SUCCESS)
            .reference(REFERENCE)
            .build();
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY, BUSINESS_PROCESS, successfulPaymentDetails)
            .toBuilder()
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .build();
        String caseId = CASE_ID.toString();

        when(paymentProcessingHelper.getCaseData(caseId)).thenReturn(caseData);
        when(paymentProcessingHelper.retrievePaymentDetails(FeeType.HEARING.name(), caseData)).thenReturn(successfulPaymentDetails);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.HEARING.name());

        verify(paymentProcessingHelper).getCaseData(caseId);
        verify(paymentProcessingHelper).retrievePaymentDetails(FeeType.HEARING.name(), caseData);
        verify(paymentProcessingHelper, never()).updateCaseDataWithPaymentDetails(anyString(), any(CaseData.class), any(PaymentDetails.class));
        verify(paymentProcessingHelper, never()).createAndSubmitEvent(any(CaseData.class), anyString(), anyString(), anyString());
        verify(updatePaymentStatusService, never()).updatePaymentStatus(any(), anyString(), any(CardPaymentStatusResponse.class));
    }

    @Test
    void shouldNotProceedWhenFeeTypeIsNotHearingOrClaimIssued() {
        ServiceRequestUpdateDto serviceRequestUpdateDto = buildServiceDto(PAID);
        String invalidFeeType = "OTHER_FEE_TYPE";
        String caseId = CASE_ID.toString();

        paymentRequestUpdateCallbackService.processCallback(serviceRequestUpdateDto, invalidFeeType);

        verify(paymentProcessingHelper, never()).getCaseData(caseId);
        verify(paymentProcessingHelper, never()).retrievePaymentDetails(anyString(), any(CaseData.class));
        verify(updatePaymentStatusService, never()).updatePaymentStatus(any(), anyString(), any(CardPaymentStatusResponse.class));
    }
}
