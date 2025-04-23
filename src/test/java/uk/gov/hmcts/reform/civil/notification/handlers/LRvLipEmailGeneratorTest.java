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

abstract class LRvLipEmailGeneratorTest {

    @Mock
    private AppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;

    @Mock
    private DefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @InjectMocks
    private LRvLipEmailGenerator lRvLipEmailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldNotifyAllParties_whenTwoRespondentRepresentativesFlagIsSet() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO appSolEmail = mock(EmailDTO.class);
        EmailDTO defendantEmail = mock(EmailDTO.class);
        when(appSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(appSolEmail);
        when(defendantEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(defendantEmail);

        Set<EmailDTO> partiesToNotify = lRvLipEmailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).containsExactlyInAnyOrder(appSolEmail, defendantEmail);
        verify(appSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(defendantEmailDTOGenerator).buildEmailDTO(caseData);
    }
}

