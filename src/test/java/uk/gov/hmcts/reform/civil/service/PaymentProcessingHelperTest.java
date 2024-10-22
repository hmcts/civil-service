package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentProcessingHelperTest {

    private static final Long CASE_ID = 1594901956117591L;
    private static final String TOKEN = "1234";

    private PaymentProcessingHelper paymentProcessingHelper;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        paymentProcessingHelper = new PaymentProcessingHelper(caseDetailsConverter, coreCaseDataService, objectMapper);
    }

    @Test
    void shouldGetCaseData() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();
        CaseData caseData = CaseData.builder().build();

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        CaseData result = paymentProcessingHelper.getCaseData(CASE_ID.toString());

        verify(coreCaseDataService).getCase(CASE_ID);
        verify(caseDetailsConverter).toCaseData(caseDetails);
        assertEquals(caseData, result);
    }

    @Test
    void shouldRetrievePaymentDetails() {
        CaseData caseData = CaseData.builder()
            .hearingFeePaymentDetails(PaymentDetails.builder().status(PaymentStatus.SUCCESS).build())
            .build();

        PaymentDetails result = paymentProcessingHelper.retrievePaymentDetails(FeeType.HEARING.name(), caseData);

        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
    }

    @Test
    void shouldUpdateCaseDataWithPaymentDetails() {
        CaseData caseData = CaseData.builder().build();
        PaymentDetails paymentDetails = PaymentDetails.builder().status(PaymentStatus.SUCCESS).build();

        CaseData result = paymentProcessingHelper.updateCaseDataWithPaymentDetails(FeeType.HEARING.name(), caseData, paymentDetails);

        assertEquals(PaymentStatus.SUCCESS, result.getHearingFeePaymentDetails().getStatus());
    }

    @Test
    void shouldCreateAndSubmitEvent() {
        CaseData caseData = CaseData.builder().build();
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token(TOKEN)
            .eventId(CaseEvent.SERVICE_REQUEST_RECEIVED.name())
            .caseDetails(caseDetails)
            .build();

        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse);
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        paymentProcessingHelper.createAndSubmitEvent(caseData, CASE_ID.toString(), FeeType.HEARING.name(), "UpdatePaymentStatus");

        verify(coreCaseDataService).startUpdate(any(), any());
        verify(coreCaseDataService).submitUpdate(any(), any());
    }

    @Test
    void shouldSubmitCaseDataWithoutEvent() {
        CaseData caseData = CaseData.builder().build();

        paymentProcessingHelper.submitCaseDataWithoutEvent(caseData, CASE_ID.toString());

        verify(coreCaseDataService).submitUpdate(any(), any());
    }

    @Test
    void shouldBuildCaseDataContent() {
        CaseData caseData = CaseData.builder().build();
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token(TOKEN)
            .eventId(CaseEvent.SERVICE_REQUEST_RECEIVED.name())
            .build();

        CaseDataContent result = paymentProcessingHelper.buildCaseDataContent(startEventResponse, caseData);

        assertEquals(TOKEN, result.getEventToken());
        assertEquals(CaseEvent.SERVICE_REQUEST_RECEIVED.name(), result.getEvent().getId());
    }

    @Test
    void shouldGetEventNameFromFeeTypeForPaymentRequestUpdate() {
        CaseData caseData = CaseData.builder().caseAccessCategory(CaseCategory.SPEC_CLAIM).build();

        CaseEvent result = paymentProcessingHelper.getEventNameFromFeeType(caseData, FeeType.CLAIMISSUED.name(), "PaymentRequestUpdate");

        assertEquals(CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT, result);
    }

    @Test
    void shouldGetEventNameFromFeeTypeForUpdatePaymentStatus() {
        CaseEvent result = paymentProcessingHelper.getEventNameFromFeeType(null, FeeType.HEARING.name(), "UpdatePaymentStatus");

        assertEquals(CaseEvent.CITIZEN_HEARING_FEE_PAYMENT, result);
    }

    @Test
    void shouldValidatePaymentUpdateHearing() {
        CaseData caseData = CaseData.builder().hearingFeePaymentDetails(PaymentDetails.builder().status(PaymentStatus.FAILED).build()).build();

        boolean result = paymentProcessingHelper.isValidPaymentUpdateHearing(FeeType.HEARING.name(), caseData);

        assertTrue(result);
    }

    @Test
    void shouldValidateUpdatePaymentClaimIssue() {
        CaseData caseData = CaseData.builder().claimIssuedPaymentDetails(PaymentDetails.builder().status(PaymentStatus.FAILED).build()).build();

        boolean result = paymentProcessingHelper.isValidUpdatePaymentClaimIssue(FeeType.CLAIMISSUED.name(), caseData);

        assertTrue(result);
    }

    @Test
    void shouldValidatePaymentUpdateHearingWhenPaymentDetailsAreNull() {
        CaseData caseData = CaseData.builder().hearingFeePaymentDetails(null).build();

        boolean result = paymentProcessingHelper.isValidPaymentUpdateHearing(FeeType.HEARING.name(), caseData);

        assertTrue(result);
    }

    @Test
    void shouldValidatePaymentUpdateHearingWhenPaymentDetailsStatusIsFailed() {
        CaseData caseData = CaseData.builder()
            .hearingFeePaymentDetails(PaymentDetails.builder().status(PaymentStatus.FAILED).build())
            .build();

        boolean result = paymentProcessingHelper.isValidPaymentUpdateHearing(FeeType.HEARING.name(), caseData);

        assertTrue(result);
    }

    @Test
    void shouldValidateUpdatePaymentClaimIssueWhenPaymentDetailsAreNull() {
        CaseData caseData = CaseData.builder().claimIssuedPaymentDetails(null).build();

        boolean result = paymentProcessingHelper.isValidUpdatePaymentClaimIssue(FeeType.CLAIMISSUED.name(), caseData);

        assertTrue(result);
    }

    @Test
    void shouldValidateUpdatePaymentClaimIssueWhenPaymentDetailsStatusIsFailed() {
        CaseData caseData = CaseData.builder()
            .claimIssuedPaymentDetails(PaymentDetails.builder().status(PaymentStatus.FAILED).build())
            .build();

        boolean result = paymentProcessingHelper.isValidUpdatePaymentClaimIssue(FeeType.CLAIMISSUED.name(), caseData);

        assertTrue(result);
    }

    @Test
    void shouldRetrievePaymentDetailsForClaimIssued() {
        CaseData caseData = CaseData.builder()
            .claimIssuedPaymentDetails(PaymentDetails.builder().status(PaymentStatus.SUCCESS).build())
            .build();

        PaymentDetails result = paymentProcessingHelper.retrievePaymentDetails(FeeType.CLAIMISSUED.name(), caseData);

        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
    }

    @Test
    void shouldUpdateCaseDataWithClaimIssuedPaymentDetails() {
        CaseData caseData = CaseData.builder().build();
        PaymentDetails paymentDetails = PaymentDetails.builder().status(PaymentStatus.SUCCESS).build();

        CaseData result = paymentProcessingHelper.updateCaseDataWithPaymentDetails(FeeType.CLAIMISSUED.name(), caseData, paymentDetails);

        assertEquals(PaymentStatus.SUCCESS, result.getClaimIssuedPaymentDetails().getStatus());
    }

    @Test
    void shouldThrowExceptionForUnknownServiceIdentifier() {
        CaseData caseData = CaseData.builder().build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            paymentProcessingHelper.getEventNameFromFeeType(caseData, FeeType.HEARING.name(), "UnknownService"));

        assertEquals("Unknown service identifier: UnknownService", exception.getMessage());
    }

    @Test
    void shouldResolvePaymentRequestUpdateEventForHearing() {
        CaseData caseData = CaseData.builder().build();

        CaseEvent result = paymentProcessingHelper.getEventNameFromFeeType(caseData, FeeType.HEARING.name(), "PaymentRequestUpdate");

        assertEquals(CaseEvent.SERVICE_REQUEST_RECEIVED, result);
    }

    @Test
    void shouldResolveUpdatePaymentStatusEventForClaimIssued() {
        CaseEvent result = paymentProcessingHelper.getEventNameFromFeeType(null, FeeType.CLAIMISSUED.name(), "UpdatePaymentStatus");

        assertEquals(CaseEvent.CITIZEN_CLAIM_ISSUE_PAYMENT, result);
    }
}
