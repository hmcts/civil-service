package uk.gov.hmcts.reform.civil.notification.handlers.claimantdefendantagreedmediation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

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
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getNotifyDefendantLRForMediation()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("mediation-agreement-respondent-notification-%s");
    }

    @Test
    void shouldNotifyWhenSendMediationNotificationDefendant2LRCarmIsTrue() {
        CaseData caseData = CaseData.builder().build();

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm(caseData, true))
            .thenReturn(true);

        boolean shouldNotify = emailDTOGenerator.getShouldNotify(caseData);

        notificationUtilsMockedStatic.close();

        assertThat(shouldNotify).isTrue();
    }

    @Test
    void shouldNotNotifyWhenSendMediationNotificationDefendant2LRCarmIsFalse() {
        CaseData caseData = CaseData.builder().build();

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(false);
        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm(caseData, true))
            .thenReturn(false);

        boolean shouldNotify = emailDTOGenerator.getShouldNotify(caseData);

        notificationUtilsMockedStatic.close();

        assertThat(shouldNotify).isFalse();
    }
}
