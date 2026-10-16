package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_STATE;
import static uk.gov.hmcts.reform.civil.handler.tasks.StartUnspecClaimSettledLetterBusinessProcessTaskHandler.CLAIM_SETTLED_LETTER_REQUIRED;

@ExtendWith(MockitoExtension.class)
class StartUnspecClaimSettledLetterBusinessProcessTaskHandlerTest {

    private static final String CASE_ID = "1";

    @Mock
    private ExternalTask externalTask;
    @Mock
    private StartBusinessProcessTaskHandler startBusinessProcessTaskHandler;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private ExternalTaskCompletionService externalTaskCompletionService;
    @Mock
    private EventProperties eventProperties;
    @Spy
    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(mapper);

    @InjectMocks
    private StartUnspecClaimSettledLetterBusinessProcessTaskHandler handler;

    @ParameterizedTest
    @CsvSource({
        "NO, AWAITING_CASE_DETAILS_NOTIFICATION, true",
        "NO, CASE_ISSUED, false",
        "NO, , false",
        "YES, AWAITING_CASE_DETAILS_NOTIFICATION, false"
    })
    void shouldStartBusinessProcessAndSetClaimSettledLetterRequired(YesOrNo respondent1Represented,
                                                                    String preStayState,
                                                                    boolean expectedLetterRequired) {
        VariableMap startVariables = Variables.createVariables().putValue(FLOW_STATE, "MAIN.DRAFT");
        when(startBusinessProcessTaskHandler.handleTask(externalTask))
            .thenReturn(new ExternalTaskData().setVariables(startVariables));
        when(externalTask.getAllVariables()).thenReturn(Map.of("caseId", CASE_ID));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
        caseData.setRespondent1Represented(respondent1Represented);
        caseData.setPreStayState(preStayState);
        when(coreCaseDataService.getCase(Long.valueOf(CASE_ID)))
            .thenReturn(CaseDetailsBuilder.builder().data(caseData).build());

        ExternalTaskData result = handler.handleTask(externalTask);

        verify(startBusinessProcessTaskHandler).handleTask(externalTask);
        assertThat(result.getVariables())
            .containsEntry(FLOW_STATE, "MAIN.DRAFT")
            .containsEntry(CLAIM_SETTLED_LETTER_REQUIRED, expectedLetterRequired);
    }

    @Test
    void shouldNotReadCase_whenStartBusinessProcessFails() {
        when(startBusinessProcessTaskHandler.handleTask(externalTask))
            .thenThrow(new BpmnError("ABORT"));

        assertThatThrownBy(() -> handler.handleTask(externalTask))
            .isInstanceOf(BpmnError.class);
        verifyNoInteractions(coreCaseDataService);
    }
}
