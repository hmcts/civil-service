package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.RAISE_QUERY_LR;

class EmailDTOGeneratorTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_TEMPLATE_ID = "template-id";
    private static final String TEST_REFERENCE_TEMPLATE = "reference-%s";
    private static final String LEGACY_CASE_REFERENCE = "12345";
    protected static final String CUSTOM_KEY = "customKey";
    protected static final String CUSTOM_VALUE = "customValue";

    private EmailDTOGenerator emailDTOGenerator;

    @Mock
    private NotificationsSignatureConfiguration configuration;
    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a concrete implementation of the abstract class for testing
        emailDTOGenerator = new EmailDTOGenerator(configuration, featureToggleService) {

            @Override
            protected Boolean getShouldNotify(CaseData caseData) {
                return Boolean.TRUE;
            }

            @Override
            protected String getEmailAddress(CaseData caseData) {
                return TEST_EMAIL;
            }

            @Override
            protected String getEmailTemplateId(CaseData caseData) {
                return TEST_TEMPLATE_ID;
            }

            @Override
            protected String getReferenceTemplate() {
                return TEST_REFERENCE_TEMPLATE;
            }

            @Override
            protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
                properties.put(CUSTOM_KEY, CUSTOM_VALUE);
                return properties;
            }
        };
    }

    @Test
    void shouldBuildEmailDTOCorrectly_whenQMToggleOff() {
        when(caseData.getLegacyCaseReference()).thenReturn(LEGACY_CASE_REFERENCE);
        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getCnbcContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

        EmailDTO emailDTO = emailDTOGenerator.buildEmailDTO(caseData);

        assertThat(emailDTO.getTargetEmail()).isEqualTo(TEST_EMAIL);
        assertThat(emailDTO.getEmailTemplate()).isEqualTo(TEST_TEMPLATE_ID);
        assertThat(emailDTO.getReference()).isEqualTo(String.format(TEST_REFERENCE_TEMPLATE, LEGACY_CASE_REFERENCE));
        assertThat(emailDTO.getParameters())
            .containsEntry(CLAIM_REFERENCE_NUMBER, "1234567890123456")
            .containsEntry(CASEMAN_REF, LEGACY_CASE_REFERENCE)
            .containsEntry(CUSTOM_KEY, CUSTOM_VALUE)
            .containsEntry(PARTY_REFERENCES, "Claimant reference: Not provided - Defendant reference: Not provided")
            .containsEntry(PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050")
            .containsEntry(OPENING_HOURS, "Monday to Friday, 8.30am to 5pm")
            .containsEntry(SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
                + "\n Email for Damages Claims: damagesclaims@justice.gov.uk")
            .containsEntry(HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service")
            .containsEntry(CNBC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk");
    }

    @Test
    void shouldBuildEmailDTOCorrectly_whenQMToggleOn() {
        when(caseData.getLegacyCaseReference()).thenReturn(LEGACY_CASE_REFERENCE);
        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(caseData.getCcdState()).thenReturn(CaseState.CASE_ISSUED);
        when(caseData.getRespondent1Represented()).thenReturn(YesOrNo.YES);
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getCnbcContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(true);

        EmailDTO emailDTO = emailDTOGenerator.buildEmailDTO(caseData);

        assertThat(emailDTO.getTargetEmail()).isEqualTo(TEST_EMAIL);
        assertThat(emailDTO.getEmailTemplate()).isEqualTo(TEST_TEMPLATE_ID);
        assertThat(emailDTO.getReference()).isEqualTo(String.format(TEST_REFERENCE_TEMPLATE, LEGACY_CASE_REFERENCE));
        assertThat(emailDTO.getParameters())
            .containsEntry(CLAIM_REFERENCE_NUMBER, "1234567890123456")
            .containsEntry(CASEMAN_REF, LEGACY_CASE_REFERENCE)
            .containsEntry(CUSTOM_KEY, CUSTOM_VALUE)
            .containsEntry(PARTY_REFERENCES, "Claimant reference: Not provided - Defendant reference: Not provided")
            .containsEntry(PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050")
            .containsEntry(OPENING_HOURS, "Monday to Friday, 8.30am to 5pm")
            .containsEntry(SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR)
            .containsEntry(HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service")
            .containsEntry(CNBC_CONTACT, RAISE_QUERY_LR);
    }
}
