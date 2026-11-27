package uk.gov.hmcts.reform.civil.notification.handlers.breathingspace;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

class BreathingSpaceLipEmailDTOGeneratorsTest {

    private static final String CLAIMANT_EMAIL = "lip.claimant@example.com";
    private static final String DEFENDANT_EMAIL = "lip.defendant@example.com";
    private static final String CLAIMANT_NAME_VALUE = "Claimant Ltd";
    private static final String DEFENDANT_NAME_VALUE = "Defendant Ltd";

    private final NotificationsProperties templates = mock(NotificationsProperties.class, invocation ->
        invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName()
            : RETURNS_DEFAULTS.answer(invocation));
    private final NotificationsSignatureConfiguration signatures = mock(NotificationsSignatureConfiguration.class, invocation ->
        invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName()
            : RETURNS_DEFAULTS.answer(invocation));
    private final FeatureToggleService toggles = mock(FeatureToggleService.class);
    private final OrganisationService organisations = mock(OrganisationService.class);
    private final BreathingSpaceEmailHelper helper = new BreathingSpaceEmailHelper(organisations, signatures, toggles);

    @ParameterizedTest
    @CsvSource(value = {
        "true,  NO,  lip.claimant@example.com, true",
        "true,  YES, lip.claimant@example.com, false",
        "true,  NO,  '',                       false",
        "true,  NO,  NULL,                     false",
        "false, NO,  lip.claimant@example.com, true",
        "false, YES, lip.claimant@example.com, false",
        "false, NO,  '',                       false",
        "false, NO,  NULL,                     false"
    }, nullValues = "NULL")
    void shouldOnlyNotifyLipClaimantWithAnEmailAddress(boolean enter, YesOrNo represented, String email, boolean expected) {
        CaseData data = lipCaseData().toBuilder()
            .applicant1Represented(represented)
            .applicant1(claimant(email))
            .build();

        assertThat(claimantGenerator(enter).getShouldNotify(data)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "true,  NO,  lip.defendant@example.com, true",
        "true,  YES, lip.defendant@example.com, false",
        "true,  NO,  '',                        false",
        "true,  NO,  NULL,                      false",
        "false, NO,  lip.defendant@example.com, true",
        "false, YES, lip.defendant@example.com, false",
        "false, NO,  '',                        false",
        "false, NO,  NULL,                      false"
    }, nullValues = "NULL")
    void shouldOnlyNotifyLipDefendantWithAnEmailAddress(boolean enter, YesOrNo represented, String email, boolean expected) {
        CaseData data = lipCaseData().toBuilder()
            .respondent1Represented(represented)
            .respondent1(defendant(email))
            .build();

        assertThat(defendantGenerator(enter).getShouldNotify(data)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotFallBackToClaimantIdamEmail(boolean enter) {
        // Old handler used applicant1.partyEmail, without the override so the base generator would fall back to claimantUserDetails
        CaseData data = lipCaseData().toBuilder()
            .applicant1(claimant(null))
            .claimantUserDetails(new IdamUserDetails().setEmail("idam@example.com"))
            .build();

        assertThat(claimantGenerator(enter).getShouldNotify(data)).isFalse();
    }

    @ParameterizedTest
    @CsvSource(value = {
        "true,  NULL,    getNotifyApplicant1EnteredBreathingSpaceLip",
        "true,  ENGLISH, getNotifyApplicant1EnteredBreathingSpaceLip",
        "true,  WELSH,   getNotifyApplicant1EnteredBreathingSpaceLipWelsh",
        "true,  BOTH,    getNotifyApplicant1EnteredBreathingSpaceLipWelsh",
        "false, NULL,    getNotifyLiPApplicantBreathingSpaceLifted",
        "false, ENGLISH, getNotifyLiPApplicantBreathingSpaceLifted",
        "false, WELSH,   getNotifyLiPApplicantBreathingSpaceLiftedWelsh",
        "false, BOTH,    getNotifyLiPApplicantBreathingSpaceLiftedWelsh"
    }, nullValues = "NULL")
    void shouldSelectClaimantTemplateByClaimantLanguage(boolean enter, String language, String expectedTemplate) {
        CaseData data = lipCaseData().toBuilder().claimantBilingualLanguagePreference(language).build();

        assertThat(claimantGenerator(enter).buildEmailDTO(data, taskId(enter)).getEmailTemplate())
            .isEqualTo(expectedTemplate);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "true,  NULL,    getNotifyEnteredBreathingSpaceForDefendantLip",
        "true,  ENGLISH, getNotifyEnteredBreathingSpaceForDefendantLip",
        "true,  WELSH,   getNotifyEnteredBreathingSpaceForDefendantLipWelsh",
        "true,  BOTH,    getNotifyEnteredBreathingSpaceForDefendantLipWelsh",
        "false, NULL,    getNotifyLiPRespondentBreathingSpaceLifted",
        "false, ENGLISH, getNotifyLiPRespondentBreathingSpaceLifted",
        "false, WELSH,   getNotifyLiPRespondentBreathingSpaceLiftedWelsh",
        "false, BOTH,    getNotifyLiPRespondentBreathingSpaceLiftedWelsh"
    }, nullValues = "NULL")
    void shouldSelectDefendantTemplateByDefendantResponseLanguage(boolean enter, String language, String expectedTemplate) {
        CaseData data = lipCaseData().toBuilder()
            .caseDataLiP(new CaseDataLiP().setRespondent1LiPResponse(
                new RespondentLiPResponse().setRespondent1ResponseLanguage(language)))
            .build();

        assertThat(defendantGenerator(enter).buildEmailDTO(data, taskId(enter)).getEmailTemplate())
            .isEqualTo(expectedTemplate);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldBuildClaimantEmail(boolean enter) {
        CaseData data = lipCaseData();

        EmailDTO email = claimantGenerator(enter).buildEmailDTO(data, taskId(enter));

        assertThat(email.getTargetEmail()).isEqualTo(CLAIMANT_EMAIL);
        assertThat(email.getReference()).isEqualTo(expectedReference(enter, data));
        assertThat(email.getParameters())
            .containsEntry(CLAIM_REFERENCE_NUMBER, data.getCcdCaseReference().toString())
            .containsEntry(CLAIMANT_NAME, CLAIMANT_NAME_VALUE)
            .containsEntry(RESPONDENT_NAME, DEFENDANT_NAME_VALUE)
            .containsEntry(PARTY_NAME, CLAIMANT_NAME_VALUE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldBuildDefendantEmail(boolean enter) {
        CaseData data = lipCaseData();

        EmailDTO email = defendantGenerator(enter).buildEmailDTO(data, taskId(enter));

        assertThat(email.getTargetEmail()).isEqualTo(DEFENDANT_EMAIL);
        assertThat(email.getReference()).isEqualTo(expectedReference(enter, data));
        assertThat(email.getParameters())
            .containsEntry(CLAIM_REFERENCE_NUMBER, data.getCcdCaseReference().toString())
            .containsEntry(CLAIMANT_NAME, CLAIMANT_NAME_VALUE)
            .containsEntry(RESPONDENT_NAME, DEFENDANT_NAME_VALUE)
            .containsEntry(PARTY_NAME, DEFENDANT_NAME_VALUE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotifyOnlyTheLipsWhenBothPartiesAreUnrepresented(boolean enter) {
        CaseData data = lipCaseData();

        Set<EmailDTO> emails = allPartiesGenerator(enter).getPartiesToNotify(data, taskId(enter));

        assertThat(emails).extracting(EmailDTO::getTargetEmail)
            .containsExactlyInAnyOrder(CLAIMANT_EMAIL, DEFENDANT_EMAIL);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotifyApplicantSolicitorAndLipDefendantWhenOnlyDefendantIsUnrepresented(boolean enter) {
        CaseData data = lipCaseData().toBuilder()
            .applicant1Represented(YesOrNo.YES)
            .build();

        Set<EmailDTO> emails = allPartiesGenerator(enter).getPartiesToNotify(data, taskId(enter));

        assertThat(emails).extracting(EmailDTO::getTargetEmail)
            .containsExactlyInAnyOrder(data.getApplicantSolicitor1UserDetails().getEmail(), DEFENDANT_EMAIL);
    }

    private EmailDTOGenerator<CaseData> claimantGenerator(boolean enter) {
        return enter
            ? new BreathingSpaceEnterClaimantEmailDTOGenerator(helper, templates)
            : new BreathingSpaceLiftedClaimantEmailDTOGenerator(helper, templates);
    }

    private EmailDTOGenerator<CaseData> defendantGenerator(boolean enter) {
        return enter
            ? new BreathingSpaceEnterDefendantEmailDTOGenerator(helper, templates)
            : new BreathingSpaceLiftedDefendantEmailDTOGenerator(helper, templates);
    }

    private AllPartiesEmailGenerator allPartiesGenerator(boolean enter) {
        SimpleStateFlowEngine engine = mock(SimpleStateFlowEngine.class);
        when(engine.evaluate(any(CaseData.class))).thenReturn(mock(StateFlow.class));
        return enter
            ? new BreathingSpaceEnterAllPartiesEmailGenerator(
                new BreathingSpaceEnterAppSolOneEmailDTOGenerator(organisations, helper, templates),
                new BreathingSpaceEnterRespSolOneEmailDTOGenerator(organisations, helper, templates),
                new BreathingSpaceEnterRespSolTwoEmailDTOGenerator(organisations, helper, templates, engine),
                new BreathingSpaceEnterClaimantEmailDTOGenerator(helper, templates),
                new BreathingSpaceEnterDefendantEmailDTOGenerator(helper, templates))
            : new BreathingSpaceLiftedAllPartiesEmailGenerator(
                new BreathingSpaceLiftedAppSolOneEmailDTOGenerator(organisations, helper, templates),
                new BreathingSpaceLiftedRespSolOneEmailDTOGenerator(organisations, helper, templates),
                new BreathingSpaceLiftedRespSolTwoEmailDTOGenerator(organisations, helper, templates, engine),
                new BreathingSpaceLiftedClaimantEmailDTOGenerator(helper, templates),
                new BreathingSpaceLiftedDefendantEmailDTOGenerator(helper, templates));
    }

    private static String taskId(boolean enter) {
        return enter ? "BreathingSpaceEnterNotifyParties" : "BreathingSpaceLiftedNotifyParties";
    }

    private static String expectedReference(boolean enter, CaseData data) {
        return (enter ? "breathing-space-enter-lip-notification-" : "breathing-space-lifted-lip-notification-")
            + data.getLegacyCaseReference();
    }

    private CaseData lipCaseData() {
        return CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .applicant1(claimant(CLAIMANT_EMAIL))
            .respondent1(defendant(DEFENDANT_EMAIL))
            .respondent2(null)
            .build();
    }

    private static Party claimant(String email) {
        return new Party().setType(Party.Type.COMPANY).setCompanyName(CLAIMANT_NAME_VALUE).setPartyEmail(email);
    }

    private static Party defendant(String email) {
        return new Party().setType(Party.Type.COMPANY).setCompanyName(DEFENDANT_NAME_VALUE).setPartyEmail(email);
    }
}
