package uk.gov.hmcts.reform.civil.handler.callback.camunda.cosc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentPaidInFullOnlineMapper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SUMMARY_JUDGEMENT;

@ExtendWith(MockitoExtension.class)
class CheckAndMarkDefendantPaidInFullCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private CheckAndMarkDefendantPaidInFullCallbackHandler handler;
    @Mock
    private JudgmentPaidInFullOnlineMapper paidInFullJudgmentOnlineMapper;
    @Mock
    private RuntimeService runtimeService;

    private static final String PROCESS_INSTANCE_ID = "process-instance-id";
    private static final String SEND_DETAILS_CJES = "sendDetailsToCJES";
    private static ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        handler = new CheckAndMarkDefendantPaidInFullCallbackHandler(
            paidInFullJudgmentOnlineMapper,
            runtimeService,
            objectMapper
        );
    }

    @Test
    void shouldSetSendToCjesProcessVariableToFalse_whenDefendantPaidInFull() {

        LocalDate markedPaymentDate = LocalDate.of(2024, 9, 20);
        UUID genAppId = UUID.randomUUID();
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .generalApplications(
                List.of(
                    Element.<GeneralApplication>builder()
                        .id(genAppId)
                        .value(GeneralApplication.builder()
                                   .generalAppType(GAApplicationType.builder().types(List.of(CONFIRM_CCJ_DEBT_PAID)).build())
                                   .certOfSC(CertOfSC.builder()
                                                 .defendantFinalPaymentDate(markedPaymentDate)
                                                 .build())
                                   .build())
                        .build()
                )
            )
            .activeJudgment(JudgmentDetails.builder()
                                .fullyPaymentMadeDate(LocalDate.of(2024, 9, 20))
                                .issueDate(LocalDate.of(2024, 9, 9))
                                .totalAmount("900000")
                                .orderedAmount("900000")
                                .build())
            .build();

        var params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        handler.handle(params);

        verify(runtimeService, times(1)).setVariable(PROCESS_INSTANCE_ID, SEND_DETAILS_CJES, false);
        verifyNoInteractions(paidInFullJudgmentOnlineMapper);
    }

    @Test
    void shouldUpdateJudgmentAndSetSendToCjesProcessVariableToTrue_whenJudgmentPaidDateProvided() {
        LocalDate markedPaymentDate = LocalDate.of(2024, 9, 20);
        UUID genAppId = UUID.randomUUID();
        JudgmentDetails activeJudgementWithoutPayment = JudgmentDetails.builder()
            .issueDate(LocalDate.of(2024, 9, 9))
            .totalAmount("900000")
            .orderedAmount("900000")
            .build();
        var proofOfDebtApp = Element.<GeneralApplication>builder()
            .id(genAppId)
            .value(GeneralApplication.builder()
                       .generalAppType(GAApplicationType.builder().types(List.of(CONFIRM_CCJ_DEBT_PAID)).build())
                       .certOfSC(CertOfSC.builder()
                                     .defendantFinalPaymentDate(markedPaymentDate)
                                     .build())
                       .build())
            .build();

        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .generalApplications(List.of(proofOfDebtApp))
            .activeJudgment(activeJudgementWithoutPayment)
            .build();

        JudgmentDetails expected = caseData.getActiveJudgment().toBuilder()
            .fullyPaymentMadeDate(markedPaymentDate)
            .build();

        when(paidInFullJudgmentOnlineMapper.addUpdateActiveJudgment(caseData, markedPaymentDate)).thenReturn(expected);

        var params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        var result = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = objectMapper.convertValue(result.getData(), CaseData.class);

        assertEquals(expected, updatedData.getActiveJudgment());
        verify(runtimeService, times(1)).setVariable(PROCESS_INSTANCE_ID, SEND_DETAILS_CJES, true);
    }

    @Test
    void shouldThrowException_whenGeneralApplicationNotFound() {
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .generalApplications(List.of())
            .build();

        var params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> handler.handle(params)
        );

        assertEquals("Cosc was not found.", exception.getMessage());
    }

    @Test
    void shouldThrowException_whenNotProofOfDebtGeneralApplication() {
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .generalAppType(GAApplicationType.builder().types(List.of(SUMMARY_JUDGEMENT)).build())
            .build();

        var params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> handler.handle(params)
        );

        assertEquals("Cosc was not found.", exception.getMessage());
    }
}
