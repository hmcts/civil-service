package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
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
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_CLAIM_ISSUE_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_HEARING_FEE_PAYMENT;
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
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private CaseData buildCaseData(CaseState state, BusinessProcessStatus businessProcessStatus, String camundaEvent, PaymentDetails paymentDetails) {
        CaseData caseData = CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        caseData.setCcdState(state);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setStatus(businessProcessStatus);
        businessProcess.setCamundaEvent(camundaEvent);
        caseData.setBusinessProcess(businessProcess);
        caseData.setHearingFeePaymentDetails(paymentDetails);
        return caseData;
    }

    private CaseDetails buildCaseDetails(CaseData caseData) {
        return CaseDetails.builder()
                .data(objectMapper.convertValue(caseData, new TypeReference<>() {
                }))
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
                        .customerReference("CUST-REF")
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
        CaseData caseData = buildCaseData(CaseState.PENDING_CASE_ISSUED, null, null, null);
        PaymentDetails claimIssuedPaymentDetails = new PaymentDetails();
        claimIssuedPaymentDetails.setStatus(PaymentStatus.FAILED);
        caseData.setClaimIssuedPaymentDetails(claimIssuedPaymentDetails);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
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
        CaseData caseData = buildCaseData(CaseState.PENDING_CASE_ISSUED, null, null, null);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
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
    void shouldProceed_WhenAdditionalPaymentExist_WithPaymentFailForClaimIssued() {
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setStatus(PaymentStatus.FAILED);
        paymentDetails.setReference(REFERENCE);
        CaseData caseData = buildCaseData(CaseState.PENDING_CASE_ISSUED, null, null, paymentDetails);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
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
    void shouldNotCallUpdatePaymentStatus_WhenLRvLR() {
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY, BUSINESS_PROCESS, null);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1Represented(YesOrNo.YES);
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

    @Test
    void shouldPersistCustomerReferenceFromCallback() {
        CaseData caseData = buildCaseData(CaseState.CASE_PROGRESSION, BusinessProcessStatus.READY, BUSINESS_PROCESS, null);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1Represented(YesOrNo.YES);
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
                caseDetails,
                CaseEvent.SERVICE_REQUEST_RECEIVED
        ));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID), FeeType.HEARING.name());

        ArgumentCaptor<CaseDataContent> captor = ArgumentCaptor.forClass(CaseDataContent.class);
        verify(coreCaseDataService).submitUpdate(any(), captor.capture());

        CaseData captured = objectMapper.convertValue(captor.getValue().getData(), CaseData.class);
        assertThat(captured.getHearingFeePaymentDetails()).isNotNull();
        assertThat(captured.getHearingFeePaymentDetails().getCustomerReference()).isEqualTo("CUST-REF");
    }

    @Test
    void shouldSubmitCitizenHearingFeePaymentEventIfFeeTypeIsHearing() {
        CaseData caseData = CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setStatus(BusinessProcessStatus.READY);
        businessProcess.setCamundaEvent(BUSINESS_PROCESS);
        caseData.setBusinessProcess(businessProcess);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setHearingFeePaymentDetails(null);
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
                caseDetails,
                CITIZEN_HEARING_FEE_PAYMENT
        ));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.updatePaymentStatus(FeeType.HEARING, String.valueOf(CASE_ID), getCardPaymentStatusResponse());

        verify(coreCaseDataService, times(1)).getCase(CASE_ID);
        verify(coreCaseDataService).startUpdate(String.valueOf(CASE_ID), CITIZEN_HEARING_FEE_PAYMENT);
        verify(coreCaseDataService).submitUpdate(any(), any());
    }

    @Test
    void shouldSubmitCitizenClaimIssuedFeePaymentEventIfFeeTypeIsClaimIssued() {
        CaseData caseData = CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setStatus(BusinessProcessStatus.READY);
        businessProcess.setCamundaEvent(BUSINESS_PROCESS);
        caseData.setBusinessProcess(businessProcess);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setHearingFeePaymentDetails(null);
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
                caseDetails,
                CITIZEN_CLAIM_ISSUE_PAYMENT
        ));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.updatePaymentStatus(FeeType.CLAIMISSUED, String.valueOf(CASE_ID), getCardPaymentStatusResponse());

        verify(coreCaseDataService, times(1)).getCase(CASE_ID);
        verify(coreCaseDataService).startUpdate(String.valueOf(CASE_ID), CITIZEN_CLAIM_ISSUE_PAYMENT);
        verify(coreCaseDataService).submitUpdate(any(), any());
    }

    @Test
    void shouldLogErrorAndReturnWhenFeeTypeIsInvalid() {
        ServiceRequestUpdateDto dto = buildServiceDto(PAID);
        String invalidFeeType = "INVALID_FEE_TYPE";

        paymentRequestUpdateCallbackService.processCallback(dto, invalidFeeType);

        verify(coreCaseDataService, never()).getCase(any());
        verify(coreCaseDataService, never()).startUpdate(any(), any());
        verify(coreCaseDataService, never()).submitUpdate(any(), any());
    }

    @Test
    void shouldRetainExistingCustomerReferenceWhenUpdatingWithCardPaymentStatusResponse() {
        PaymentDetails existingDetails = new PaymentDetails();
        existingDetails.setCustomerReference("EXISTING-REF");
        existingDetails.setStatus(PaymentStatus.FAILED);

        CaseData caseData = CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setStatus(BusinessProcessStatus.READY);
        businessProcess.setCamundaEvent(BUSINESS_PROCESS);
        caseData.setBusinessProcess(businessProcess);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setHearingFeePaymentDetails(existingDetails);
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
                caseDetails,
                CITIZEN_HEARING_FEE_PAYMENT
        ));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        CardPaymentStatusResponse response = new CardPaymentStatusResponse()
                .setPaymentReference(REFERENCE)
                .setStatus("Success");

        paymentRequestUpdateCallbackService.updatePaymentStatus(FeeType.HEARING, CASE_ID.toString(), response);

        ArgumentCaptor<CaseDataContent> captor = ArgumentCaptor.forClass(CaseDataContent.class);
        verify(coreCaseDataService).submitUpdate(any(), captor.capture());

        CaseData captured = objectMapper.convertValue(captor.getValue().getData(), CaseData.class);
        assertThat(captured.getHearingFeePaymentDetails()).isNotNull();
        assertThat(captured.getHearingFeePaymentDetails().getCustomerReference()).isEqualTo("EXISTING-REF");
    }

    private CardPaymentStatusResponse getCardPaymentStatusResponse() {
        return new CardPaymentStatusResponse()
                .setPaymentReference("1234")
                .setStatus(PaymentStatus.SUCCESS.name());
    }
}
