package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.offline.counterclaimordivergedresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class SpecCaseOfflineHelperTest {

    private NotificationsProperties notificationsProperties;
    private SpecCaseOfflineHelper helper;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        helper = new SpecCaseOfflineHelper(notificationsProperties);
    }

    @Test
    void shouldReturnBilingualTemplate_whenClaimantIsBilingual() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.BOTH.toString())
            .build();
        when(notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate()).thenReturn("bilingual-template");

        String result = helper.getClaimantTemplateForLipVLRSpecClaims(caseData);

        assertThat(result).isEqualTo("bilingual-template");
    }

    @Test
    void shouldReturnCounterClaimTemplate_when1v1AndCounterClaim() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .respondent2(null)
            .respondentResponseIsSame(YES)
            .build();

        when(notificationsProperties.getClaimantSolicitorCounterClaimForSpec()).thenReturn("counter-claim-template");

        String result = helper.getApplicantTemplateForSpecClaims(caseData);

        assertThat(result).isEqualTo("counter-claim-template");
    }

    @Test
    void shouldReturn1v1ResponseTemplate_whenIs1v1() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondent2()).thenReturn(null);
        when(caseData.getRespondentResponseIsSame()).thenReturn(null);
        when(caseData.getRespondent1()).thenReturn(Party.builder()
                                                       .type(Party.Type.INDIVIDUAL)
                                                       .individualFirstName("John")
                                                       .individualLastName("Doe").build());
        when(caseData.getApplicant1()).thenReturn(Party.builder().partyName("Claimant 1").build());

        when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline()).thenReturn("offline-template");

        String result = helper.getApplicantTemplateForSpecClaims(caseData);

        assertThat(result).isEqualTo("offline-template");
    }

    @Test
    void shouldReturnRespondentCounterClaimTemplate_whenCounterClaimAndSingleResponse() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .respondent2(null)
            .respondentResponseIsSame(YES)
            .build();

        when(notificationsProperties.getRespondentSolicitorCounterClaimForSpec()).thenReturn("resp-counter-claim-template");

        String result = helper.getRespTemplateForSpecClaims(caseData);

        assertThat(result).isEqualTo("resp-counter-claim-template");
    }

    @Test
    void shouldReturnRespondentDefaultTemplate_whenNotCounterClaim() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2(null)
            .build();

        when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec()).thenReturn("default-template");

        String result = helper.getRespTemplateForSpecClaims(caseData);

        assertThat(result).isEqualTo("default-template");
    }

    @Test
    void shouldReturnMapFor1v1CaseInNotificationProperties() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        Map<String, String> result = SpecCaseOfflineHelper.caseOfflineNotificationProperties(caseData);

        assertThat(result).containsEntry("reason", "Defends all of the claim");
    }
}
