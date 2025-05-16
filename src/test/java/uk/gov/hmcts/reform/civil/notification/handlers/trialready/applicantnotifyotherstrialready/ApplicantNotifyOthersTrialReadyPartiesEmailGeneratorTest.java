package uk.gov.hmcts.reform.civil.notification.handlers.trialready.applicantnotifyotherstrialready;

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
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class ApplicantNotifyOthersTrialReadyPartiesEmailGeneratorTest {

    @InjectMocks
    private ApplicantNotifyOthersTrialReadyPartiesEmailGenerator emailGenerator;

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

    @Test
    void shouldNotifyRespSolOneWhenRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);

        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(respSolOneEmail);

        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyRespSolTwoWhenRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);

        when(caseData.getTrialReadyRespondent1()).thenReturn(null);
        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolTwoEmail);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        multiPartyScenarioMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(respSolOneEmail, respSolTwoEmail);

        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyDefendantOneAndTwoWhenLip() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO defendant1Email = mock(EmailDTO.class);
        EmailDTO defendant2Email = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);

        when(caseData.isRespondent1LiP()).thenReturn(true);
        when(caseData.isRespondent2LiP()).thenReturn(true);
        when(caseData.getTrialReadyRespondent1()).thenReturn(null);
        when(caseData.getTrialReadyRespondent2()).thenReturn(null);
        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddRespondent2()).thenReturn(YES);
        when(caseData.getRespondent2Represented()).thenReturn(NO);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(defendant1Email);
        when(defendantTwoEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(defendant2Email);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        multiPartyScenarioMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(defendant1Email, defendant2Email);

        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyRespSolOneAndRespSolTwoWhenNotSpecClaim() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);

        when(caseData.getTrialReadyRespondent1()).thenReturn(null);
        when(caseData.getTrialReadyRespondent2()).thenReturn(null);
        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
        when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddRespondent2()).thenReturn(YES);
        when(caseData.getRespondent2Represented()).thenReturn(NO);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolTwoEmail);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        multiPartyScenarioMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(respSolOneEmail, respSolTwoEmail);

        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotNotifyPartiesWhenTrialReadyRespondentIsSet() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.getTrialReadyRespondent1()).thenReturn(YES);
        when(caseData.getTrialReadyRespondent2()).thenReturn(YES);

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
