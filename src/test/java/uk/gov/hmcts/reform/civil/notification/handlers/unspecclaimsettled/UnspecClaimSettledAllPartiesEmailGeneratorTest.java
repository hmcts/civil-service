package uk.gov.hmcts.reform.civil.notification.handlers.unspecclaimsettled;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.settleclaimpaidinfullnotification.SettleClaimPaidInFullNotificationAllPartiesEmailGenerator;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnspecClaimSettledAllPartiesEmailGeneratorTest {

    private static final String TASK_ID = "UnspecClaimSettledNotifier";

    @Mock
    private UnspecClaimSettledRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @Mock
    private SettleClaimPaidInFullNotificationAllPartiesEmailGenerator settleClaimPaidInFullEmailGenerator;

    private UnspecClaimSettledAllPartiesEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        emailGenerator = new UnspecClaimSettledAllPartiesEmailGenerator(respSolOneEmailDTOGenerator,
                                                                        settleClaimPaidInFullEmailGenerator);
    }

    @Test
    void shouldNotifyDefendantLrWithUnspecClaimSettledEmail_whenOneVOne() {
        CaseData caseData = CaseData.builder().build();
        EmailDTO emailDTO = new EmailDTO();
        when(respSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, TASK_ID)).thenReturn(emailDTO);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData, TASK_ID);

        assertThat(result).containsExactly(emailDTO);
        verifyNoInteractions(settleClaimPaidInFullEmailGenerator);
    }

    @Test
    void shouldKeepExistingSettleClaimNotifications_whenMultiParty() {
        CaseData caseData = CaseData.builder()
            .respondent2(new Party().setType(Party.Type.COMPANY).setCompanyName("Defendant Two"))
            .build();
        EmailDTO emailDTO = new EmailDTO();
        when(settleClaimPaidInFullEmailGenerator.getPartiesToNotify(any(), any())).thenReturn(Set.of(emailDTO));

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData, TASK_ID);

        assertThat(result).containsExactly(emailDTO);
        verify(settleClaimPaidInFullEmailGenerator).getPartiesToNotify(caseData, TASK_ID);
        verifyNoInteractions(respSolOneEmailDTOGenerator);
    }
}
