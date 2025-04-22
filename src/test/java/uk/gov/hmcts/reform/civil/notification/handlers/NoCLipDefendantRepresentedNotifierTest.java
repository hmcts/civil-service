package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;

@ExtendWith(MockitoExtension.class)
class NoCLipDefendantRepresentedNotifierTest {

    public static final Long CASE_ID = 1594901956117591L;

    @Mock
    NotificationsProperties notificationsProperties;

    @Mock
    OrganisationService organisationService;

    @InjectMocks
    private NoCLipDefendantRepresentedNotifier notifier;

    private CaseData caseData;

    EmailDTO claimantLR, claimantLip, defendantLR, defendantLip;


    private static final String DEFENDANT_EMAIL_ADDRESS = "defendantmail@hmcts.net";
    private static final String CLAIMANT_EMAIL_ADDRESS = "applicantmail@hmcts.net";
    private static final String DEFENDANT_SOLICITOR_EMAIL_ADDRESS = "defendantSolicitorMail@hmcts.net";
    private static final String DEFENDANT_PARTY_NAME = "ABC ABC";
    private static final String CLAIMANT_COMPANY_NAME = "Company Name";
    private static final String DEFENDANT_ORG_NAME = "Org Name";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final String CLAIMANT_LIP_TEMPLATE = "claimant-lip-template-id";
    private static final String CLAIMANT_LIP_WELSH_TEMPLATE = "claimant-lip-welsh-template-id";
    private static final String DEFENDANT_LIP_TEMPLATE = "defendant-lip-template-id";
    private static final String DEFENDANT_LIP_WELSH_TEMPLATE = "defendant-lip-welsh-template-id";
    private static final String DEFENDANT_LR_TEMPLATE = "defendant-lr-template-id";
    private static final String CLAIMANT_LR_TEMPLATE = "claimant-lr-template-id";

    @BeforeEach
    public void setup() {
        caseData = CaseData.builder()
            .ccdCaseReference(CASE_ID)
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                DEFENDANT_EMAIL_ADDRESS).build())
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_COMPANY_NAME)
                            .partyEmail(CLAIMANT_EMAIL_ADDRESS).build())
            .respondentSolicitor1EmailAddress(DEFENDANT_SOLICITOR_EMAIL_ADDRESS)
            .changeOfRepresentation(ChangeOfRepresentation.builder().organisationToAddID(DEFENDANT_ORG_NAME)
                                        .caseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()).build())
            .legacyCaseReference(REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .issueDate(now())
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1ResponseLanguage(Language.ENGLISH.toString())
                                                          .build())
                             .build())
            .claimantBilingualLanguagePreference(Language.ENGLISH.toString()).build();


        when(notificationsProperties.getNotifyDefendantLrAfterNoticeOfChangeTemplate())
            .thenReturn(DEFENDANT_LR_TEMPLATE);
       // when(notificationsProperties.getNoticeOfChangeOtherParties()).thenReturn(CLAIMANT_LR_TEMPLATE);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().name("Civil - Organisation").build()));

        claimantLR = EmailDTO.builder()
            .targetEmail("applicantsolicitor@example.com")
            .emailTemplate("template-id")
            .parameters(Map.of(CLAIM_NUMBER, REFERENCE_NUMBER,
                               RESPONDENT_NAME, DEFENDANT_PARTY_NAME,
                               CLAIM_16_DIGIT_NUMBER, CASE_ID.toString()))
            .reference("")
            .build();

        claimantLip = EmailDTO.builder()
            .targetEmail(CLAIMANT_EMAIL_ADDRESS)
            .emailTemplate(CLAIMANT_LIP_TEMPLATE)
            .parameters(Map.of(CLAIM_NUMBER, REFERENCE_NUMBER,
                               DEFENDANT_NAME_INTERIM, DEFENDANT_PARTY_NAME,
                               CLAIM_16_DIGIT_NUMBER, CASE_ID.toString(),
                               CLAIMANT_NAME, CLAIMANT_COMPANY_NAME))
            .reference("notify-claimant-lip-after-defendant-noc-approval-000DC001")
            .build();

        defendantLR = EmailDTO.builder()
            .targetEmail(DEFENDANT_SOLICITOR_EMAIL_ADDRESS)
            .emailTemplate(DEFENDANT_LR_TEMPLATE)
            .parameters(Map.of(CASEMAN_REF, REFERENCE_NUMBER,
                               DEFENDANT_NAME, DEFENDANT_PARTY_NAME,
                                   CLAIM_16_DIGIT_NUMBER, CASE_ID.toString(),
                                   LEGAL_REP_NAME, "Civil - Organisation",
                                   PARTY_REFERENCES, "Claimant reference: Not provided - Defendant reference: Not provided"))
            .reference("notify-lr-after-defendant-noc-approval-000DC001")
            .build();

        defendantLip = EmailDTO.builder()
            .targetEmail(DEFENDANT_EMAIL_ADDRESS)
            .emailTemplate(DEFENDANT_LIP_TEMPLATE)
            .parameters(Map.of(CLAIM_NUMBER, REFERENCE_NUMBER,
                               RESPONDENT_NAME, DEFENDANT_PARTY_NAME,
                               CLAIM_16_DIGIT_NUMBER, CASE_ID.toString()))
            .reference("notify-lip-after-defendant-noc-approval-000DC001")
            .build();
    }

    @Test
    void shouldNotifyAllRelevantParties_WhenClaimantIsNotBilingual() {
        when(notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate())
            .thenReturn(CLAIMANT_LIP_TEMPLATE);
        when(notificationsProperties.getNotifyDefendantLipForNoLongerAccessTemplate())
            .thenReturn(DEFENDANT_LIP_TEMPLATE);
        Set<EmailDTO> emailsToNotify = notifier.getPartiesToNotify(caseData);

        final Set<EmailDTO> expectedEmail = Set.of(claimantLip, defendantLip, defendantLR);

        assertThat(emailsToNotify.size()).isEqualTo(3);
        assertThat(emailsToNotify).containsAll(expectedEmail);
    }

    @Test
    void shouldNotifyAllRelevantParties_WhenClaimantIsBilingual() {
        when(notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC())
            .thenReturn(CLAIMANT_LIP_WELSH_TEMPLATE);
        when(notificationsProperties.getNotifyDefendantLipBilingualAfterDefendantNOC())
            .thenReturn(DEFENDANT_LIP_WELSH_TEMPLATE);
    }

    @Test
    void shouldNotifyAllRelevantParties_WhenDefendantIsNotBilingual() {
        when(notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate())
            .thenReturn(CLAIMANT_LIP_TEMPLATE);
        when(notificationsProperties.getNotifyDefendantLipForNoLongerAccessTemplate())
            .thenReturn(DEFENDANT_LIP_TEMPLATE);
    }

    @Test
    void shouldNotifyAllRelevantParties_WhenDefendantIsBilingual() {
        when(notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate())
            .thenReturn(CLAIMANT_LIP_TEMPLATE);
        when(notificationsProperties.getNotifyDefendantLipBilingualAfterDefendantNOC())
            .thenReturn(DEFENDANT_LIP_WELSH_TEMPLATE);
    }

}
