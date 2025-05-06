package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;

class AppSolOneEmailGeneratorTest {

    protected static final String APPLICANT_LEGAL_ORG_NAME = "applicant-legal-org-name";

    private OrganisationService organisationService;
    private AppSolOneEmailDTOGenerator emailGenerator;
    private NotificationsSignatureConfiguration configuration;
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        organisationService = Mockito.mock(OrganisationService.class);
        emailGenerator = new AppSolOneEmailDTOGenerator(configuration, featureToggleService, organisationService) {
            @Override
            public String getEmailTemplateId(CaseData caseData) {
                return "template-id";
            }

            @Override
            protected String getReferenceTemplate() {
                return "reference-template";
            }
        };
    }

    @Test
    void shouldReturnCorrectEmailAddress() {
        CaseData caseData = CaseData.builder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("test@example.com").build())
            .build();

        String emailAddress = emailGenerator.getEmailAddress(caseData);

        assertThat(emailAddress).isEqualTo("test@example.com");
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = CaseData.builder().build();
        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn(APPLICANT_LEGAL_ORG_NAME);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailGenerator.addCustomProperties(properties, caseData);

        assertThat(updatedProperties).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, APPLICANT_LEGAL_ORG_NAME);
        notificationUtilsMockedStatic.close();
    }

    @Test
    void shouldReturnNotifyAsTrue_WhenApplicantRepresented() {
        CaseData caseData = CaseData.builder()
                .applicant1Represented(YesOrNo.YES)
                .build();
        Boolean shouldNotify = emailGenerator.getShouldNotify(caseData);
        assertThat(shouldNotify).isTrue();
    }

    @Test
    void shouldReturnNotifyAsFalse_WhenApplicantIsLip() {
        CaseData caseData = CaseData.builder()
                .applicant1Represented(YesOrNo.NO)
                .build();
        Boolean shouldNotify = emailGenerator.getShouldNotify(caseData);
        assertThat(shouldNotify).isFalse();
    }
}
