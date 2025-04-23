package uk.gov.hmcts.reform.civil.notification.handlers.claimantLipHelpWithFees;

import org.junit.jupiter.api.BeforeEach;
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
class NotifyClaimantLipHelpWithFeesPartiesEmailGeneratorTest {

    @Mock
    private NotifyClaimantLipHelpWithFeesEmailDTOGenerator dtoGenerator;

    @InjectMocks
    private NotifyClaimantLipHelpWithFeesPartiesEmailGenerator partiesGenerator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder().build();
    }

    @Test
    void shouldReturnExactlyOneEmailDTO() {
        EmailDTO dto = EmailDTO.builder().build();
        when(dtoGenerator.buildEmailDTO(caseData)).thenReturn(dto);

        Set<EmailDTO> result = partiesGenerator.getPartiesToNotify(caseData);
        assertThat(result).containsExactly(dto);
    }
}
