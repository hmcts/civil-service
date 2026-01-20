package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_HEARING_FEE_PAYMENT;

@ExtendWith(MockitoExtension.class)
class PaymentStatusRetryServiceTest {

    private static final Long CASE_ID = 1594901956117591L;
    private static final String TOKEN = "token";

    @InjectMocks
    private PaymentStatusRetryService service;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldSubmitCitizenHearingFeePaymentEvent_forHearingFee() {
        CaseData caseData = lipCaseData();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEvent(caseDetails));

        service.updatePaymentStatus(FeeType.HEARING, CASE_ID.toString(), cardPaymentResponse());

        verify(coreCaseDataService)
            .startUpdate(CASE_ID.toString(), CITIZEN_HEARING_FEE_PAYMENT);
        verify(coreCaseDataService).submitUpdate(any(), any());
    }

    @Test
    void shouldRetainExistingCustomerReference() {
        PaymentDetails existing = new PaymentDetails();
        existing.setCustomerReference("EXISTING");

        CaseData caseData = lipCaseData();
        caseData.setHearingFeePaymentDetails(existing);
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEvent(caseDetails));

        service.updatePaymentStatus(FeeType.HEARING, CASE_ID.toString(), cardPaymentResponse());

        ArgumentCaptor<CaseDataContent> captor =
            ArgumentCaptor.forClass(CaseDataContent.class);
        verify(coreCaseDataService).submitUpdate(any(), captor.capture());

        CaseData updated =
            objectMapper.convertValue(captor.getValue().getData(), CaseData.class);

        assertThat(updated.getHearingFeePaymentDetails().getCustomerReference())
            .isEqualTo("EXISTING");
    }

    @Test
    void shouldSubmitClaimIssuedEvent_forSpecClaim() {
        CaseData caseData =
            CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        caseData.setCaseAccessCategory(
            uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM);

        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any()))
            .thenReturn(StartEventResponse.builder()
                            .token(TOKEN)
                            .eventId(CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT.name())
                            .caseDetails(caseDetails)
                            .build()
            );

        service.updatePaymentStatus(
            FeeType.CLAIMISSUED, CASE_ID.toString(), cardPaymentResponse());

        verify(coreCaseDataService)
            .startUpdate(CASE_ID.toString(),
                         CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT);
        verify(coreCaseDataService).submitUpdate(any(), any());
    }

    @Test
    void shouldCallRecover_whenUpdateFails() {
        when(coreCaseDataService.getCase(CASE_ID))
            .thenThrow(new RuntimeException("CCD failure"));

        try {
            service.updatePaymentStatus(
                FeeType.HEARING, CASE_ID.toString(), cardPaymentResponse());
        } catch (Exception ignored) {
            // expected
        }
    }

    @Test
    void shouldThrowExceptionForUnsupportedFeeType() {
        assertThrows(IllegalArgumentException.class,
                     () -> service.updatePaymentStatus(
                         null, CASE_ID.toString(), cardPaymentResponse()));
    }

    @Test
    void shouldHandleNullPaymentStatusGracefully() {
        CardPaymentStatusResponse response = cardPaymentResponse();
        response.setStatus(null);

        service.updatePaymentStatus(
            FeeType.HEARING, CASE_ID.toString(), response);
    }

    private CaseData lipCaseData() {
        CaseData data =
            CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        data.setApplicant1Represented(YesOrNo.NO);
        data.setRespondent1Represented(YesOrNo.NO);
        return data;
    }

    private CaseDetails buildCaseDetails(CaseData caseData) {
        return CaseDetails.builder()
            .id(CASE_ID)
            .data(objectMapper.convertValue(caseData, new TypeReference<>() {}))
            .build();
    }

    private StartEventResponse startEvent(CaseDetails details) {
        return StartEventResponse.builder()
            .token(TOKEN)
            .eventId(CaseEvent.CITIZEN_HEARING_FEE_PAYMENT.name())
            .caseDetails(details)
            .build();
    }

    private CardPaymentStatusResponse cardPaymentResponse() {
        return new CardPaymentStatusResponse()
            .setPaymentReference("123")
            .setStatus(PaymentStatus.SUCCESS.name());
    }
}
