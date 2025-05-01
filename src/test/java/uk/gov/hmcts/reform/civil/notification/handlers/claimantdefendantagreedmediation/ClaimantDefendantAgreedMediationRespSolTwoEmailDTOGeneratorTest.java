package uk.gov.hmcts.reform.civil.notification.handlers.claimantdefendantagreedmediation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ClaimantDefendantAgreedMediationRespSolTwoEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ClaimantDefendantAgreedMediationRespSolTwoEmailDTOGenerator emailDTOGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenCarmIsEnabled() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        when(notificationsProperties.getNotifyDefendantLRForMediation()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenCarmIsNotEnabled() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(false);
        when(notificationsProperties.getNotifyRespondentLRMediationAgreementTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("mediation-agreement-respondent-notification-%s");
    }
}
