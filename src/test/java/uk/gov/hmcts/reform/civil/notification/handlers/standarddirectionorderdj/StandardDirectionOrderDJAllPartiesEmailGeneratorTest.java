package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class StandardDirectionOrderDJAllPartiesEmailGeneratorTest {

    @Mock
    private StandardDirectionOrderDJAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;

    @Mock
    private StandardDirectionOrderDJRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @Mock
    private StandardDirectionOrderDJRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;

    private static final String TASK_ID = "taskId";

    @InjectMocks
    private StandardDirectionOrderDJAllPartiesEmailGenerator generator;

    @Test
    void shouldNotifyAllParties_whenAllShouldBeNotified() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO applicantEmail = mock(EmailDTO.class);
        EmailDTO respondent1Email = mock(EmailDTO.class);
        EmailDTO respondent2Email = mock(EmailDTO.class);

        when(appSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(respSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(respSolTwoEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);

        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, TASK_ID)).thenReturn(applicantEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, TASK_ID)).thenReturn(respondent1Email);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData, TASK_ID)).thenReturn(respondent2Email);

        Set<EmailDTO> result = generator.getPartiesToNotify(caseData, TASK_ID);

        assertThat(result).containsExactlyInAnyOrder(applicantEmail, respondent1Email, respondent2Email);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData, TASK_ID);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData, TASK_ID);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData, TASK_ID);
    }

    @Test
    void shouldNotifyOnlyClaimant_whenOnlyClaimantShouldBeNotified() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO applicantEmail = mock(EmailDTO.class);

        when(appSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(respSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(respSolTwoEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);

        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, TASK_ID)).thenReturn(applicantEmail);

        Set<EmailDTO> result = generator.getPartiesToNotify(caseData, TASK_ID);

        assertThat(result).containsExactly(applicantEmail);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData, TASK_ID);
        verify(respSolOneEmailDTOGenerator).getShouldNotify(caseData);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, TASK_ID);
        verify(respSolTwoEmailDTOGenerator).getShouldNotify(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, TASK_ID);
    }

    @Test
    void shouldNotifyOnlyDefendant1_whenOnlyDefendant1ShouldBeNotified() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO respondent1Email = mock(EmailDTO.class);

        when(appSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(respSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(respSolTwoEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);

        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, TASK_ID)).thenReturn(respondent1Email);

        Set<EmailDTO> result = generator.getPartiesToNotify(caseData, TASK_ID);

        assertThat(result).containsExactly(respondent1Email);

        verify(appSolOneEmailDTOGenerator).getShouldNotify(caseData);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, TASK_ID);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData, TASK_ID);
        verify(respSolTwoEmailDTOGenerator).getShouldNotify(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, TASK_ID);
    }

    @Test
    void shouldNotifyNoOne_whenNoOneShouldBeNotified() {
        CaseData caseData = mock(CaseData.class);

        when(appSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(respSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(respSolTwoEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);

        Set<EmailDTO> result = generator.getPartiesToNotify(caseData, TASK_ID);

        assertThat(result).isEmpty();

        verify(appSolOneEmailDTOGenerator).getShouldNotify(caseData);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, TASK_ID);
        verify(respSolOneEmailDTOGenerator).getShouldNotify(caseData);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, TASK_ID);
        verify(respSolTwoEmailDTOGenerator).getShouldNotify(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, TASK_ID);
    }
}
