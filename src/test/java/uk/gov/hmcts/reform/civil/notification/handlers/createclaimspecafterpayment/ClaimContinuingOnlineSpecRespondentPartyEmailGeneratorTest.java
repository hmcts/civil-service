package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineSpecRespondentPartyEmailGeneratorTest {

    public static final String TASK_ID = "reference";
    @Mock
    private ClaimContinuingOnlineSpecRespondentPartyEmailDTOGenerator dtoGenerator;

    @InjectMocks
    private ClaimContinuingOnlineSpecRespondentPartyEmailGenerator generator;

    @Test
    void shouldReturnEmailDTOWhenShouldNotifyIsTrue() {
        CaseData caseData = CaseData.builder().build();
        EmailDTO emailDTO = EmailDTO.builder().build();
        when(dtoGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(dtoGenerator.buildEmailDTO(caseData, TASK_ID)).thenReturn(emailDTO);

        Set<EmailDTO> result = generator.getPartiesToNotify(caseData, TASK_ID);

        assertThat(result).containsExactly(emailDTO);
    }

    @Test
    void shouldReturnEmptySetWhenShouldNotifyIsFalse() {
        CaseData caseData = CaseData.builder().build();
        when(dtoGenerator.getShouldNotify(caseData)).thenReturn(false);

        Set<EmailDTO> result = generator.getPartiesToNotify(caseData, TASK_ID);

        assertThat(result).isEmpty();
    }
}