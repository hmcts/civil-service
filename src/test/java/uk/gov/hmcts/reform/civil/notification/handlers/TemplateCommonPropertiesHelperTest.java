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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;

class TemplateCommonPropertiesHelperTest {

    private static final String TEST_HMCTS_SIGNATURE = "HMCTS Signature";
    private static final String TEST_PHONE_CONTACT = "123-456-789";
    private static final String TEST_OPENING_HOURS = "9:00 AM - 5:00 PM";
    private static final String TEST_SPEC_UNSPEC_CONTACT = "SpecUnspec Contact";
    private static final String TEST_CNBC_CONTACT = "CNBC Contact";
    private static final String TEST_HMCTS_SIGNATURE_WELSH = "hawliadau am Arian yn y Llys Sifil Ar-lein \\n Gwasanaeth Llysoedd a Thribiwnlysoedd EF";
    private static final String TEST_PHONE_CONTACT_WELSH = "Ffôn: 0300 303 5174";
    private static final String TEST_OPENING_HOURS_WELSH = "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm";
    private static final String TEST_LIP_CONTACT = "Email: contactocmc@justice.gov.uk";
    private static final String TEST_LIP_CONTACT_WELSH = "E-bost: ymholiadaucymraeg@justice.gov.uk";
    private static final String TEST_RAISE_QUERY_LR = "Raise query LR";
    private static final String TEST_RAISE_QUERY_LIP = "Raise query LIP";
    private static final String TEST_RAISE_QUERY_LIP_WELSH = "Raise query LIP welsh";

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
        when(configuration.getWelshHmctsSignature()).thenReturn(TEST_HMCTS_SIGNATURE_WELSH);
        when(configuration.getWelshPhoneContact()).thenReturn(TEST_PHONE_CONTACT_WELSH);
        when(configuration.getWelshOpeningHours()).thenReturn(TEST_OPENING_HOURS_WELSH);
        when(configuration.getLipContactEmail()).thenReturn(TEST_LIP_CONTACT);
        when(configuration.getLipContactEmailWelsh()).thenReturn(TEST_LIP_CONTACT_WELSH);
        when(configuration.getRaiseQueryLr()).thenReturn(TEST_RAISE_QUERY_LR);
        when(configuration.getRaiseQueryLip()).thenReturn(TEST_RAISE_QUERY_LIP);
        when(configuration.getRaiseQueryLipWelsh()).thenReturn(TEST_RAISE_QUERY_LIP_WELSH);
    }

    @Test
    void shouldAddSpecAndUnspecContactForLRCase_offline() {
        when(caseData.isLipCase()).thenReturn(false);
        when(caseData.getCcdState()).thenReturn(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);

        Map<String, String> properties = new java.util.HashMap<>();
        helper.addSpecAndUnspecContact(caseData, properties);

        assertThat(properties)
            .containsEntry(SPEC_UNSPEC_CONTACT, TEST_SPEC_UNSPEC_CONTACT);
    }

    @Test
    void shouldAddContactForLipCase() {
        when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
        when(caseData.isLipCase()).thenReturn(true);
        when(caseData.getCcdState()).thenReturn(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);

        Map<String, String> properties = new java.util.HashMap<>();
        helper.addLipContact(caseData, properties);

        assertThat(properties)
            .containsEntry(LIP_CONTACT, TEST_LIP_CONTACT);
    }

    @Test
    void shouldAddContactForWelshLipCase() {
        when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
        when(caseData.isLipCase()).thenReturn(true);
        when(caseData.getCcdState()).thenReturn(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);

        Map<String, String> properties = new java.util.HashMap<>();
        helper.addLipContactWelsh(caseData, properties);

        assertThat(properties)
            .containsEntry(LIP_CONTACT_WELSH, TEST_LIP_CONTACT_WELSH);
    }

    @Test
    void shouldAddCnbcContactForLRCase() {
        when(caseData.isLipCase()).thenReturn(false);
        when(caseData.getCcdState()).thenReturn(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);

        Map<String, String> properties = new java.util.HashMap<>();
        helper.addCnbcContact(caseData, properties);

        assertThat(properties)
            .containsEntry(CNBC_CONTACT, TEST_CNBC_CONTACT);
    }

    @Test
    void shouldCheckIfQueryManagementAllowedForLRCase() {
        when(caseData.isLipCase()).thenReturn(false);
        when(caseData.getCcdState()).thenReturn(CaseState.AWAITING_APPLICANT_INTENTION);

        boolean result = helper.isQueryManagementAllowedForLRCase(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldCheckIfQueryManagementAllowedForLipCase() {
        when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
        when(caseData.isLipCase()).thenReturn(true);
        when(caseData.getCcdState()).thenReturn(CaseState.AWAITING_APPLICANT_INTENTION);

        boolean result = helper.isQueryManagementAllowedForLipCase(caseData);

        assertThat(result).isTrue();
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

    @Test
    void shouldAddCommonFooterSignatureWelsh() {
        Map<String, String> properties = new java.util.HashMap<>();

        helper.addCommonFooterSignatureWelsh(properties);

        assertThat(properties)
            .containsEntry(NotificationData.WELSH_HMCTS_SIGNATURE, TEST_HMCTS_SIGNATURE_WELSH)
            .containsEntry(NotificationData.WELSH_PHONE_CONTACT, TEST_PHONE_CONTACT_WELSH)
            .containsEntry(NotificationData.WELSH_OPENING_HOURS, TEST_OPENING_HOURS_WELSH);
    }
}
