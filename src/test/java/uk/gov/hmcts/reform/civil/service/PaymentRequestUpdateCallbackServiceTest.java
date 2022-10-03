package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;

@SpringBootTest(classes = {
    PaymentRequestUpdateCallbackService.class,
    JacksonAutoConfiguration.class,

})
class PaymentRequestUpdateCallbackServiceTest {

    private static final String PAID = "Paid";
    private static final String NOT_PAID = "NotPaid";
    private static final Long CASE_ID = 1594901956117591L;
    public static final String REFERENCE = "123445";
    public static final String ACCOUNT_NUMBER = "123445555";
    public static final String TOKEN = "1234";
    public static final String BUSINESS_PROCESS = "JUDICIAL_REFERRAL";
    @Mock
    ObjectMapper objectMapper;
    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    Time time;
    @Autowired
    PaymentRequestUpdateCallbackService paymentRequestUpdateCallbackService;
    @MockBean
    CaseDetailsConverter caseDetailsConverter;

    @BeforeEach
    public void setup() {
        when(time.now()).thenReturn(LocalDateTime.of(2020, 1, 1, 12, 0, 0));
    }

    @Test
    public void shouldStartAndSubmitEventWithCaseDetails() {

        CaseData caseData = CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        caseData = caseData.toBuilder()
                                .ccdState(CASE_PROGRESSION)
                                .businessProcess(BusinessProcess.builder()
                                                     .status(BusinessProcessStatus.READY)
                                                     .camundaEvent(BUSINESS_PROCESS)
                                .build())
                    .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(caseDetails));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID));

        verify(coreCaseDataService, times(1)).getCase(Long.valueOf(CASE_ID));
        verify(coreCaseDataService, times(1)).startUpdate(any(), any());
        verify(coreCaseDataService, times(1)).submitUpdate(any(), any());
        verify(coreCaseDataService, times(1)).triggerEvent(any(), any());
    }

    @Test
    public void shouldProceed_WhenAdditionalPaymentExist_WithPaymentFail() {

        CaseData caseData = CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        caseData = caseData.toBuilder()
                                .ccdState(CASE_PROGRESSION)
                                .hearingFeePaymentDetails(PaymentDetails.builder()
                                                              .status(FAILED)
                                                              .reference("REFERENCE")
                                                              .build())
                                .businessProcess(BusinessProcess.builder()
                                                     .status(BusinessProcessStatus.READY)
                                                     .camundaEvent(BUSINESS_PROCESS)
                                                     .build())
                                .build();

        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(Long.valueOf(CASE_ID))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails))
            .thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(caseDetails));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID));

        verify(coreCaseDataService, times(1)).getCase(Long.valueOf(CASE_ID));
        verify(coreCaseDataService, times(1)).startUpdate(any(), any());
        verify(coreCaseDataService, times(1)).submitUpdate(any(), any());
        verify(coreCaseDataService, times(1)).triggerEvent(any(), any());

    }

    @Test
    public void shouldNotProceed_WhenPaymentFailed() {
        CaseData caseData = CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        caseData = caseData.toBuilder().ccdState(CASE_PROGRESSION).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(Long.valueOf(CASE_ID))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails))
            .thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(caseDetails));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(NOT_PAID));

        verify(coreCaseDataService, never()).getCase(Long.valueOf(CASE_ID));
        verify(coreCaseDataService, never()).startUpdate(any(), any());
        verify(coreCaseDataService, never()).submitUpdate(any(), any());
        verify(coreCaseDataService, never()).triggerEvent(any(), any());
    }

    private CaseDetails buildCaseDetails(CaseData caseData) {
        return CaseDetails.builder()
            .data(objectMapper.convertValue(caseData,
                                            new TypeReference<Map<String, Object>>() {}))
                                                                .id(Long.valueOf(CASE_ID)).build();
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
                         .build())
            .build();
    }

    private StartEventResponse startEventResponse(CaseDetails caseDetails) {
        return StartEventResponse.builder()
            .token(TOKEN)
            .eventId(HEARING_READINESS.name())
            .caseDetails(caseDetails)
            .build();
    }

}
