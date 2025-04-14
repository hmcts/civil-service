package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_APPLICANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

@ExtendWith(MockitoExtension.class)
class NoCLipClaimantRepresentedNotifierTest {

    public static final Long CASE_ID = 1594901956117591L;

    @Mock
    NotificationsProperties notificationsProperties;

    @Mock
    OrganisationService organisationService;

    @InjectMocks
    private NoCLipClaimantRepresentedNotifier notifier;

    private CaseData caseData;

    private static final String DEFENDANT_EMAIL_ADDRESS = "defendantmail@hmcts.net";
    private static final String APPLICANT_EMAIL_ADDRESS = "applicantmail@hmcts.net";
    private static final String APPLICANT_SOLICITOR_EMAIL_ADDRESS = "applicantsolicitor@example.com";
    private static final String DEFENDANT_PARTY_NAME = "ABC ABC";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final String CLAIMANT_LIP_TEMPLATE = "claimant-lip-template-id";
    private static final String CLAIMANT_LIP_WELSH_TEMPLATE = "claimant-lip-template-welsh-id";
    private static final String DEFENDANT_LIP_TEMPLATE = "defendant-lip-template-id";
    private static final String CLAIMANT_COMPANY_NAME = "Company Name";
    private static final String APPLICANT_SOLICITOR_TEMPLATE = "applicant1-solicitor-template-id";
    public static final String TEMPLATE_REFERENCE = "notify-lip-after-noc-approval-000DC001";

    @BeforeEach
    public void setup() {
        caseData = CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                DEFENDANT_EMAIL_ADDRESS).build())
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_COMPANY_NAME)
                            .partyEmail(APPLICANT_EMAIL_ADDRESS).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .orgPolicyReference("CLAIMANTREF1")
                                              .organisation(Organisation.builder().organisationID("org1").build())
                                              .build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(APPLICANT_SOLICITOR_EMAIL_ADDRESS).build())
            .legacyCaseReference(REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.ENGLISH.toString())
            .build();

        when(notificationsProperties.getNotifyRespondentLipForClaimantRepresentedTemplate())
            .thenReturn(DEFENDANT_LIP_TEMPLATE);
        when(notificationsProperties.getNoticeOfChangeApplicantLipSolicitorTemplate())
            .thenReturn(APPLICANT_SOLICITOR_TEMPLATE);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().name("Civil - Organisation").build()));
    }

    @Test
    void shouldNotifyAllRelevantParties_WhenClaimantIsNotBilingual() {
        when(notificationsProperties.getNotifyClaimantLipForNoLongerAccessTemplate())
            .thenReturn(CLAIMANT_LIP_TEMPLATE);
        Set<EmailDTO> emailsToNotify = notifier.getPartiesToNotify(caseData);

        Set<EmailDTO> expectedEmailDTO = createExpectedEmailDTOForLip(caseData, false);
        assertThat(emailsToNotify.size()).isEqualTo(3);
        assertThat(emailsToNotify).containsAll(expectedEmailDTO);
    }


    @Test
    void shouldNotifyAllRelevantParties_WhenClaimantIsBilingual() {
        caseData = caseData.toBuilder()
            .claimantBilingualLanguagePreference(Language.WELSH.toString())
            .build();

        when(notificationsProperties.getNotifyClaimantLipForNoLongerAccessWelshTemplate())
            .thenReturn(CLAIMANT_LIP_WELSH_TEMPLATE);

        Set<EmailDTO> emailsToNotify = notifier.getPartiesToNotify(caseData);

        Set<EmailDTO> expectedEmailDTO = createExpectedEmailDTOForLip(caseData, true);
        assertThat(emailsToNotify.size()).isEqualTo(3);
        assertThat(emailsToNotify).containsAll(expectedEmailDTO);
    }

    private Set<EmailDTO> createExpectedEmailDTOForLip(CaseData caseData, boolean isWelsh) {
        Map<String, String> notifyLipProps = getPropertiesForLip();

        EmailDTO claimantLipWelsh = createEmailDTO(APPLICANT_EMAIL_ADDRESS,
                                                   isWelsh ? CLAIMANT_LIP_WELSH_TEMPLATE : CLAIMANT_LIP_TEMPLATE,
                                                   notifyLipProps);
        EmailDTO otherSolicitorLR = createEmailDTO(DEFENDANT_EMAIL_ADDRESS, DEFENDANT_LIP_TEMPLATE, notifyLipProps);
        EmailDTO newSolicitorLR = createEmailDTO(APPLICANT_SOLICITOR_EMAIL_ADDRESS, APPLICANT_SOLICITOR_TEMPLATE, getPropertiesForApplicantSolicitor());

        return Set.of(claimantLipWelsh, otherSolicitorLR, newSolicitorLR);
    }


    private EmailDTO createEmailDTO(String targetEmail, String template, Map<String, String> parameters) {
        return EmailDTO.builder()
            .targetEmail(targetEmail)
            .emailTemplate(template)
            .parameters(parameters)
            .reference(TEMPLATE_REFERENCE)
            .build();
    }


    public Map<String, String> getPropertiesForLip() {
        return Map.of(
            RESPONDENT_NAME, "ABC ABC",
            CLAIM_REFERENCE_NUMBER, REFERENCE_NUMBER,
            CLAIMANT_NAME, "Company Name"
        );
    }

    public Map<String, String> getPropertiesForApplicantSolicitor() {
        return Map.of(
            CLAIM_NUMBER, CASE_ID.toString(),
            CLAIMANT_V_DEFENDANT, "Company Name V ABC ABC",
            LEGAL_ORG_APPLICANT1, "Civil - Organisation",
            CLAIMANT_NAME, "Company Name",
            PARTY_REFERENCES, "Claimant reference: Not provided - Defendant reference: Not provided",
            CASEMAN_REF, REFERENCE_NUMBER
        );
    }
}
