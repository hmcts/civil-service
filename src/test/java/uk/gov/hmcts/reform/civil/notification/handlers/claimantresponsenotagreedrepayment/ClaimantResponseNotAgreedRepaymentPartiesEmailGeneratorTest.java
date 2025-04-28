package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsenotagreedrepayment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClaimantResponseNotAgreedRepaymentPartiesEmailGeneratorTest {

    @Mock
    private ClaimantResponseNotAgreedRepaymentAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;

    @Mock
    private ClaimantResponseNotAgreedRepaymentClaimantEmailDTOGenerator claimantEmailDTOGenerator;

    @Mock
    private ClaimantResponseNotAgreedRepaymentDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @Mock
    private ClaimantResponseNotAgreedRepaymentRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @InjectMocks
    private ClaimantResponseNotAgreedRepaymentPartiesEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldNotifyAppSolOneAndRespSolOne_whenRepresented() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO respSolOneEmail = mock(EmailDTO.class);

        when(caseData.isApplicantLiP()).thenReturn(false);
        when(caseData.isRespondent1NotRepresented()).thenReturn(false);

        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolEmail);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respSolOneEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, respSolOneEmail);
        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(respSolOneEmailDTOGenerator).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotifyClaimantAndDefendant_whenLip() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO claimantEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);

        when(caseData.isApplicantLiP()).thenReturn(true);
        when(caseData.isRespondent1NotRepresented()).thenReturn(true);

        when(claimantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(claimantEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(defendantEmail);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(claimantEmail, defendantEmail);
        verify(claimantEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData);
    }
}
