package uk.gov.hmcts.reform.civil.notification.handlers.translatedordernoticeuploaded;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TranslatedOrderNoticeUploadedAllPartiesEmailGeneratorTest {

    @Mock
    private TranslatedOrderNoticeUploadedClaimantEmailDTOGenerator claimantEmailDTOGenerator;

    @Mock
    private TranslatedOrderNoticeUploadedDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @InjectMocks
    private TranslatedOrderNoticeUploadedAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGeneratorWithCorrectDependencies() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
