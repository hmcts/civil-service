package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

abstract class LipvLipEmailGeneratorTest {

    @Mock
    private ClaimantEmailDTOGenerator claimantEmailDTOGenerator;

    @Mock
    private DefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @InjectMocks
    private LipvLipEmailGenerator lipvLipEmailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldNotifyAllParties_whenTwoRespondentRepresentativesFlagIsSet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO claimantEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);
        when(claimantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(claimantEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(defendantEmail);

        Set<EmailDTO> partiesToNotify = lipvLipEmailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(claimantEmail, defendantEmail);
        verify(claimantEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData);
    }
}
