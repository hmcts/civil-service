package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentRequestUpdateCallbackServiceTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private UpdatePaymentStatusService updatePaymentStatusService;

    private PaymentRequestUpdateCallbackService paymentRequestUpdateCallbackService;

    private ServiceRequestUpdateDto serviceRequestUpdateDto;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        paymentRequestUpdateCallbackService = new PaymentRequestUpdateCallbackService(caseDetailsConverter, coreCaseDataService,
                                                                                      objectMapper, updatePaymentStatusService);
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber("123")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder().paymentReference("ref").customerReference("custRef").build())
            .build();

        caseData = CaseData.builder().build();
    }

    @Test
    void shouldProcessCallbackAndCreateEvent() {
        CaseDetails caseDetails = CaseDetails.builder().build();
        StartEventResponse startEventResponse = StartEventResponse.builder().token("token").eventId("eventId").build();

        when(coreCaseDataService.getCase(anyLong())).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse);

        paymentRequestUpdateCallbackService.processCallback(serviceRequestUpdateDto, FeeType.HEARING.name());

        verify(coreCaseDataService).startUpdate(any(), any());
        verify(coreCaseDataService).submitUpdate(any(), any());
    }

    @Test
    void shouldUpdateCaseDataWithStateAndPaymentDetails() {
        PaymentDetails paymentDetails = PaymentDetails.builder().status(PaymentStatus.SUCCESS).build();
        caseData = CaseData.builder().hearingFeePaymentDetails(paymentDetails).build();

        when(coreCaseDataService.getCase(anyLong())).thenReturn(CaseDetails.builder().build());
        when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(serviceRequestUpdateDto, FeeType.HEARING.name());

        assertEquals(PaymentStatus.SUCCESS, caseData.getHearingFeePaymentDetails().getStatus());
    }
}
