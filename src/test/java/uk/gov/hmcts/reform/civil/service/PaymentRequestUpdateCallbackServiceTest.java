package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentRequestUpdateCallbackServiceTest {

    private static final String PAID = "Paid";
    private static final String NOT_PAID = "NotPaid";
    private static final Long CASE_ID = 1594901956117591L;

    @InjectMocks
    private PaymentRequestUpdateCallbackService service;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private PaymentStatusRetryService retryService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldNotProceed_whenPaymentIsNotPaid() {
        service.processCallback(buildServiceDto(NOT_PAID), FeeType.HEARING.name());

        verifyNoInteractions(coreCaseDataService, retryService);
    }

    @Test
    void shouldNotProceed_whenFeeTypeIsInvalid() {
        service.processCallback(buildServiceDto(PAID), "INVALID");

        verifyNoInteractions(coreCaseDataService, retryService);
    }

    @Test
    void shouldCallRetryService_whenPaidAndFeeTypeHandled() {
        CaseData caseData = CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        when(retryService.updateCaseDataWithPaymentDetails(any(), any(), any(), any()))
            .thenAnswer(invocation -> invocation.getArgument(1));

        service.processCallback(buildServiceDto(PAID), FeeType.HEARING.name());

        verify(retryService).updatePaymentStatus(
            FeeType.HEARING,
            CASE_ID.toString(),
            caseData
        );
    }

    @Test
    void shouldUseCustomerReferenceFromCallback() {
        CaseData caseData = CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        ServiceRequestUpdateDto dto = buildServiceDto(PAID);
        dto.getPayment().setCustomerReference("CUSTOMER-123"); // override to test reference

        when(retryService.updateCaseDataWithPaymentDetails(any(), any(), any(), any()))
            .thenAnswer(invocation -> invocation.getArgument(1));

        service.processCallback(dto, FeeType.HEARING.name());

        verify(retryService).updatePaymentStatus(
            FeeType.HEARING,
            CASE_ID.toString(),
            caseData
        );
    }

    private CaseDetails buildCaseDetails(CaseData caseData) {
        return CaseDetails.builder()
            .id(CASE_ID)
            .data(objectMapper.convertValue(caseData, new TypeReference<>() {}))
            .build();
    }

    private ServiceRequestUpdateDto buildServiceDto(String status) {
        return new ServiceRequestUpdateDto()
            .setCcdCaseNumber(CASE_ID.toString())
            .setServiceRequestStatus(status)
            .setPayment(PaymentDto.builder()
                .customerReference("CUST-REF")
                .paymentReference("123")
                .build());
    }
}
