package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

class AllPartiesEmailGeneratorTest {

    String taskId = "someTaskId";

    @Mock
    private AppSolOneEmailDTOGenerator appSolOneEmailGenerator;

    @Mock
    private RespSolOneEmailDTOGenerator respSolOneEmailGenerator;

    @Mock
    private RespSolTwoEmailDTOGenerator respSolTwoEmailGenerator;

    @Mock
    private ClaimantEmailDTOGenerator claimantEmailDTOGenerator;

    @Mock
    private DefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @InjectMocks
    private AllPartiesEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appSolOneEmailGenerator = Mockito.mock(AppSolOneEmailDTOGenerator.class);
        respSolOneEmailGenerator = Mockito.mock(RespSolOneEmailDTOGenerator.class);
        respSolTwoEmailGenerator = Mockito.mock(RespSolTwoEmailDTOGenerator.class);
        claimantEmailDTOGenerator = Mockito.mock(ClaimantEmailDTOGenerator.class);
        defendantEmailDTOGenerator = Mockito.mock(DefendantEmailDTOGenerator.class);
        emailGenerator = new AllPartiesEmailGenerator(List.of(appSolOneEmailGenerator,
                                                      respSolOneEmailGenerator,
                                                      respSolTwoEmailGenerator,
                                                      claimantEmailDTOGenerator,
                                                      defendantEmailDTOGenerator));
    }

    @Test
    void shouldNotifyAllParties_whenTwoRespondentRepresentativesFlagIsSet() {
        CaseData caseData = CaseData.builder()
            .respondent2(Party.builder().build())
            .respondent2SameLegalRepresentative(NO)
            .build();
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);

        when(appSolOneEmailGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolEmail);
        when(respSolOneEmailGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolTwoEmail);
        when(appSolOneEmailGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);
        when(respSolOneEmailGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);
        when(respSolTwoEmailGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail, respSolTwoEmail);
        verify(appSolOneEmailGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailGenerator).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyOnlyAppSolAndRespSolOne_whenTwoRespondentRepresentativesFlagIsNotSet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);

        when(appSolOneEmailGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolEmail);
        when(respSolOneEmailGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);
        when(appSolOneEmailGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);
        when(respSolOneEmailGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail);
        verify(appSolOneEmailGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyOnlyClaimantAndDefendant_whenLipvLipCaseIsInvoked() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO claimantEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);

        when(claimantEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(claimantEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(defendantEmail);
        when(claimantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);
        when(defendantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(claimantEmail, defendantEmail);
        verify(claimantEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyOnlyClaimantAndRespSolOne_whenLipVLRCaseIsInvoked() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO claimantEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);

        when(claimantEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(claimantEmail);
        when(respSolOneEmailGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);
        when(claimantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);
        when(respSolOneEmailGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(claimantEmail, respSolOneEmail);
        verify(claimantEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyOnlyClaimantAndRespSolOne_whenLRVLipCaseIsInvoked() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);

        when(appSolOneEmailGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(defendantEmail);
        when(appSolOneEmailGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);
        when(defendantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, defendantEmail);
        verify(appSolOneEmailGenerator).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailGenerator, never()).buildEmailDTO(caseData, taskId);
    }
}
