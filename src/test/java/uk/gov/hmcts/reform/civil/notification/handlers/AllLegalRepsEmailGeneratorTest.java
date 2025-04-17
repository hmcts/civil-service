package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

class AllLegalRepsEmailGeneratorTest {

    @Mock
    private AppSolOneEmailDTOGenerator appSolOneEmailGenerator;

    @Mock
    private RespSolOneEmailDTOGenerator respSolOneEmailGenerator;

    @Mock
    private RespSolTwoEmailDTOGenerator respSolTwoEmailGenerator;

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @InjectMocks
    private AllLegalRepsEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldNotifyAllParties_whenTwoRespondentRepresentativesFlagIsSet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);

        when(appSolOneEmailGenerator.buildEmailDTO(caseData)).thenReturn(appSolEmail);
        when(respSolOneEmailGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailGenerator.buildEmailDTO(caseData)).thenReturn(respSolTwoEmail);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
            .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);

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
        StateFlow stateFlow = mock(StateFlow.class);

        when(appSolOneEmailGenerator.buildEmailDTO(caseData)).thenReturn(appSolEmail);
        when(respSolOneEmailGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
            .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail);
        verify(appSolOneEmailGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailGenerator, never()).buildEmailDTO(caseData);
    }
}

