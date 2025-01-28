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
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_CLAIM_ISSUE_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_HEARING_FEE_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;

@ExtendWith(MockitoExtension.class)
class UpdatePaymentStatusServiceTest {

    @InjectMocks
    UpdatePaymentStatusService updatePaymentStatusService;

    @Mock
    CaseDetailsConverter caseDetailsConverter;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    public static final String BUSINESS_PROCESS = "JUDICIAL_REFERRAL";
    private static final Long CASE_ID = 1594901956117591L;
    public static final String TOKEN = "1234";

    @Test
    public void shouldSubmitCitizenHearingFeePaymentEventIfFeeTypeIsHearing() {

        CaseData caseData = CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        caseData = caseData.toBuilder()
            .ccdState(CASE_PROGRESSION)
            .businessProcess(BusinessProcess.builder()
                                 .status(BusinessProcessStatus.READY)
                                 .camundaEvent(BUSINESS_PROCESS)
                                 .build())
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .hearingFeePaymentDetails(null)
            .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
            caseDetails,
            CITIZEN_HEARING_FEE_PAYMENT
        ));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        updatePaymentStatusService.updatePaymentStatus(FeeType.HEARING, String.valueOf(CASE_ID), getCardPaymentStatusResponse());

        verify(coreCaseDataService, times(1)).getCase(Long.valueOf(CASE_ID));
        verify(coreCaseDataService).startUpdate(String.valueOf(CASE_ID), CITIZEN_HEARING_FEE_PAYMENT);
        verify(coreCaseDataService).submitUpdate(any(), any());

    }

    @Test
    public void shouldSubmitCitizenClaimIssuedFeePaymentEventIfFeeTypeIsClaimIssued() {

        CaseData caseData = CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        caseData = caseData.toBuilder()
            .ccdState(CASE_PROGRESSION)
            .businessProcess(BusinessProcess.builder()
                                 .status(BusinessProcessStatus.READY)
                                 .camundaEvent(BUSINESS_PROCESS)
                                 .build())
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .hearingFeePaymentDetails(null)
            .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
            caseDetails,
            CITIZEN_CLAIM_ISSUE_PAYMENT
        ));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        updatePaymentStatusService.updatePaymentStatus(FeeType.CLAIMISSUED, String.valueOf(CASE_ID), getCardPaymentStatusResponse());

        verify(coreCaseDataService, times(1)).getCase(Long.valueOf(CASE_ID));
        verify(coreCaseDataService).startUpdate(String.valueOf(CASE_ID), CITIZEN_CLAIM_ISSUE_PAYMENT);
        verify(coreCaseDataService).submitUpdate(any(), any());

    }


    @Test
    void shouldThrowCaseDataUpdateExceptionWhenExceptionOccurs() {
        // Arrange
        FeeType feeType = FeeType.CLAIMISSUED; // Replace with an actual FeeType
        String caseReference = "123456";
        CardPaymentStatusResponse cardPaymentStatusResponse = mock(CardPaymentStatusResponse.class);

        when(coreCaseDataService.getCase(Long.valueOf(caseReference))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        CaseDataUpdateException exception = assertThrows(
            CaseDataUpdateException.class,
            () -> updatePaymentStatusService.updatePaymentStatus(feeType, caseReference, cardPaymentStatusResponse)
        );

        // Verify logging (optional; you need a library like LogCaptor for assertions)
        verify(coreCaseDataService).getCase(Long.valueOf(caseReference));

        // Verify no further interactions
        verifyNoInteractions(caseDetailsConverter);
    }

    private CaseDetails buildCaseDetails(CaseData caseData) {
        return CaseDetails.builder()
            .data(objectMapper.convertValue(caseData,
                                            new TypeReference<Map<String, Object>>() {}))
            .id(Long.valueOf(CASE_ID)).build();
    }

    private StartEventResponse startEventResponse(CaseDetails caseDetails, CaseEvent event) {
        return StartEventResponse.builder()
            .token(TOKEN)
            .eventId(event.name())
            .caseDetails(caseDetails)
            .build();
    }

    private CardPaymentStatusResponse getCardPaymentStatusResponse() {
        return CardPaymentStatusResponse.builder().paymentReference("1234").status(PaymentStatus.SUCCESS.name()).build();
    }
}
