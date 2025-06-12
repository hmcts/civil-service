package uk.gov.hmcts.reform.civil.notification.handlers.createsdo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateSDOPartiesEmailGeneratorTest {

    @InjectMocks
    private CreateSDOPartiesEmailGenerator emailGenerator;

    @Mock
    private CreateSDOAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;

    @Mock
    private CreateSDOClaimantEmailDTOGenerator claimantEmailDTOGenerator;

    @Mock
    private CreateSDORespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @Mock
    private CreateSDORespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;

    @Mock
    private CreateSDODefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @Mock
    private CreateSDODefendantTwoEmailDTOGenerator defendantTwoEmailDTOGenerator;

    @Test
    void shouldNotifyAppSolOneAndRespSolOneWhenRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        String taskId = "taskId";

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
    void shouldNotifyRespSolTwoWhenRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);
        String taskId = "taskId";

        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolOneEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolTwoEmail);
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolOneEmail, respSolOneEmail, respSolTwoEmail);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyClaimantAndDefendantsWhenLip() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO claimantEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);
        EmailDTO defendantTwoEmail = mock(EmailDTO.class);
        String taskId = "taskId";

        when(claimantEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(claimantEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(defendantEmail);
        when(defendantTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(defendantTwoEmail);
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.isApplicantLiP()).thenReturn(true);
        when(caseData.isRespondent1LiP()).thenReturn(true);
        when(caseData.isRespondent2LiP()).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(claimantEmail, defendantEmail, defendantTwoEmail);

        verify(claimantEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(defendantTwoEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }
}
