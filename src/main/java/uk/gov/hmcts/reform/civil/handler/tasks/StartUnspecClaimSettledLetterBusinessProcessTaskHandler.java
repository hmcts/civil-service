package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis.DefendantLetterHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;

/**
 * Starts the unspec claim settled letter business process and adds the variable the BPMN
 * uses to decide whether the letter task runs, so the letter event is only recorded when a letter is sent.
 */
@Component
public class StartUnspecClaimSettledLetterBusinessProcessTaskHandler extends BaseExternalTaskHandler {

    public static final String CLAIM_SETTLED_LETTER_REQUIRED = "isClaimSettledLetterRequired";
    private final StartBusinessProcessTaskHandler startBusinessProcessTaskHandler;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;

    public StartUnspecClaimSettledLetterBusinessProcessTaskHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        StartBusinessProcessTaskHandler startBusinessProcessTaskHandler,
        CoreCaseDataService coreCaseDataService,
        CaseDetailsConverter caseDetailsConverter,
        ObjectMapper mapper
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.startBusinessProcessTaskHandler = startBusinessProcessTaskHandler;
        this.coreCaseDataService = coreCaseDataService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.mapper = mapper;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        ExternalTaskData externalTaskData = startBusinessProcessTaskHandler.handleTask(externalTask);

        String caseId = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class).getCaseId();
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(Long.valueOf(caseId)));
        boolean letterRequired = DefendantLetterHandler.isClaimSettledLetterRequired(caseData);
        log.info("Claim settled letter required: {} for caseId {} (respondent1Represented: {}, preStayState: {})",
                 letterRequired, caseId, caseData.getRespondent1Represented(), caseData.getPreStayState());
        externalTaskData.getVariables().putValue(CLAIM_SETTLED_LETTER_REQUIRED, letterRequired);

        return externalTaskData;
    }

    @Override
    public VariableMap getVariableMap(ExternalTaskData externalTaskData) {
        return externalTaskData.getVariables();
    }
}
