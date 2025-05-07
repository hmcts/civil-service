package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;

class TemplateCommonPropertiesHelperTest {

    private static final String TEST_SPEC_UNSPEC_CONTACT = "SpecUnspec Contact";
    private static final String TEST_CNBC_CONTACT = "CNBC Contact";
    private static final String TEST_RAISE_QUERY_LR = "Raise Query LR"; // Adding the constant here

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private CaseData caseData;

    private TemplateCommonPropertiesHelper helper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        helper = new TemplateCommonPropertiesHelper(configuration, featureToggleService);
    }

    @Test
    void shouldAddSpecAndUnspecContactForLRCase() {
        Map<String, String> properties = new java.util.HashMap<>();
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(true);
        when(caseData.isLipCase()).thenReturn(false);
        when(caseData.getCcdState()).thenReturn(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);

        helper.addSpecAndUnspecContact(caseData, properties);

        assertThat(properties)
            .containsEntry(SPEC_UNSPEC_CONTACT, TEST_RAISE_QUERY_LR);
    }

    @Test
    void shouldAddCnbcContactForLRCase() {
        Map<String, String> properties = new java.util.HashMap<>();
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(true);
        when(caseData.isLipCase()).thenReturn(false);
        when(caseData.getCcdState()).thenReturn(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);

        helper.addCnbcContact(caseData, properties);

        assertThat(properties)
            .containsEntry(CNBC_CONTACT, TEST_RAISE_QUERY_LR);
    }

    @Test
    void shouldAddSpecAndUnspecContactForNonLRCase() {
        Map<String, String> properties = new java.util.HashMap<>();
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(false);

        helper.addSpecAndUnspecContact(caseData, properties);

        assertThat(properties)
            .containsEntry(SPEC_UNSPEC_CONTACT, TEST_SPEC_UNSPEC_CONTACT);
    }

    @Test
    void shouldAddCnbcContactForNonLRCase() {
        Map<String, String> properties = new java.util.HashMap<>();
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(false);

        helper.addCnbcContact(caseData, properties);

        assertThat(properties)
            .containsEntry(CNBC_CONTACT, TEST_CNBC_CONTACT);
    }

    @Test
    void shouldCheckIfQueryManagementAllowedForLRCase() {
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(true);
        when(caseData.isLipCase()).thenReturn(false);
        when(caseData.getCcdState()).thenReturn(CaseState.AWAITING_APPLICANT_INTENTION);

        boolean result = helper.isQueryManagementAllowedForLRCase(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldCheckIfQueryManagementNotAllowedForNonLRCase() {
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(false);

        boolean result = helper.isQueryManagementAllowedForLRCase(caseData);

        assertThat(result).isFalse();
    }
}
