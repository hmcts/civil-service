package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.APPLICANT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

public class ClaimantResponseConfirmsToProceedAppSolOneEmailDTOGeneratorTest {

    @Mock
    private ClaimantResponseConfirmsToProceedEmailHelper helper;

    @InjectMocks
    private ClaimantResponseConfirmsToProceedAppSolOneEmailDTOGenerator emailDTOGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private OrganisationService organisationService;

    @Test
    void shouldGetEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();

        String expectedTemplateId = "template-id";
        when(helper.getTemplate(caseData, true)).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("claimant-confirms-to-proceed-applicant-notification-%s");
    }

    @Test
    void shouldAddCustomPropertiesWhenUnspec() {
        CaseData caseData = CaseData.builder().caseAccessCategory(UNSPEC_CLAIM).build();

        String legalOrg = "legal org";
        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn(legalOrg);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        notificationUtilsMockedStatic.close();

        assertThat(updatedProperties.size()).isEqualTo(1);
        assertThat(updatedProperties).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, legalOrg);
    }

    @Test
    void shouldAddCustomPropertiesWhenSpec() {
        CaseData caseData = CaseData.builder().caseAccessCategory(SPEC_CLAIM).build();

        String legalOrg = "legal org";
        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn(legalOrg);

        String partyName = "partyName";
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(any())).thenReturn(partyName);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        notificationUtilsMockedStatic.close();
        partyUtilsMockedStatic.close();

        assertThat(updatedProperties.size()).isEqualTo(3);
        assertThat(updatedProperties).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, legalOrg);
        assertThat(updatedProperties).containsEntry(RESPONDENT_NAME, partyName);
        assertThat(updatedProperties).containsEntry(APPLICANT_ONE_NAME, partyName);
    }

}
