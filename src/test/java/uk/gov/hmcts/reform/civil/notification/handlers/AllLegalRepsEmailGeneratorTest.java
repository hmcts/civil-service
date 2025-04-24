package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

class AllLegalRepsEmailGeneratorTest {

    @Mock
    private AppSolOneEmailDTOGenerator appSolOneEmailGenerator;

    @Mock
    private RespSolOneEmailDTOGenerator respSolOneEmailGenerator;

    @Mock
    private RespSolTwoEmailDTOGenerator respSolTwoEmailGenerator;

    @InjectMocks
    private AllLegalRepsEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appSolOneEmailGenerator = Mockito.mock(AppSolOneEmailDTOGenerator.class);
        respSolOneEmailGenerator = Mockito.mock(RespSolOneEmailDTOGenerator.class);
        respSolTwoEmailGenerator = Mockito.mock(RespSolTwoEmailDTOGenerator.class);
        emailGenerator = new AllLegalRepsEmailGenerator(appSolOneEmailGenerator,
                                                        respSolOneEmailGenerator,
                                                        respSolTwoEmailGenerator);
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

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail);
        verify(appSolOneEmailGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailGenerator, never()).buildEmailDTO(caseData);
    }
}

