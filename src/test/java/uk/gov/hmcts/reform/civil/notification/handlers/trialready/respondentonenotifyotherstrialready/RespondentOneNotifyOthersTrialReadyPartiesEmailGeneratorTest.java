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

    @Test
    void shouldNotifyAppSolOneWhenRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolOneEmail = mock(EmailDTO.class);
        String taskId = "task id";

        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolOneEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolOneEmail);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyRespSolTwoWhenRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);
        String taskId = "task id";

        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolOneEmail);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolTwoEmail);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        multiPartyScenarioMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolOneEmail, respSolTwoEmail);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyClaimantAndDefendantTwoWhenLip() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO claimantEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);
        String taskId = "task id";

        when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddRespondent2()).thenReturn(YES);
        when(caseData.getRespondent2Represented()).thenReturn(NO);
        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(caseData.isRespondent2LiP()).thenReturn(true);
        when(caseData.isApplicantNotRepresented()).thenReturn(true);
        when(claimantEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(claimantEmail);
        when(defendantTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(defendantEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(claimantEmail, defendantEmail);

        verify(claimantEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyAppSolOneAndRespSolTwoWhenNotSpecClaim() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);
        String taskId = "task id";

        when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddRespondent2()).thenReturn(YES);
        when(caseData.getRespondent2Represented()).thenReturn(NO);
        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolOneEmail);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolTwoEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolOneEmail, respSolTwoEmail);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotNotifyPartiesWhenTrialReadyRespondentIsSet() {
        CaseData caseData = mock(CaseData.class);
        String taskId = "task id";

        when(caseData.getTrialReadyApplicant()).thenReturn(YES);
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
