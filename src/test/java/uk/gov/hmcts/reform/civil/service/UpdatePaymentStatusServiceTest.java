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
    void shouldUpdatePaymentStatusAndSubmitEvent() {
        CaseData caseData = CaseData.builder().build();
        String caseReference = "1234";
        FeeType feeType = FeeType.HEARING;
        PaymentDetails existingPaymentDetails = PaymentDetails.builder()
            .status(PaymentStatus.FAILED)
            .reference("OLD REF")
            .build();
        CardPaymentStatusResponse cardPaymentStatusResponse = CardPaymentStatusResponse.builder()
            .paymentReference("NEW REF")
            .status(PaymentStatus.SUCCESS.name())
            .build();

        when(paymentProcessingHelper.getCaseData(caseReference)).thenReturn(caseData);
        when(paymentProcessingHelper.retrievePaymentDetails(feeType.name(), caseData)).thenReturn(existingPaymentDetails);

        PaymentDetails updatedPaymentDetails = existingPaymentDetails.toBuilder()
            .status(PaymentStatus.SUCCESS)
            .reference(cardPaymentStatusResponse.getPaymentReference())
            .errorCode(cardPaymentStatusResponse.getErrorCode())
            .errorMessage(cardPaymentStatusResponse.getErrorDescription())
            .build();

        when(paymentProcessingHelper.updateCaseDataWithPaymentDetails(feeType.name(), caseData, updatedPaymentDetails))
            .thenReturn(caseData);

        updatePaymentStatusService.updatePaymentStatus(feeType, caseReference, cardPaymentStatusResponse);

        verify(paymentProcessingHelper).getCaseData(caseReference);
        verify(paymentProcessingHelper).retrievePaymentDetails(feeType.name(), caseData);
        verify(paymentProcessingHelper).updateCaseDataWithPaymentDetails(feeType.name(), caseData, updatedPaymentDetails);
        verify(paymentProcessingHelper).createAndSubmitEvent(caseData, caseReference, feeType.name(), "UpdatePaymentStatus");
        verifyNoMoreInteractions(paymentProcessingHelper);
    }

    @Test
    void shouldThrowCaseDataUpdateExceptionWhenExceptionOccurs() {
        String caseReference = "1234";
        FeeType feeType = FeeType.HEARING;
        CardPaymentStatusResponse cardPaymentStatusResponse = CardPaymentStatusResponse.builder()
            .paymentReference("NEW REF")
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
