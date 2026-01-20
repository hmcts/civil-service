package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentStatusRetryServiceTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentStatusRetryService service;

    private CaseData caseData;

    @BeforeEach
    void setup() {
        caseData = mock(CaseData.class);
    }

    @Test
    void shouldUpdatePaymentStatusUsingCaseData() {
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token("token")
            .eventId("eventId")
            .build();

        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(coreCaseDataService.startUpdate("123", CaseEvent.CITIZEN_CLAIM_ISSUE_PAYMENT))
            .thenReturn(startEventResponse);

        service.updatePaymentStatus(FeeType.CLAIMISSUED, "123", caseData);

        verify(coreCaseDataService).submitUpdate(eq("123"), any());
    }

    @Test
    void shouldUpdatePaymentStatusUsingCardPaymentResponse() {
        CardPaymentStatusResponse response = CardPaymentStatusResponse.builder()
            .status("success")
            .paymentReference("ref")
            .errorCode("err")
            .errorDescription("desc")
            .build();

        CaseDetails caseDetails = mock(CaseDetails.class);
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token("token")
            .eventId("eventId")
            .build();

        when(coreCaseDataService.getCase(123L)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(caseData.isLipvLipOneVOne()).thenReturn(false);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.UNSPEC_CLAIM);
        when(coreCaseDataService.startUpdate("123", CaseEvent.CREATE_CLAIM_AFTER_PAYMENT))
            .thenReturn(startEventResponse);

        service.updatePaymentStatus(FeeType.CLAIMISSUED, "123", response);

        verify(coreCaseDataService).submitUpdate(eq("123"), any());
    }

    @Test
    void shouldResolvePaymentStatus() {
        PaymentStatus status = service.resolvePaymentStatus("success");
        assertThat(status).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void shouldApplyPaymentDetailsForHearingFee() {
        PaymentDetails paymentDetails = new PaymentDetails();

        when(caseData.setHearingFeePaymentDetails(paymentDetails)).thenReturn(caseData);

        CaseData result = service.applyPaymentDetails(caseData, FeeType.HEARING, paymentDetails);

        assertThat(result).isEqualTo(caseData);
        verify(caseData).setHearingFeePaymentDetails(paymentDetails);
    }

    @Test
    void shouldUpdateCaseDataWithPaymentDetails() {
        CardPaymentStatusResponse response = CardPaymentStatusResponse.builder()
            .status("failed")
            .paymentReference("ref")
            .errorCode("code")
            .errorDescription("desc")
            .build();

        when(caseData.getClaimIssuedPaymentDetails()).thenReturn(null);
        when(caseData.setClaimIssuedPaymentDetails(any())).thenReturn(caseData);

        CaseData result = service.updateCaseDataWithPaymentDetails(response, caseData, FeeType.CLAIMISSUED);

        assertThat(result).isEqualTo(caseData);
        verify(caseData).setClaimIssuedPaymentDetails(any(PaymentDetails.class));
    }
}


