package uk.gov.hmcts.reform.civil.notification.handlers.translateddocumentuploaded;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TranslatedDocumentUploadedAllPartiesEmailGeneratorTest {

    @Mock
    private TranslatedDocumentUploadedAppSolOneEmailDTOGenerator appSolOneEmailGenerator;

    @Mock
    private TranslatedDocumentUploadedClaimantEmailDTOGenerator claimantEmailDTOGenerator;

    @Mock
    private TranslatedDocumentUploadedDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @InjectMocks
    private TranslatedDocumentUploadedAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGeneratorWithCorrectDependencies() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
