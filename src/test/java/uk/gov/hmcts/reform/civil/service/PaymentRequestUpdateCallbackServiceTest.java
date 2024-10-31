package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentRequestUpdateCallbackServiceTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private UpdatePaymentStatusService updatePaymentStatusService;

    @Mock
    private PaymentServiceHelper paymentServiceHelper;

    @InjectMocks
    private PaymentRequestUpdateCallbackService paymentRequestUpdateCallbackService;

    private ServiceRequestUpdateDto serviceRequestUpdateDto;
    private CaseDetails caseDetails;
    private CaseData caseData;
    private PaymentDetails paymentDetails;

    @BeforeEach
    void setUp() {
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber("1234")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder().paymentReference("NEW REF").build())
            .build();

        caseDetails = CaseDetails.builder().id(1234L).build();
        paymentDetails = PaymentDetails.builder().status(PaymentStatus.FAILED).build();
        caseData = CaseData.builder().hearingFeePaymentDetails(paymentDetails).build();
    }

    @Test
    void shouldProcessCallbackAndUpdatePaymentStatus() {
        when(coreCaseDataService.getCase(anyLong())).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);
        when(paymentServiceHelper.buildPaymentDetails(any(CardPaymentStatusResponse.class)))
            .thenReturn(paymentDetails);
        when(paymentServiceHelper.updateCaseDataByFeeType(any(CaseData.class), anyString(), any(PaymentDetails.class)))
            .thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(serviceRequestUpdateDto, FeeType.HEARING.name());

        verify(coreCaseDataService).getCase(anyLong());
        verify(caseDetailsConverter).toCaseData(any(CaseDetails.class));
        verify(paymentServiceHelper).buildPaymentDetails(any(CardPaymentStatusResponse.class));
        verify(paymentServiceHelper).updateCaseDataByFeeType(any(CaseData.class), anyString(), any(PaymentDetails.class));
        verify(paymentServiceHelper).createEvent(any(CaseData.class), anyString(), anyString(), true);
    }

    @Test
    void shouldNotProcessCallbackWhenStatusIsNotPaid() {
        serviceRequestUpdateDto.setServiceRequestStatus("NotPaid");

        paymentRequestUpdateCallbackService.processCallback(serviceRequestUpdateDto, FeeType.HEARING.name());

        verifyNoInteractions(coreCaseDataService, caseDetailsConverter, paymentServiceHelper, updatePaymentStatusService);
    }
}
