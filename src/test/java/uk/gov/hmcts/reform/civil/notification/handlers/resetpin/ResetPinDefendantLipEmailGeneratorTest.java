package uk.gov.hmcts.reform.civil.notification.handlers.resetpin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResetPinDefendantLipEmailGeneratorTest {

    @Mock
    private ResetPinDefendantLipEmailDTOGenerator resetPinDefendantLipEmailDTOGenerator;

    private ResetPinDefendantLipEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailGenerator = new ResetPinDefendantLipEmailGenerator(resetPinDefendantLipEmailDTOGenerator);
    }

    @Test
    void shouldGenerateEmailDTOForPartiesToNotify() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO emailDTO = EmailDTO.builder()
            .targetEmail("respondent@example.com")
            .emailTemplate("template-id")
            .parameters(Map.of("key", "value"))
            .reference("reset-pin-notification")
            .build();

        when(resetPinDefendantLipEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(emailDTO);

        Set<EmailDTO> partiesToNotify = emailGenerator.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).hasSize(1);
        assertThat(partiesToNotify).contains(emailDTO);
        verify(resetPinDefendantLipEmailDTOGenerator, times(1)).buildEmailDTO(caseData);
    }
}
