package uk.gov.hmcts.reform.civil.notification.handlers.trialready.respondentonenotifyotherstrialready;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyAppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyDefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyDefendantTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyRespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyRespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class RespondentOneNotifyOthersTrialReadyPartiesEmailGeneratorTest {

    @InjectMocks
    private RespondentOneNotifyOthersTrialReadyPartiesEmailGenerator emailGenerator;

    @Mock
    private TrialReadyAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;

    @Mock
    private TrialReadyClaimantEmailDTOGenerator claimantEmailDTOGenerator;

    @Mock
    private TrialReadyRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @Mock
    private TrialReadyRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;

    @Mock
    private TrialReadyDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @Mock
    private TrialReadyDefendantTwoEmailDTOGenerator defendantTwoEmailDTOGenerator;

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @Test
    void shouldNotifyAppSolOneWhenRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolOneEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);

        when(stateFlow.isFlagSet(any())).thenReturn(false);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolOneEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolOneEmail);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shuldNotifyRespSolTwoWhenRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);

        when(stateFlow.isFlagSet(any())).thenReturn(false);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolOneEmail);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolTwoEmail);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        multiPartyScenarioMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolOneEmail, respSolTwoEmail);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyClaimantAndDefendantTwoWhenLip() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO claimantEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);

        when(stateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(caseData.isRespondent2LiP()).thenReturn(true);
        when(caseData.isApplicantNotRepresented()).thenReturn(true);
        when(claimantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(claimantEmail);
        when(defendantTwoEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(defendantEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(claimantEmail, defendantEmail);

        verify(claimantEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyAppSolOneAndRespSolTwoWhenNotSpecClaim() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);

        when(stateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolOneEmail);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolTwoEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolOneEmail, respSolTwoEmail);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotNotifyPartiesWhenTrialReadyRespondentIsSet() {
        CaseData caseData = mock(CaseData.class);
        StateFlow stateFlow = mock(StateFlow.class);

        when(caseData.getTrialReadyApplicant()).thenReturn(YES);
        when(caseData.getTrialReadyRespondent2()).thenReturn(YES);
        when(stateFlow.isFlagSet(any())).thenReturn(false);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        multiPartyScenarioMockedStatic.close();

        assertThat(partiesToNotify).isEmpty();

        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }
}
