package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

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
class CreateClaimAfterPaymentContinuingOnlineEmailGeneratorTest {

    public static final String TARGET_EMAIL = "test@example.com";
    public static final String TEMPLATE_ID = "template-id";
    public static final String REFERENCE = "reference";

    @Mock
    private CreateClaimAfterPaymentContinuingOnlineAppSolOneEmailDTOGenerator appSolOneGenerator;

    @InjectMocks
    private CreateClaimAfterPaymentContinuingOnlineEmailGenerator emailGenerator;

    @Test
    void shouldReturnSetOfEmailDTOs() {
        CaseData caseData = CaseData.builder().build();

        EmailDTO emailDTO = EmailDTO.builder()
                .targetEmail(TARGET_EMAIL)
                .emailTemplate(TEMPLATE_ID)
                .reference(REFERENCE)
                .build();

        when(appSolOneGenerator.buildEmailDTO(caseData)).thenReturn(emailDTO);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData);

        assertThat(result).containsExactly(emailDTO);
    }
}