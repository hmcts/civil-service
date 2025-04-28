package uk.gov.hmcts.reform.civil.notification.handlers.claimantdefendantagreedmediation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

public class ClaimantDefendantAgreedMediationPartiesEmailGeneratorTest {

    @Mock
    private ClaimantDefendantAgreedMediationAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;

    @Mock
    private ClaimantDefendantAgreedMediationRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @Mock
    private ClaimantDefendantAgreedMediationRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;

    @Mock
    private ClaimantDefendantAgreedMediationDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @InjectMocks
    private ClaimantDefendantAgreedMediationPartiesEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldNotifyAppSolOneAndRespSolOne_whenTwoRespondentRepresentativesFlagIsNotSet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);

        when(caseData.isRespondent1NotRepresented()).thenReturn(false);

        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);

        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
                 .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail);
        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyRespSolTwo_whenTwoRespondentRepresentativesFlagIsSet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);

        when(caseData.isRespondent1NotRepresented()).thenReturn(false);

        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolTwoEmail);

        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
                 .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail, respSolTwoEmail);
        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyDefendant_whenDefendantIsUnrepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);

        when(caseData.isRespondent1NotRepresented()).thenReturn(true);

        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(defendantEmail);

        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
                 .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, defendantEmail);
        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }
}
