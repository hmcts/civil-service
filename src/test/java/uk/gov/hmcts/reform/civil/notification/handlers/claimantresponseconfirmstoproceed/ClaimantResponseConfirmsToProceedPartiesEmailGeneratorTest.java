package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed;

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
import uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed.multipartynottoproceed.MultiPartyNotToProceedAppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed.multipartynottoproceed.MultiPartyNotToProceedRespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed.multipartynottoproceed.MultiPartyNotToProceedRespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

public class ClaimantResponseConfirmsToProceedPartiesEmailGeneratorTest {

    @Mock
    private ClaimantResponseConfirmsToProceedAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;

    @Mock
    private ClaimantResponseConfirmsToProceedDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @Mock
    private ClaimantResponseConfirmsToProceedRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @Mock
    private ClaimantResponseConfirmsToProceedRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;

    @Mock
    private MultiPartyNotToProceedAppSolOneEmailDTOGenerator appSolOneEmailDTOGeneratorNTP;

    @Mock
    private MultiPartyNotToProceedRespSolOneEmailDTOGenerator respSolOneEmailDTOGeneratorNTP;

    @Mock
    private MultiPartyNotToProceedRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGeneratorNTP;

    @Mock
    private ClaimantResponseConfirmsToProceedEmailHelper helper;

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @InjectMocks
    private ClaimantResponseConfirmsToProceedPartiesEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldNotifyOnlyAppSolAndRespSolOneWhenTwoRespondentRepresentativesFlagAndMultiPartyNotProceedIsNotSet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);

        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
                 .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(helper.isMultiPartyNotProceed(caseData, false)).thenReturn(false);
        when(helper.isMultiPartyNotProceed(caseData, true)).thenReturn(false);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail);
        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(appSolOneEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyRespSolTwoWhenTwoRespondentRepresentativesFlagAndMultiPartyNotProceedIsNotSet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);

        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolTwoEmail);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
                 .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
        when(helper.isMultiPartyNotProceed(caseData, false)).thenReturn(false);
        when(helper.isMultiPartyNotProceed(caseData, true)).thenReturn(false);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        multiPartyScenarioMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail, respSolTwoEmail);
        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(appSolOneEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyLRsNotToProceedWhenMultiPartyNotProceedIsSet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);

        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
        when(appSolOneEmailDTOGeneratorNTP.buildEmailDTO(caseData)).thenReturn(appSolEmail);
        when(respSolOneEmailDTOGeneratorNTP.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailDTOGeneratorNTP.buildEmailDTO(caseData)).thenReturn(respSolTwoEmail);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
                 .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
        when(helper.isMultiPartyNotProceed(caseData, false)).thenReturn(true);
        when(helper.isMultiPartyNotProceed(caseData, true)).thenReturn(true);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        multiPartyScenarioMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail, respSolTwoEmail);
        verify(appSolOneEmailDTOGeneratorNTP).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGeneratorNTP).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGeneratorNTP).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyDefendant_whenLip() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);

        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(caseData.isLRvLipOneVOne()).thenReturn(true);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(defendantEmail);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
                 .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(helper.isMultiPartyNotProceed(caseData, false)).thenReturn(false);
        when(helper.isMultiPartyNotProceed(caseData, true)).thenReturn(false);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, defendantEmail);
        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(appSolOneEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData);
    }
}
