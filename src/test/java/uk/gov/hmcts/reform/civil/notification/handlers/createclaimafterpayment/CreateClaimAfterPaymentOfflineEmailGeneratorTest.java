package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateClaimAfterPaymentOfflineEmailGeneratorTest {

    public static final String TARGET_EMAIL = "test@example.com";
    public static final String TEMPLATE_ID = "template-id";
    public static final String REFERENCE = "reference";

    @Mock
    private CreateClaimAfterPaymentOfflineAppSolOneEmailDTOGenerator appSolOneGenerator;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CreateClaimAfterPaymentOfflineEmailGenerator emailGenerator;

    @Test
    void shouldReturnSingleEmailDTO_whenNotLipvLipAndToggleOff() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLipOneVOne()).thenReturn(false);

        EmailDTO dto = EmailDTO.builder()
                .targetEmail(TARGET_EMAIL)
                .emailTemplate(TEMPLATE_ID)
                .reference(REFERENCE)
                .build();
        when(appSolOneGenerator.buildEmailDTO(caseData)).thenReturn(dto);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData);

        assertThat(result).containsExactly(dto);
    }

    @Test
    void shouldReturnEmptySet_whenLipvLipAndToggleOn() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnSingleEmailDTO_whenLipvLipAndToggleOff() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        EmailDTO dto = EmailDTO.builder()
                .targetEmail(TARGET_EMAIL)
                .emailTemplate(TEMPLATE_ID)
                .reference(REFERENCE)
                .build();
        when(appSolOneGenerator.buildEmailDTO(caseData)).thenReturn(dto);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData);

        assertThat(result).containsExactly(dto);
    }
}
