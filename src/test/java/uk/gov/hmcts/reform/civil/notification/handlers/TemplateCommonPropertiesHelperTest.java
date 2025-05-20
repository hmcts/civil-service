package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;

class TemplateCommonPropertiesHelperTest {

    private static final String TEST_HMCTS_SIGNATURE = "HMCTS Signature";
    private static final String TEST_PHONE_CONTACT = "123-456-789";
    private static final String TEST_OPENING_HOURS = "9:00 AM - 5:00 PM";
    private static final String TEST_SPEC_UNSPEC_CONTACT = "SpecUnspec Contact";
    private static final String TEST_LIP_CONTACT = "contactocmc@justice.gov.uk";
    private static final String TEST_CNBC_CONTACT = "CNBC Contact";

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

        when(configuration.getHmctsSignature()).thenReturn(TEST_HMCTS_SIGNATURE);
        when(configuration.getPhoneContact()).thenReturn(TEST_PHONE_CONTACT);
        when(configuration.getOpeningHours()).thenReturn(TEST_OPENING_HOURS);
        when(configuration.getSpecUnspecContact()).thenReturn(TEST_SPEC_UNSPEC_CONTACT);
        when(configuration.getCnbcContact()).thenReturn(TEST_CNBC_CONTACT);
    }

    @Test
    void shouldAddSpecAndUnspecContactForLRCase() {
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(true);
        when(caseData.isLipCase()).thenReturn(false);
        when(caseData.getCcdState()).thenReturn(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);

        Map<String, String> properties = new java.util.HashMap<>();
        helper.addSpecAndUnspecContact(caseData, properties);

        assertThat(properties)
            .containsEntry(SPEC_UNSPEC_CONTACT, TEST_SPEC_UNSPEC_CONTACT);
    }

    @Test
    void shouldLipContactForLipCase() {
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(true);
        when(featureToggleService.isLipQueryManagementEnabled(any())).thenReturn(true);
        when(caseData.isLipCase()).thenReturn(true);
        when(caseData.getCcdState()).thenReturn(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);

        Map<String, String> properties = new java.util.HashMap<>();
        helper.addLipContact(caseData, properties);

        assertThat(properties)
            .containsEntry(SPEC_UNSPEC_CONTACT, TEST_LIP_CONTACT);
    }

    @Test
    void shouldAddCnbcContactForLRCase() {
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(true);
        when(caseData.isLipCase()).thenReturn(false);
        when(caseData.getCcdState()).thenReturn(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);

        Map<String, String> properties = new java.util.HashMap<>();
        helper.addCnbcContact(caseData, properties);

        assertThat(properties)
            .containsEntry(CNBC_CONTACT, TEST_CNBC_CONTACT);
    }

    @Test
    void shouldAddSpecAndUnspecContactForNonLRCase() {
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(false);

        Map<String, String> properties = new java.util.HashMap<>();
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
    void shouldCheckIfQueryManagementAllowedForLipCase() {
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(true);
        when(featureToggleService.isLipQueryManagementEnabled(any())).thenReturn(true);
        when(caseData.isLipCase()).thenReturn(true);
        when(caseData.getCcdState()).thenReturn(CaseState.AWAITING_APPLICANT_INTENTION);

        boolean result = helper.isQueryManagementAllowedForLipCase(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldCheckIfQueryManagementNotAllowedForNonLRCase() {
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(false);

        boolean result = helper.isQueryManagementAllowedForLRCase(caseData);

        assertThat(result).isFalse();
    }

    @Test
    void shouldAddCommonFooterSignature() {
        Map<String, String> properties = new java.util.HashMap<>();

        helper.addCommonFooterSignature(properties);

        assertThat(properties)
            .containsEntry(NotificationData.HMCTS_SIGNATURE, TEST_HMCTS_SIGNATURE)
            .containsEntry(NotificationData.PHONE_CONTACT, TEST_PHONE_CONTACT)
            .containsEntry(NotificationData.OPENING_HOURS, TEST_OPENING_HOURS);
    }
}
