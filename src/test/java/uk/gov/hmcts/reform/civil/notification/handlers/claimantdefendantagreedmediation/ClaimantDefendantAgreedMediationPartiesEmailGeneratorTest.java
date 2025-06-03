package uk.gov.hmcts.reform.civil.notification.handlers.claimantdefendantagreedmediation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

public class ClaimantDefendantAgreedMediationPartiesEmailGeneratorTest {

    @Mock
    private ClaimantDefendantAgreedMediationAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;

    @Mock
    private ClaimantDefendantAgreedMediationRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @Mock
    private ClaimantDefendantAgreedMediationRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;

    @Mock
    private ClaimantDefendantAgreedMediationDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ClaimantDefendantAgreedMediationPartiesEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldNotifyAppSolOneAndRespSolOne_whenTwoRespondentRepresentativesFlagIsNotSet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);
        String taskId = "task id";

        when(caseData.isRespondent1NotRepresented()).thenReturn(false);

        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);

        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
                 .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(false);
        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm(caseData, false))
                .thenReturn(false);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        notificationUtilsMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail);
        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyRespSolTwoWhenCarmEnabled() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);
        String taskId = "task id";

        when(caseData.isRespondent1NotRepresented()).thenReturn(false);

        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolTwoEmail);

        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
                 .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm(caseData, true))
                .thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        notificationUtilsMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail, respSolTwoEmail);
        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyDefendant_whenDefendantIsUnrepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);
        String taskId = "task id";

        when(caseData.isRespondent1NotRepresented()).thenReturn(true);

        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(defendantEmail);

        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
                 .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(false);
        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm(caseData, false))
                .thenReturn(false);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        notificationUtilsMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, defendantEmail);
        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }
}
