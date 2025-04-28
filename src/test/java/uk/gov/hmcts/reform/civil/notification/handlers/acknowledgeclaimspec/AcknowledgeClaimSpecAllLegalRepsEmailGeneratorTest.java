package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AcknowledgeClaimSpecAllLegalRepsEmailGeneratorTest {

    @InjectMocks
    private AcknowledgeClaimSpecAllLegalRepsEmailGenerator emailGenerator;

    @Test
    void shouldNotifyRespondents() {
        CaseData caseData = CaseData.builder().build();

        boolean result = emailGenerator.shouldNotifyRespondents(caseData);

        assertThat(result).isTrue();
    }
}
