package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AllLegalRepsEmailGeneratorTest {

    String taskId = "someTaskId";

    @Mock
    private AppSolOneEmailDTOGenerator appSolOneEmailGenerator;

    @Mock
    private RespSolOneEmailDTOGenerator respSolOneEmailGenerator;

    @Mock
    private RespSolTwoEmailDTOGenerator respSolTwoEmailGenerator;

    private MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic;

    @InjectMocks
    private AllLegalRepsEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
    }

    @AfterEach
    void tearDown() {
        multiPartyScenarioMockedStatic.close();
    }

    @Test
    void shouldNotifyAllParties_whenTwoRespondentRepresentativesFlagIsSet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(any()))
            .thenReturn(Boolean.TRUE);

        when(appSolOneEmailGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolEmail);
        when(respSolOneEmailGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolTwoEmail);

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

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(any()))
            .thenReturn(Boolean.FALSE);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail);
        verify(appSolOneEmailGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailGenerator, never()).buildEmailDTO(caseData, taskId);
    }
}
