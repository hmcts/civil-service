package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OTHER_SOL_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
class NoCLipClaimantRepresentedNotifierTest {

    @Mock
    NotificationsProperties notificationsProperties;

    @InjectMocks
    private NoCLipClaimantRepresentedNotifier notifier;

    private static final String DEFENDANT_EMAIL_ADDRESS = "defendantmail@hmcts.net";
    private static final String APPLICANT_EMAIL_ADDRESS = "applicantmail@hmcts.net";
    private static final String DEFENDANT_PARTY_NAME = "ABC ABC";
    private static final String REFERENCE_NUMBER = "8372942374";
    private static final String EMAIL_TEMPLATE = "test-notification-id";
    private static final String EMAIL_WELSH_TEMPLATE = "test-notification-welsh-id";
    private static final String CLAIMANT_ORG_NAME = "Org Name";
    private static final String APPLICANT_SOLICITOR_TEMPLATE = "applicant1-solicitor-id";
    public static final String TEMPLATE_REFERENCE = "notify-lip-after-noc-approval-000DC001";

    @Test
    void shouldNotifyAllRelevantParties_WhenClaimantIsNotBilingual() {
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                DEFENDANT_EMAIL_ADDRESS).build())
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME)
                            .partyEmail(APPLICANT_EMAIL_ADDRESS).build())
            .legacyCaseReference(REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.ENGLISH.toString())
            .build();
    }


    @Test
    void shouldNotifyAllRelevantParties_WhenClaimantIsNotBilingual() {
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                DEFENDANT_EMAIL_ADDRESS).build())
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME)
                            .partyEmail(APPLICANT_EMAIL_ADDRESS).build())
            .legacyCaseReference(REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.WELSH.toString())
            .build();

        when(notificationsProperties.getNotifyClaimantLipForNoLongerAccessWelshTemplate())
            .thenReturn(EMAIL_WELSH_TEMPLATE);

        Set<EmailDTO> emailsToNotify = notifier.getPartiesToNotify(caseData);

        Set<EmailDTO> expectedEmailDTO = createExpectedEmailDTO(caseData, true);
        assertThat(emailsToNotify.size()).isEqualTo(3);
        assertThat(emailsToNotify).containsAll(expectedEmailDTO);
    }

    private Set<EmailDTO> createExpectedEmailDTOForLip(CaseData caseData, boolean isWelsh) {
        Map<String, String> notifyClaimLipProps = getPropsForClaimantLip();

        Map<String, String> notifySolProperties = new HashMap<>(getNoCPropertiesForLR(caseData));
        notifySolProperties.put(OTHER_SOL_NAME, "LiP");
        notifySolProperties.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
        notifySolProperties.put(CASE_NAME, "Mr. John Rambo v Mr. Sole Trader");

        EmailDTO claimantLipWelsh = createEmailDTO("rambo@email.com",
                                                   isWelsh ? CLAIMANT_LIP_WELSH_TEMPLATE : CLAIMANT_LIP_TEMPLATE,
                                                   notifyClaimLipProps);
        EmailDTO otherSolicitorLR = createEmailDTO("previous-solicitor@example.com", PREVIOUS_SOL_TEMPLATE, notifySolProperties);
        EmailDTO newSolicitorLR = createEmailDTO("respondentsolicitor@example.com", NEW_SOL_TEMPLATE, notifySolProperties);

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


    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    public Map<String, String> addPropertiesApplicantSolicitor(CaseData caseData) {
        return Map.of(
            CLAIM_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData),
            LEGAL_ORG_APPLICANT1, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                                                               .getOrganisation().getOrganisationID()),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }
}
