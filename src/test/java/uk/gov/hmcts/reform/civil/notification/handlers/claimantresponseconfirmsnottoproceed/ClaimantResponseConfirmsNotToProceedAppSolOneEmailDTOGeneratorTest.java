package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmsnottoproceed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
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
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

public class ClaimantResponseConfirmsNotToProceedAppSolOneEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private ClaimantResponseConfirmsNotToProceedAppSolOneEmailDTOGenerator emailDTOGenerator;

    private OrganisationService organisationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenCaseIsUnspec() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getClaimantSolicitorConfirmsNotToProceed()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenCaseIsSpecAndPartAdmitPayImmediatelyAccepted() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YES)
            .showResponseOneVOneFlag(ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getNotifyRespondentSolicitorPartAdmitPayImmediatelyAcceptedSpec()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenCaseIsSpecAndNotPartAdmitPayImmediatelyAccepted() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getClaimantSolicitorConfirmsNotToProceedSpec()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("claimant-confirms-not-to-proceed-respondent-notification-%s");
    }

    @Test
    void shouldAddCustomPropertiesWhenCaseIsUnspec() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .build();

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
    void shouldAddCustomPropertiesWhenCaseIsSpecAndNotPartAdmitPayImmediatelyAccepted() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .build();

        String partyName = "party name";
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);
        partyUtilsMockedStatic.when(() -> getPartyNameBasedOnType(any())).thenReturn(partyName);

        String legalOrg = "legal org";
        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn(legalOrg);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        partyUtilsMockedStatic.close();
        notificationUtilsMockedStatic.close();

        assertThat(updatedProperties.size()).isEqualTo(2);
        assertThat(updatedProperties).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, legalOrg);
        assertThat(updatedProperties).containsEntry(RESPONDENT_NAME, partyName);
    }

    @Test
    void shouldAddCustomPropertiesWhenCaseIsSpecAndPartAdmitPayImmediatelyAccepted() {
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().build();

        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YES)
            .respondent1OrganisationPolicy(organisationPolicy)
            .showResponseOneVOneFlag(ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        String appLegalOrg = "app legal org";
        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn(appLegalOrg);

        String respLegalOrg = "resp legal org";
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getRespondentLegalOrganizationName(organisationPolicy, organisationService))
            .thenReturn(respLegalOrg);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        notificationUtilsMockedStatic.close();

        assertThat(updatedProperties.size()).isEqualTo(2);
        assertThat(updatedProperties).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, appLegalOrg);
        assertThat(updatedProperties).containsEntry(CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, respLegalOrg);
    }
}
