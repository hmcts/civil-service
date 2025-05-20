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

        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolOneEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolOneEmail, respSolOneEmail);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyRespSolTwoWhenRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);

        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolOneEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolTwoEmail);
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolOneEmail, respSolOneEmail, respSolTwoEmail);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyClaimantAndDefendantsWhenLip() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO claimantEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);
        EmailDTO defendantTwoEmail = mock(EmailDTO.class);

        when(claimantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(claimantEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(defendantEmail);
        when(defendantTwoEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(defendantTwoEmail);
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.isApplicantLiP()).thenReturn(true);
        when(caseData.isRespondent1LiP()).thenReturn(true);
        when(caseData.isRespondent2LiP()).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(claimantEmail, defendantEmail, defendantTwoEmail);

        verify(claimantEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator).buildEmailDTO(caseData);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }
}
