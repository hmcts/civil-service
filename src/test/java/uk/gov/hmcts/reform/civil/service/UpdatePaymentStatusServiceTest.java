package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_CLAIM_ISSUE_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_HEARING_FEE_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;

@SpringBootTest(classes = {
    UpdatePaymentStatusService.class,
    JacksonAutoConfiguration.class

})
class UpdatePaymentStatusServiceTest {

    @MockBean
    FeatureToggleService featureToggleService;
    @MockBean
    CaseDetailsConverter caseDetailsConverter;
    @Mock
    ObjectMapper objectMapper;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @Autowired
    UpdatePaymentStatusService updatePaymentStatusService;

    public static final String BUSINESS_PROCESS = "JUDICIAL_REFERRAL";
    private static final Long CASE_ID = 1594901956117591L;
    public static final String TOKEN = "1234";

    @Test
    public void shouldSubmitCitizenHearingFeePaymentEventIfFeeTypeIsHearing() {

        CaseData caseData = CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        caseData = caseData.toBuilder()
            .ccdState(CASE_PROGRESSION)
            .businessProcess(BusinessProcess.builder()
                                 .status(BusinessProcessStatus.READY)
                                 .camundaEvent(BUSINESS_PROCESS)
                                 .build())
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .hearingFeePaymentDetails(null)
            .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
            caseDetails,
            CITIZEN_HEARING_FEE_PAYMENT
        ));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        updatePaymentStatusService.updatePaymentStatus(FeeType.HEARING, String.valueOf(CASE_ID), getCardPaymentStatusResponse());

        verify(coreCaseDataService, times(1)).getCase(Long.valueOf(CASE_ID));
        verify(coreCaseDataService).startUpdate(String.valueOf(CASE_ID), CITIZEN_HEARING_FEE_PAYMENT);
        verify(coreCaseDataService).submitUpdate(any(), any());

    }

    @Test
    public void shouldSubmitCitizenClaimIssuedFeePaymentEventIfFeeTypeIsClaimIssued() {

        CaseData caseData = CaseDataBuilder.builder().receiveUpdatePaymentRequest().build();
        caseData = caseData.toBuilder()
            .ccdState(CASE_PROGRESSION)
            .businessProcess(BusinessProcess.builder()
                                 .status(BusinessProcessStatus.READY)
                                 .camundaEvent(BUSINESS_PROCESS)
                                 .build())
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .hearingFeePaymentDetails(null)
            .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
            caseDetails,
            CITIZEN_CLAIM_ISSUE_PAYMENT
        ));
        when(coreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        updatePaymentStatusService.updatePaymentStatus(FeeType.CLAIMISSUED, String.valueOf(CASE_ID), getCardPaymentStatusResponse());

        verify(coreCaseDataService, times(1)).getCase(Long.valueOf(CASE_ID));
        verify(coreCaseDataService).startUpdate(String.valueOf(CASE_ID), CITIZEN_CLAIM_ISSUE_PAYMENT);
        verify(coreCaseDataService).submitUpdate(any(), any());

    }

    private CaseDetails buildCaseDetails(CaseData caseData) {
        return CaseDetails.builder()
            .data(objectMapper.convertValue(caseData,
                                            new TypeReference<Map<String, Object>>() {}))
            .id(Long.valueOf(CASE_ID)).build();
    }

    private StartEventResponse startEventResponse(CaseDetails caseDetails, CaseEvent event) {
        return StartEventResponse.builder()
            .token(TOKEN)
            .eventId(event.name())
            .caseDetails(caseDetails)
            .build();
    }

    private CardPaymentStatusResponse getCardPaymentStatusResponse() {
        return CardPaymentStatusResponse.builder().paymentReference("1234").status(PaymentStatus.SUCCESS.name()).build();
    }
}
