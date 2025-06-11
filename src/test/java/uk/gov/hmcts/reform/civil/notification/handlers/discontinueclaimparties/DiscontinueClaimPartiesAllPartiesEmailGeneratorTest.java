package uk.gov.hmcts.reform.civil.notification.handlers.discontinueclaimparties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

class DiscontinueClaimPartiesAllPartiesEmailGeneratorTest {

    @Mock
    private DiscontinueClaimPartiesAppSolOneEmailDTOGenerator discontinueClaimPartiesAppSolOneEmailDTOGenerator;

    @Mock
    private DiscontinueClaimPartiesRespSolOneEmailDTOGenerator discontinueClaimPartiesRespSolOneEmailDTOGenerator;

    @Mock
    private DiscontinueClaimPartiesRespSolTwoEmailDTOGenerator discontinueClaimPartiesRespSolTwoEmailDTOGenerator;

    @InjectMocks
    private DiscontinueClaimPartiesAllPartiesEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldExtendAllPartiesEmailGeneratorWithCorrectDependencies() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }

    @Test
    void shouldNotifyDefendant1Always() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO defendant1Email = mock(EmailDTO.class);

        when(discontinueClaimPartiesRespSolOneEmailDTOGenerator.buildEmailDTO(caseData, "taskId"))
                .thenReturn(defendant1Email);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData, "taskId");

        assertThat(result).contains(defendant1Email);
        verify(discontinueClaimPartiesRespSolOneEmailDTOGenerator).buildEmailDTO(caseData, "taskId");
    }

    @Test
    void shouldNotifyClaimantIfDefendant1IsLegallyRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO defendant1Email = mock(EmailDTO.class);
        EmailDTO claimantEmail = mock(EmailDTO.class);

        when(caseData.getRespondent1Represented()).thenReturn(YesOrNo.YES);
        when(discontinueClaimPartiesRespSolOneEmailDTOGenerator.buildEmailDTO(caseData, "taskId"))
                .thenReturn(defendant1Email);
        when(discontinueClaimPartiesAppSolOneEmailDTOGenerator.buildEmailDTO(caseData, "taskId"))
                .thenReturn(claimantEmail);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData, "taskId");

        assertThat(result).contains(defendant1Email, claimantEmail);
        verify(discontinueClaimPartiesRespSolOneEmailDTOGenerator).buildEmailDTO(caseData, "taskId");
        verify(discontinueClaimPartiesAppSolOneEmailDTOGenerator).buildEmailDTO(caseData, "taskId");
    }

    @Test
    void shouldNotifyDefendant2IfLegallyRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO defendant1Email = mock(EmailDTO.class);
        EmailDTO defendant2Email = mock(EmailDTO.class);
        Party respondent2 = mock(Party.class);

        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(caseData.getRespondent2Represented()).thenReturn(YesOrNo.YES);
        when(discontinueClaimPartiesRespSolOneEmailDTOGenerator.buildEmailDTO(caseData, "taskId"))
                .thenReturn(defendant1Email);
        when(discontinueClaimPartiesRespSolTwoEmailDTOGenerator.buildEmailDTO(caseData, "taskId"))
                .thenReturn(defendant2Email);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData, "taskId");

        assertThat(result).contains(defendant1Email, defendant2Email);
        verify(discontinueClaimPartiesRespSolOneEmailDTOGenerator).buildEmailDTO(caseData, "taskId");
        verify(discontinueClaimPartiesRespSolTwoEmailDTOGenerator).buildEmailDTO(caseData, "taskId");
    }

    @Test
    void shouldNotNotifyClaimantOrDefendant2IfConditionsAreNotMet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO defendant1Email = mock(EmailDTO.class);

        when(caseData.getRespondent1Represented()).thenReturn(YesOrNo.NO);
        when(caseData.getRespondent2()).thenReturn(null);
        when(discontinueClaimPartiesRespSolOneEmailDTOGenerator.buildEmailDTO(caseData, "taskId"))
                .thenReturn(defendant1Email);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData, "taskId");

        assertThat(result).containsOnly(defendant1Email);
        verify(discontinueClaimPartiesRespSolOneEmailDTOGenerator).buildEmailDTO(caseData, "taskId");
        verifyNoInteractions(discontinueClaimPartiesAppSolOneEmailDTOGenerator);
        verifyNoInteractions(discontinueClaimPartiesRespSolTwoEmailDTOGenerator);
    }

    @Test
    void shouldNotNotifyDefendant2IfRespondent2IsNullOrNotRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO defendant1Email = mock(EmailDTO.class);

        // Respondent 2 is null
        when(caseData.getRespondent2()).thenReturn(null);
        when(discontinueClaimPartiesRespSolOneEmailDTOGenerator.buildEmailDTO(caseData, "taskId"))
                .thenReturn(defendant1Email);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData, "taskId");

        assertThat(result).containsOnly(defendant1Email);
        verify(discontinueClaimPartiesRespSolOneEmailDTOGenerator).buildEmailDTO(caseData, "taskId");
        verifyNoInteractions(discontinueClaimPartiesRespSolTwoEmailDTOGenerator);

        // Respondent 2 exists but is not represented
        Party respondent2 = mock(Party.class);
        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(caseData.getRespondent2Represented()).thenReturn(YesOrNo.NO);

        result = emailGenerator.getPartiesToNotify(caseData, "taskId");

        assertThat(result).containsOnly(defendant1Email);
        verify(discontinueClaimPartiesRespSolOneEmailDTOGenerator, times(2)).buildEmailDTO(caseData, "taskId");
        verifyNoInteractions(discontinueClaimPartiesRespSolTwoEmailDTOGenerator);
    }
}
