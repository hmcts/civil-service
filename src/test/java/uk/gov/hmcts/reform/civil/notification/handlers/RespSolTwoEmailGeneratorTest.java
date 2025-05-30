package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;

class RespSolTwoEmailGeneratorTest {

    protected static final String RESPONDENT_LEGAL_ORG_NAME = "respondent-legal-org-name";

    private OrganisationService organisationService;
    private RespSolTwoEmailDTOGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        organisationService = Mockito.mock(OrganisationService.class);
        emailGenerator = new RespSolTwoEmailDTOGenerator(organisationService) {
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
            .respondentSolicitor2EmailAddress("test@example.com").build();

        String emailAddress = emailGenerator.getEmailAddress(caseData);

        assertThat(emailAddress).isEqualTo("test@example.com");
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = CaseData.builder().build();
        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, Boolean.FALSE, organisationService))
            .thenReturn(RESPONDENT_LEGAL_ORG_NAME);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailGenerator.addCustomProperties(properties, caseData);

        assertThat(updatedProperties).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, RESPONDENT_LEGAL_ORG_NAME);
        notificationUtilsMockedStatic.close();
    }

    @Test
    void shouldReturnNotifyAsFalse_WhenTwoLRsAreInvolved() {
        CaseData caseData = CaseData.builder()
            .build();
        Boolean shouldNotify = emailGenerator.getShouldNotify(caseData);
        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldReturnNotifyAsTrue_WhenTwoLRsAreNotInvolved() {
        CaseData caseData = CaseData.builder()
            .respondent2(Party.builder().build())
            .respondent2SameLegalRepresentative(NO)
            .build();
        Boolean shouldNotify = emailGenerator.getShouldNotify(caseData);
        assertThat(shouldNotify).isTrue();
    }
}
