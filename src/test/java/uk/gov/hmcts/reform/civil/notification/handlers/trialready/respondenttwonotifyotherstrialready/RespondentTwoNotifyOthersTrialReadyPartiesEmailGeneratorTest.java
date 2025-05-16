package uk.gov.hmcts.reform.civil.notification.handlers.trialready.respondenttwonotifyotherstrialready;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyAppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyDefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyDefendantTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyRespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyRespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class RespondentTwoNotifyOthersTrialReadyPartiesEmailGeneratorTest {

    @InjectMocks
    private RespondentTwoNotifyOthersTrialReadyPartiesEmailGenerator emailGenerator;

    @Mock
    private TrialReadyAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;

    @Mock
    private TrialReadyClaimantEmailDTOGenerator claimantEmailDTOGenerator;

    @Mock
    private TrialReadyRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @Mock
    private TrialReadyRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;

    @Mock
    private TrialReadyDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @Mock
    private TrialReadyDefendantTwoEmailDTOGenerator defendantTwoEmailDTOGenerator;

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @Test
    void shouldNotifyAppSolOneAndRespSolOneWhenRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);

        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolOneEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolOneEmail, respSolOneEmail);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyClaimantAndDefendantWhenLip() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO claimantEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);

        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
        when(claimantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(claimantEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(defendantEmail);
        when(caseData.isApplicantNotRepresented()).thenReturn(true);
        when(caseData.isRespondent1LiP()).thenReturn(true);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(claimantEmail, defendantEmail);

        verify(claimantEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyAppSolOneAndRespSolOneWhenNotSpec() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolOneEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);

        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolOneEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolOneEmail, respSolOneEmail);

        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotNotifyPartiesWhenTrialReadyRespondentIsSet() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.getTrialReadyApplicant()).thenReturn(YES);
        when(caseData.getTrialReadyRespondent1()).thenReturn(YES);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).isEmpty();

        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(respSolTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(defendantTwoEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(appSolOneEmailDTOGenerator, never()).buildEmailDTO(caseData);
        verify(claimantEmailDTOGenerator, never()).buildEmailDTO(caseData);
    }
}
