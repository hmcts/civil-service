package uk.gov.hmcts.reform.civil.notification.handlers.unspecclaimsettled;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnspecClaimSettledAllPartiesEmailGeneratorTest {

    private static final String TASK_ID = "UnspecClaimSettledNotifier";

    @Mock
    private UnspecClaimSettledRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    private UnspecClaimSettledAllPartiesEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        emailGenerator = new UnspecClaimSettledAllPartiesEmailGenerator(respSolOneEmailDTOGenerator);
    }

    @Test
    void shouldNotifyDefendantLrWithUnspecClaimSettledEmail_whenOneVOne() {
        CaseData caseData = CaseData.builder().build();
        EmailDTO emailDTO = new EmailDTO();
        when(respSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, TASK_ID)).thenReturn(emailDTO);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData, TASK_ID);

        assertThat(result).containsExactly(emailDTO);
    }

    @Test
    void shouldNotSendAnyEmail_whenMultiParty() {
        CaseData caseData = CaseData.builder()
            .respondent2(new Party().setType(Party.Type.COMPANY).setCompanyName("Defendant Two"))
            .build();
        when(respSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData, TASK_ID);

        assertThat(result).isEmpty();
        verify(respSolOneEmailDTOGenerator, never()).buildEmailDTO(any(), any());
    }
}
