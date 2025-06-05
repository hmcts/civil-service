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
import uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed.multipartyclaimantoptsoutonerespandcasenotproceeded.MultiPartyNotToProceedAppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed.multipartyclaimantoptsoutonerespandcasenotproceeded.MultiPartyNotToProceedRespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed.multipartyclaimantoptsoutonerespandcasenotproceeded.MultiPartyNotToProceedRespSolTwoEmailDTOGenerator;
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
        String taskId = "task id";

        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
                 .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(helper.isMultiPartyNotProceed(caseData, false)).thenReturn(false);
        when(helper.isMultiPartyNotProceed(caseData, true)).thenReturn(false);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail);
        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyRespSolTwoWhenTwoRespondentRepresentativesFlagAndMultiPartyNotProceedIsNotSet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);
        String taskId = "task id";

        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(respSolTwoEmail);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
                 .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
        when(helper.isMultiPartyNotProceed(caseData, false)).thenReturn(false);
        when(helper.isMultiPartyNotProceed(caseData, true)).thenReturn(false);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        multiPartyScenarioMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail, respSolTwoEmail);
        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyLRsNotToProceedWhenMultiPartyNotProceedIsSet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolTwoEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);
        String taskId = "task id";

        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
        when(appSolOneEmailDTOGeneratorNTP.buildEmailDTO(caseData, taskId)).thenReturn(appSolEmail);
        when(respSolOneEmailDTOGeneratorNTP.buildEmailDTO(caseData, taskId)).thenReturn(respSolOneEmail);
        when(respSolTwoEmailDTOGeneratorNTP.buildEmailDTO(caseData, taskId)).thenReturn(respSolTwoEmail);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
                 .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
        when(helper.isMultiPartyNotProceed(caseData, false)).thenReturn(true);
        when(helper.isMultiPartyNotProceed(caseData, true)).thenReturn(true);

        MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        multiPartyScenarioMockedStatic.close();

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail, respSolTwoEmail);
        verify(appSolOneEmailDTOGeneratorNTP).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGeneratorNTP).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGeneratorNTP).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldNotifyDefendant_whenLip() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);
        StateFlow stateFlow = mock(StateFlow.class);
        String taskId = "task id";

        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(caseData.isLRvLipOneVOne()).thenReturn(true);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(appSolEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData, taskId)).thenReturn(defendantEmail);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlowEngine.evaluate(caseData)
                 .isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(helper.isMultiPartyNotProceed(caseData, false)).thenReturn(false);
        when(helper.isMultiPartyNotProceed(caseData, true)).thenReturn(false);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData, taskId);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, defendantEmail);
        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData, taskId);
        verify(appSolOneEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData, taskId);
        verify(respSolOneEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData, taskId);
        verify(respSolTwoEmailDTOGeneratorNTP, never()).buildEmailDTO(caseData, taskId);
    }
}
