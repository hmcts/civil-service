package uk.gov.hmcts.reform.civil.ga.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;

@ExtendWith(MockitoExtension.class)
class UpdatePaymentStatusServiceTest {

    @Mock
    CaseDetailsConverter caseDetailsConverter;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    private GaCoreCaseDataService gaCoreCaseDataService;
    @InjectMocks
    UpdatePaymentStatusService updatePaymentStatusService;

    public static final String BUSINESS_PROCESS = "JUDICIAL_REFERRAL";
    private static final Long CASE_ID = 1594901956117591L;
    public static final String TOKEN = "1234";

    @Test
    public void shouldSubmitCitizenApplicationFeePaymentEvent() {

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(CASE_PROGRESSION)
            .businessProcess(new BusinessProcess()
                                 .setStatus(BusinessProcessStatus.READY)
                                 .setCamundaEvent(BUSINESS_PROCESS))
            .generalAppPBADetails(new GeneralApplicationPbaDetails().setPaymentDetails(new PaymentDetails()
                    .setCustomerReference("RC-1604-0739-2145-4711")
                    ))
            .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(gaCoreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);
        when(gaCoreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
            caseDetails,
            INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT
        ));
        when(gaCoreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        updatePaymentStatusService.updatePaymentStatus(String.valueOf(CASE_ID), getCardPaymentStatusResponse());

        verify(gaCoreCaseDataService, times(1)).getCase(CASE_ID);
        verify(gaCoreCaseDataService).startUpdate(String.valueOf(CASE_ID), INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT);
        verify(gaCoreCaseDataService).submitUpdate(any(), any());

    }

    @Test
    public void shouldSubmitCitizenAdditionalFeePaymentEvent() {

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(CASE_PROGRESSION)
            .businessProcess(new BusinessProcess()
                                 .setStatus(BusinessProcessStatus.READY)
                                 .setCamundaEvent(BUSINESS_PROCESS))
            .generalAppPBADetails(new GeneralApplicationPbaDetails().setAdditionalPaymentDetails(new PaymentDetails()
                                                                            .setCustomerReference("RC-1604-0739-2145-4711")
                                                                            )
                                      .setAdditionalPaymentServiceRef("2023-1701090705600"))
            .applicationFeeAmountInPence(new BigDecimal("10000"))
            .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(gaCoreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);
        when(gaCoreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
            caseDetails,
            MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID
        ));
        when(gaCoreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        updatePaymentStatusService.updatePaymentStatus(String.valueOf(CASE_ID), getCardPaymentStatusResponse());

        verify(gaCoreCaseDataService, times(1)).getCase(CASE_ID);
        verify(gaCoreCaseDataService).startUpdate(String.valueOf(CASE_ID), MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID);
        verify(gaCoreCaseDataService).submitUpdate(any(), any());

    }

    private CaseDetails buildCaseDetails(GeneralApplicationCaseData caseData) {
        return CaseDetails.builder()
            .data(objectMapper.convertValue(
                caseData,
                new TypeReference<Map<String, Object>>() {
                }
            ))
            .id(CASE_ID).build();
    }

    private StartEventResponse startEventResponse(CaseDetails caseDetails, CaseEvent event) {
        return StartEventResponse.builder()
            .token(TOKEN)
            .eventId(event.name())
            .caseDetails(caseDetails)
            .build();
    }

    private CardPaymentStatusResponse getCardPaymentStatusResponse() {
        return new CardPaymentStatusResponse()
            .setPaymentReference("1234")
            .setStatus(PaymentStatus.SUCCESS.name());
    }
}
