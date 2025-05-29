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
        String taskId = "task id";

        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(respSolOneEmail);

        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyRespSolTwoWhenRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);
        String taskId = "task id";

        when(caseData.getTrialReadyRespondent1()).thenReturn(null);
        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolTwoEmail);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        multiPartyScenarioMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(respSolOneEmail, respSolTwoEmail);

        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyDefendantOneAndTwoWhenLip() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO defendant1Email = mock(EmailDTO.class);
        EmailDTO defendant2Email = mock(EmailDTO.class);
        String taskId = "task id";

        when(caseData.isRespondent1LiP()).thenReturn(true);
        when(caseData.isRespondent2LiP()).thenReturn(true);
        when(caseData.getTrialReadyRespondent1()).thenReturn(null);
        when(caseData.getTrialReadyRespondent2()).thenReturn(null);
        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddRespondent2()).thenReturn(YES);
        when(caseData.getRespondent2Represented()).thenReturn(NO);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(defendant1Email);
        when(defendantTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(defendant2Email);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        multiPartyScenarioMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(defendant1Email, defendant2Email);

        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyRespSolOneAndRespSolTwoWhenNotSpecClaim() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);
        String taskId = "task id";

        when(caseData.getTrialReadyRespondent1()).thenReturn(null);
        when(caseData.getTrialReadyRespondent2()).thenReturn(null);
        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
        when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddRespondent2()).thenReturn(YES);
        when(caseData.getRespondent2Represented()).thenReturn(NO);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolTwoEmail);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        multiPartyScenarioMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(respSolOneEmail, respSolTwoEmail);

        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotNotifyPartiesWhenTrialReadyRespondentIsSet() {
        CaseData caseData = mock(CaseData.class);
        String taskId = "task id";

        when(caseData.getTrialReadyRespondent1()).thenReturn(YES);
        when(caseData.getTrialReadyRespondent2()).thenReturn(YES);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        multiPartyScenarioMockedStatic.close();

        assertThat(partiesToNotify).isEmpty();

        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }
}
