package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.ga.handler.tasks.CheckStayOrderDeadlineEndTaskHandler;
import uk.gov.hmcts.reform.civil.ga.handler.tasks.CheckUnlessOrderDeadlineEndTaskHandler;
import uk.gov.hmcts.reform.civil.ga.handler.tasks.GAJudgeRevisitTaskHandler;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.handler.event.BundleCreationTriggerEventHandler;
import uk.gov.hmcts.reform.civil.handler.event.HearingFeePaidEventHandler;
import uk.gov.hmcts.reform.civil.handler.event.HearingFeeUnpaidEventHandler;
import uk.gov.hmcts.reform.civil.handler.event.TrialReadyNotificationEventHandler;
import uk.gov.hmcts.reform.civil.handler.tasks.ClaimDetailsNotificationDeadlineHandler;
import uk.gov.hmcts.reform.civil.handler.tasks.ClaimDismissedHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.scheduler.common.TestingSupportSchedulerRegistry;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.hearings.HearingValuesService;
import uk.gov.hmcts.reform.civil.service.judgments.CjesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpec;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForUnspec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TestingSupportControllerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CamundaRestEngineClient camundaRestEngineClient;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private GaStateFlowEngine gaStateFlowEngine;
    @Mock
    private EventHistoryMapper eventHistoryMapper;
    @Mock
    private RoboticsDataMapperForUnspec roboticsDataMapper;
    @Mock
    private RoboticsDataMapperForSpec roboticsSpecDataMapper;
    @Mock
    private CjesMapper cjesMapper;
    @Mock
    private HearingValuesService hearingValuesService;
    @Mock
    private SystemUpdateUserConfiguration systemUserConfig;
    @Mock
    private UserService userService;
    @Mock
    private ClaimDismissedHandler claimDismissedHandler;
    @Mock
    private ClaimDetailsNotificationDeadlineHandler claimDetailsNotificationDeadlineHandler;
    @Mock
    private HearingFeePaidEventHandler hearingFeePaidHandler;
    @Mock
    private HearingFeeUnpaidEventHandler hearingFeeUnpaidHandler;
    @Mock
    private TrialReadyNotificationEventHandler trialReadyNotificationHandler;
    @Mock
    private BundleCreationTriggerEventHandler bundleCreationTriggerEventHandler;
    @Mock
    private CheckStayOrderDeadlineEndTaskHandler checkStayOrderDeadlineEndTaskHandler;
    @Mock
    private CheckUnlessOrderDeadlineEndTaskHandler checkUnlessOrderDeadlineEndTaskHandler;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private CoreCaseUserService coreCaseUserService;
    @Mock
    private GAJudgeRevisitTaskHandler gaJudgeRevisitTaskHandler;
    @Mock
    private TestingSupportSchedulerRegistry civilSchedulerRepository;

    @InjectMocks
    private TestingSupportController controller;

    @Test
    void shouldTriggerNotifyClaimDetailsScheduler() {
        ResponseEntity<String> response = controller.getNotifyClaimDetailsScheduler();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("success");
        verify(claimDetailsNotificationDeadlineHandler).handleTask(any(ExternalTask.class));
    }

    @Test
    void shouldReturnFailedWhenNotifyClaimDetailsSchedulerThrowsException() {
        doThrow(new RuntimeException("Scheduler failed"))
            .when(claimDetailsNotificationDeadlineHandler).handleTask(any(ExternalTask.class));

        ResponseEntity<String> response = controller.getNotifyClaimDetailsScheduler();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("failed");
        verify(claimDetailsNotificationDeadlineHandler).handleTask(any(ExternalTask.class));
    }
}
