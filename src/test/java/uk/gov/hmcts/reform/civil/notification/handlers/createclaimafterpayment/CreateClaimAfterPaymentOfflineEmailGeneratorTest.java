package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateClaimAfterPaymentOfflineEmailGeneratorTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CreateClaimAfterPaymentOfflineEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }

    @Test
    void shouldReturnEmptySetWhenLipVLipEnabled() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData);

        assertThat(result).isEmpty();
        verify(featureToggleService).isLipVLipEnabled();
    }

    @Test
    void shouldCallSuperWhenLipVLipDisabled() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData);

        assertThat(result).isNotNull();
        verify(featureToggleService).isLipVLipEnabled();
    }

    @Test
    void shouldCallSuperWhenLipVLipNotOneVOneAndLipVLipDisabled() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLipOneVOne()).thenReturn(false);

        Set<EmailDTO> result = emailGenerator.getPartiesToNotify(caseData);

        assertThat(result).isNotNull();
    }
}
