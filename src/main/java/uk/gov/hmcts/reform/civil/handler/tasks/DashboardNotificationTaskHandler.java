package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.exceptions.InvalidCaseDataException;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationTransactionalService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.utils.MarkPaidInFullUtil.checkMarkPaidInFull;

@Component
public class DashboardNotificationTaskHandler extends BaseExternalTaskHandler {

    public static final String CIVIL_TOPIC = "dashboardNotifications";
    public static final String GA_TOPIC = "gaDashboardNotifications";

    private final CoreCaseDataService coreCaseDataService;
    private final GaCoreCaseDataService gaCoreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;
    private final IStateFlowEngine stateFlowEngine;
    private final GaStateFlowEngine gaStateFlowEngine;
    private final DashboardNotificationTransactionalService dashboardNotificationTransactionalService;
    private final SystemUpdateUserConfiguration userConfig;
    private final UserService userService;

    protected DashboardNotificationTaskHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        CoreCaseDataService coreCaseDataService,
        GaCoreCaseDataService gaCoreCaseDataService,
        CaseDetailsConverter caseDetailsConverter,
        ObjectMapper mapper,
        IStateFlowEngine stateFlowEngine,
        GaStateFlowEngine gaStateFlowEngine,
        DashboardNotificationTransactionalService dashboardNotificationTransactionalService,
        SystemUpdateUserConfiguration userConfig,
        UserService userService
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.coreCaseDataService = coreCaseDataService;
        this.gaCoreCaseDataService = gaCoreCaseDataService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.mapper = mapper;
        this.stateFlowEngine = stateFlowEngine;
        this.gaStateFlowEngine = gaStateFlowEngine;
        this.dashboardNotificationTransactionalService = dashboardNotificationTransactionalService;
        this.userConfig = userConfig;
        this.userService = userService;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);
        String caseId = ofNullable(variables.getCaseId())
            .orElseThrow(() -> new InvalidCaseDataException("The caseId was not provided"));

        return switch (externalTask.getTopicName()) {
            case CIVIL_TOPIC -> new ExternalTaskData().setCaseData(processCivilDashboardNotification(externalTask, caseId));
            case GA_TOPIC -> new ExternalTaskData().setParentCaseData(processGaDashboardNotification(externalTask, caseId));
            default -> throw new InvalidCaseDataException("Unsupported dashboard notification topic");
        };
    }

    private CaseData processCivilDashboardNotification(ExternalTask externalTask, String caseId) {
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(Long.valueOf(caseId)));
        CaseData dashboardCaseData = caseData.toBuilder()
            .businessProcess(dashboardBusinessProcess(caseData.getBusinessProcess(), externalTask))
            .build();

        log.info("Dispatch dashboard notifications for caseId {} activityId {}", caseId, externalTask.getActivityId());
        dashboardNotificationTransactionalService.dispatch(
            externalTask.getActivityId(),
            DashboardTaskContext.civil(dashboardCaseData, systemUpdateUserToken())
        );
        return dashboardCaseData;
    }

    private GeneralApplicationCaseData processGaDashboardNotification(ExternalTask externalTask, String caseId) {
        GeneralApplicationCaseData caseData =
            caseDetailsConverter.toGeneralApplicationCaseData(gaCoreCaseDataService.getCase(Long.valueOf(caseId)));
        caseData.businessProcess(dashboardBusinessProcess(caseData.getBusinessProcess(), externalTask));

        log.info("Dispatch GA dashboard notifications for caseId {} activityId {}", caseId, externalTask.getActivityId());
        dashboardNotificationTransactionalService.dispatch(
            externalTask.getActivityId(),
            DashboardTaskContext.generalApplication(caseData, systemUpdateUserToken())
        );
        return caseData;
    }

    private String systemUpdateUserToken() {
        return userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }

    private BusinessProcess dashboardBusinessProcess(BusinessProcess currentBusinessProcess, ExternalTask externalTask) {
        BusinessProcess businessProcess = ofNullable(currentBusinessProcess)
            .map(BusinessProcess::copy)
            .orElseGet(BusinessProcess::new);
        businessProcess.updateActivityId(externalTask.getActivityId());

        if (!businessProcess.hasSameProcessInstanceId(externalTask.getProcessInstanceId())) {
            businessProcess.updateProcessInstanceId(externalTask.getProcessInstanceId());
        }

        return businessProcess;
    }

    @Override
    public VariableMap getVariableMap(ExternalTaskData externalTaskData) {
        VariableMap variables = Variables.createVariables();
        externalTaskData.caseData().ifPresent(caseData -> {
            var stateFlow = stateFlowEngine.getStateFlow(caseData);
            variables.putValue(FLOW_STATE, stateFlow.getState().getName());
            variables.putValue(FLOW_FLAGS, stateFlow.getFlags());
            variables.putValue("isJudgmentMarkedPaidInFull", checkMarkPaidInFull(caseData));
        });
        externalTaskData.parentCaseData().ifPresent(caseData -> {
            var stateFlow = gaStateFlowEngine.evaluate(caseData);
            variables.putValue(FLOW_STATE, stateFlow.getState().getName());
            variables.putValue(FLOW_FLAGS, stateFlow.getFlags());
            if (caseData.getGeneralAppParentCaseLink() != null) {
                variables.putValue("generalAppParentCaseLink", caseData.getGeneralAppParentCaseLink().getCaseReference());
            }
        });
        return variables;
    }
}
