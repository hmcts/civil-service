package uk.gov.hmcts.reform.civil.notification.handlers.discontinueclaimparties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

class DiscontinueClaimPartiesAllPartiesEmailGeneratorTest {

    @Mock
    private DiscontinueClaimPartiesAppSolOneEmailDTOGenerator discontinueClaimPartiesAppSolOneEmailDTOGenerator;

    @Mock
    private DiscontinueClaimPartiesRespSolOneEmailDTOGenerator discontinueClaimPartiesRespSolOneEmailDTOGenerator;

    @Mock
    private DiscontinueClaimPartiesRespSolTwoEmailDTOGenerator discontinueClaimPartiesRespSolTwoEmailDTOGenerator;

    @InjectMocks
    private DiscontinueClaimPartiesAllPartiesEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldExtendAllPartiesEmailGeneratorWithCorrectDependencies() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
