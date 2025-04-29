package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

class AllPartiesEmailGeneratorTest {

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
        emailGenerator = new AllPartiesEmailGenerator(appSolOneEmailGenerator,
                                                      respSolOneEmailGenerator,
                                                      respSolTwoEmailGenerator,
                                                      claimantEmailDTOGenerator,
                                                      defendantEmailDTOGenerator);
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

        when(appSolOneEmailGenerator.buildEmailDTO(caseData)).thenReturn(appSolEmail);
        when(respSolOneEmailGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailGenerator.buildEmailDTO(caseData)).thenReturn(respSolTwoEmail);
        when(appSolOneEmailGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);
        when(respSolOneEmailGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);
        when(respSolTwoEmailGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail, respSolTwoEmail);
        verify(appSolOneEmailGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailGenerator).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyOnlyAppSolAndRespSolOne_whenTwoRespondentRepresentativesFlagIsNotSet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);

        when(appSolOneEmailGenerator.buildEmailDTO(caseData)).thenReturn(appSolEmail);
        when(respSolOneEmailGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);
        when(appSolOneEmailGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);
        when(respSolOneEmailGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail);
        verify(appSolOneEmailGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyOnlyClaimantAndDefendant_whenLipvLipCaseIsInvoked() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO claimantEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);

        when(claimantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(claimantEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(defendantEmail);
        when(claimantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);
        when(defendantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(claimantEmail, defendantEmail);
        verify(claimantEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData);
        verify(appSolOneEmailGenerator, never()).buildEmailDTO(caseData);
        verify(respSolOneEmailGenerator, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyOnlyClaimantAndRespSolOne_whenLipVLRCaseIsInvoked() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO claimantEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);

        when(claimantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(claimantEmail);
        when(respSolOneEmailGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);
        when(claimantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);
        when(respSolOneEmailGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(claimantEmail, respSolOneEmail);
        verify(claimantEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailGenerator, never()).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(appSolOneEmailGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyOnlyClaimantAndRespSolOne_whenLRVLipCaseIsInvoked() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);

        when(appSolOneEmailGenerator.buildEmailDTO(caseData)).thenReturn(appSolEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(defendantEmail);
        when(appSolOneEmailGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);
        when(defendantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(Boolean.TRUE);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, defendantEmail);
        verify(appSolOneEmailGenerator).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolOneEmailGenerator, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailGenerator, never()).buildEmailDTO(caseData);
    }
}
