package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePaymentStatusServiceTest {

    @InjectMocks
    private UpdatePaymentStatusService updatePaymentStatusService;

    @Mock
    private PaymentProcessingHelper paymentProcessingHelper;

    @Test
    void shouldUpdatePaymentStatusAndSubmitEventForNonLiPCase() {
        String caseReference = "1594901956117591";
        FeeType feeType = FeeType.HEARING;
        CaseData caseData = mock(CaseData.class);
        PaymentDetails existingPaymentDetails = mock(PaymentDetails.class);
        PaymentDetails.PaymentDetailsBuilder paymentDetailsBuilder = PaymentDetails.builder();

        CardPaymentStatusResponse cardPaymentStatusResponse = CardPaymentStatusResponse.builder()
            .paymentReference("1234")
            .status(PaymentStatus.SUCCESS.name())
            .build();

        when(paymentProcessingHelper.getCaseData(caseReference)).thenReturn(caseData);
        when(paymentProcessingHelper.retrievePaymentDetails(feeType.name(), caseData)).thenReturn(existingPaymentDetails);
        when(existingPaymentDetails.toBuilder()).thenReturn(paymentDetailsBuilder);

        PaymentDetails paymentDetails = paymentDetailsBuilder
            .status(PaymentStatus.valueOf(cardPaymentStatusResponse.getStatus().toUpperCase()))
            .reference(cardPaymentStatusResponse.getPaymentReference())
            .errorCode(cardPaymentStatusResponse.getErrorCode())
            .errorMessage(cardPaymentStatusResponse.getErrorDescription())
            .build();

        when(paymentProcessingHelper.updateCaseDataWithPaymentDetails(feeType.name(), caseData, paymentDetails))
            .thenReturn(caseData);

        when(caseData.isLipvLipOneVOne()).thenReturn(false);

        updatePaymentStatusService.updatePaymentStatus(feeType, caseReference, cardPaymentStatusResponse);

        verify(paymentProcessingHelper).getCaseData(caseReference);
        verify(paymentProcessingHelper).retrievePaymentDetails(feeType.name(), caseData);
        verify(existingPaymentDetails).toBuilder();
        verify(paymentProcessingHelper).updateCaseDataWithPaymentDetails(feeType.name(), caseData, paymentDetails);
        verify(caseData).isLipvLipOneVOne();
        verify(paymentProcessingHelper).createAndSubmitEvent(caseData, caseReference, feeType.name(), "UpdatePaymentStatus");
        verifyNoMoreInteractions(paymentProcessingHelper);
    }

    @Test
    void shouldUpdatePaymentStatusAndSubmitCaseDataWithoutEventForLiPCase() {
        String caseReference = "1594901956117591";
        FeeType feeType = FeeType.CLAIMISSUED;
        CaseData caseData = mock(CaseData.class);
        PaymentDetails existingPaymentDetails = mock(PaymentDetails.class);
        PaymentDetails.PaymentDetailsBuilder paymentDetailsBuilder = PaymentDetails.builder();

        CardPaymentStatusResponse cardPaymentStatusResponse = CardPaymentStatusResponse.builder()
            .paymentReference("5678")
            .status(PaymentStatus.SUCCESS.name())
            .build();

        when(paymentProcessingHelper.getCaseData(caseReference)).thenReturn(caseData);
        when(paymentProcessingHelper.retrievePaymentDetails(feeType.name(), caseData)).thenReturn(existingPaymentDetails);
        when(existingPaymentDetails.toBuilder()).thenReturn(paymentDetailsBuilder);

        PaymentDetails paymentDetails = paymentDetailsBuilder
            .status(PaymentStatus.valueOf(cardPaymentStatusResponse.getStatus().toUpperCase()))
            .reference(cardPaymentStatusResponse.getPaymentReference())
            .errorCode(cardPaymentStatusResponse.getErrorCode())
            .errorMessage(cardPaymentStatusResponse.getErrorDescription())
            .build();

        when(paymentProcessingHelper.updateCaseDataWithPaymentDetails(feeType.name(), caseData, paymentDetails))
            .thenReturn(caseData);

        when(caseData.isLipvLipOneVOne()).thenReturn(true);

        updatePaymentStatusService.updatePaymentStatus(feeType, caseReference, cardPaymentStatusResponse);

        verify(paymentProcessingHelper).getCaseData(caseReference);
        verify(paymentProcessingHelper).retrievePaymentDetails(feeType.name(), caseData);
        verify(existingPaymentDetails).toBuilder();
        verify(paymentProcessingHelper).updateCaseDataWithPaymentDetails(feeType.name(), caseData, paymentDetails);
        verify(caseData).isLipvLipOneVOne();
        verify(paymentProcessingHelper).submitCaseDataWithoutEvent(caseData, caseReference);
        verifyNoMoreInteractions(paymentProcessingHelper);
    }

    @Test
    void shouldThrowCaseDataUpdateExceptionWhenExceptionOccurs() {
        String caseReference = "1594901956117591";
        FeeType feeType = FeeType.HEARING;
        CardPaymentStatusResponse cardPaymentStatusResponse = CardPaymentStatusResponse.builder()
            .paymentReference("1234")
            .status(PaymentStatus.SUCCESS.name())
            .build();

        when(paymentProcessingHelper.getCaseData(caseReference)).thenThrow(new RuntimeException("Test exception"));

        assertThrows(CaseDataUpdateException.class, () ->
            updatePaymentStatusService.updatePaymentStatus(feeType, caseReference, cardPaymentStatusResponse)
        );

        verify(paymentProcessingHelper).getCaseData(caseReference);
        verifyNoMoreInteractions(paymentProcessingHelper);
    }
}
