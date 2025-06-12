package uk.gov.hmcts.reform.civil.notification.handlers.trialready.respondenttwonotifyotherstrialready;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyAppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyDefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyDefendantTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyRespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyRespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class RespondentTwoNotifyOthersTrialReadyPartiesEmailGeneratorTest {

    @InjectMocks
    private RespondentTwoNotifyOthersTrialReadyPartiesEmailGenerator emailGenerator;

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
    void shouldNotifyAppSolOneAndRespSolOneWhenRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        String taskId = "task id";

        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolOneEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolOneEmail, respSolOneEmail);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyClaimantAndDefendantWhenLip() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO claimantEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);
        String taskId = "task id";

        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(claimantEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(claimantEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(defendantEmail);
        when(caseData.isApplicantNotRepresented()).thenReturn(true);
        when(caseData.isRespondent1LiP()).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(claimantEmail, defendantEmail);

        verify(claimantEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyAppSolOneAndRespSolOneWhenNotSpec() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        String taskId = "task id";

        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolOneEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolOneEmail, respSolOneEmail);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotNotifyPartiesWhenTrialReadyRespondentIsSet() {
        CaseData caseData = mock(CaseData.class);
        String taskId = "task id";

        when(caseData.getTrialReadyApplicant()).thenReturn(YES);
        when(caseData.getTrialReadyRespondent1()).thenReturn(YES);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).isEmpty();

        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }
}
