package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
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
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private PaymentServiceHelper paymentServiceHelper;

    @Test
    void shouldUpdatePaymentStatusAndSubmitEvent() {
        String caseReference = "1234";
        FeeType feeType = FeeType.HEARING;
        CaseDetails caseDetails = CaseDetails.builder().build();
        CaseData caseData = CaseData.builder()
            .hearingFeePaymentDetails(PaymentDetails.builder()
                                          .status(PaymentStatus.FAILED)
                                          .reference("OLD REF")
                                          .build())
            .build();
        CardPaymentStatusResponse cardPaymentStatusResponse = CardPaymentStatusResponse.builder()
            .paymentReference("NEW REF")
            .status(PaymentStatus.SUCCESS.name())
            .build();
        PaymentDetails updatedPaymentDetails = PaymentDetails.builder()
            .status(PaymentStatus.SUCCESS)
            .reference("NEW REF")
            .build();

        when(coreCaseDataService.getCase(Long.valueOf(caseReference))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(paymentServiceHelper.buildPaymentDetails(cardPaymentStatusResponse))
            .thenReturn(updatedPaymentDetails);
        when(paymentServiceHelper.updateCaseDataByFeeType(caseData, feeType.name(), updatedPaymentDetails))
            .thenReturn(caseData);

        updatePaymentStatusService.updatePaymentStatus(feeType, caseReference, cardPaymentStatusResponse);

        verify(coreCaseDataService).getCase(Long.valueOf(caseReference));
        verify(caseDetailsConverter).toCaseData(caseDetails);
        verify(paymentServiceHelper).buildPaymentDetails(cardPaymentStatusResponse);
        verify(paymentServiceHelper).updateCaseDataByFeeType(caseData, feeType.name(), updatedPaymentDetails);
        verify(paymentServiceHelper).createEvent(caseData, caseReference, feeType.name(), false);
        verifyNoMoreInteractions(coreCaseDataService, caseDetailsConverter, paymentServiceHelper);
    }

    @Test
    void shouldThrowCaseDataUpdateExceptionWhenExceptionOccurs() {
        String caseReference = "1234";
        FeeType feeType = FeeType.HEARING;
        CardPaymentStatusResponse cardPaymentStatusResponse = CardPaymentStatusResponse.builder()
            .paymentReference("NEW REF")
            .status(PaymentStatus.SUCCESS.name())
            .build();

        when(coreCaseDataService.getCase(Long.valueOf(caseReference))).thenThrow(new RuntimeException("Test exception"));

        assertThrows(CaseDataUpdateException.class, () ->
            updatePaymentStatusService.updatePaymentStatus(feeType, caseReference, cardPaymentStatusResponse)
        );

        verify(coreCaseDataService).getCase(Long.valueOf(caseReference));
        verifyNoMoreInteractions(coreCaseDataService, caseDetailsConverter, paymentServiceHelper);
    }
}
