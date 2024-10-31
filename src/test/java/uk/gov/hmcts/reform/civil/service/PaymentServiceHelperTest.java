package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceHelperTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    private PaymentServiceHelper paymentServiceHelper;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        paymentServiceHelper = new PaymentServiceHelper(coreCaseDataService, objectMapper);
    }

    @Test
    void shouldCreateEventForHearingFee() {
        CaseData caseData = CaseData.builder().build();
        StartEventResponse startEventResponse = StartEventResponse.builder().token("token").eventId("eventId").build();
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse);

        paymentServiceHelper.createEvent(caseData, "123", FeeType.HEARING.name());

        verify(coreCaseDataService).startUpdate("123", CaseEvent.SERVICE_REQUEST_RECEIVED);
        verify(coreCaseDataService).submitUpdate(any(), any(CaseDataContent.class));
    }

    @Test
    void shouldCreateEventForClaimIssuedSpec() {
        CaseData caseData = CaseData.builder().caseAccessCategory(CaseCategory.SPEC_CLAIM).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().token("token").eventId("eventId").build();
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse);

        paymentServiceHelper.createEvent(caseData, "123", FeeType.CLAIMISSUED.name());

        verify(coreCaseDataService).startUpdate("123", CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT);
        verify(coreCaseDataService).submitUpdate(any(), any(CaseDataContent.class));
    }

    @Test
    void shouldCreateEventForClaimIssuedUnspec() {
        CaseData caseData = CaseData.builder().caseAccessCategory(CaseCategory.UNSPEC_CLAIM).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().token("token").eventId("eventId").build();
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse);

        paymentServiceHelper.createEvent(caseData, "123", FeeType.CLAIMISSUED.name());

        verify(coreCaseDataService).startUpdate("123", CaseEvent.CREATE_CLAIM_AFTER_PAYMENT);
        verify(coreCaseDataService).submitUpdate(any(), any(CaseDataContent.class));
    }

    @Test
    void shouldCreateEventForOtherFeeType() {
        CaseData caseData = CaseData.builder().build();
        StartEventResponse startEventResponse = StartEventResponse.builder().token("token").eventId("eventId").build();
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse);

        paymentServiceHelper.createEvent(caseData, "123", "OTHER_FEE_TYPE");

        verify(coreCaseDataService).startUpdate("123", CaseEvent.RESUBMIT_CLAIM);
        verify(coreCaseDataService).submitUpdate(any(), any(CaseDataContent.class));
    }

    @Test
    void shouldUpdateCaseDataByFeeType() {
        CaseData caseData = CaseData.builder().build();
        PaymentDetails paymentDetails = PaymentDetails.builder().status(PaymentStatus.SUCCESS).build();

        CaseData updatedCaseData = paymentServiceHelper.updateCaseDataByFeeType(caseData, FeeType.HEARING.name(), paymentDetails);

        assertEquals(paymentDetails, updatedCaseData.getHearingFeePaymentDetails());
    }

    @Test
    void shouldBuildPaymentDetails() {
        CardPaymentStatusResponse response = CardPaymentStatusResponse.builder().status("SUCCESS").paymentReference("ref").build();

        PaymentDetails paymentDetails = paymentServiceHelper.buildPaymentDetails(response);

        assertEquals(PaymentStatus.SUCCESS, paymentDetails.getStatus());
        assertEquals("ref", paymentDetails.getReference());
    }
}
