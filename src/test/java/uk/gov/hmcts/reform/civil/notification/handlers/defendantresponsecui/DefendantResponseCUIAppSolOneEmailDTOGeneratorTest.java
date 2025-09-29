package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponsecui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

public class DefendantResponseCUIAppSolOneEmailDTOGeneratorTest {

    @InjectMocks
    private DefendantResponseCUIAppSolOneEmailDTOGenerator emailDTOGenerator;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenNotFullDefence() {
        CaseData caseData = CaseData.builder().respondent1ClaimResponseTypeForSpec(PART_ADMISSION).build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getRespondentLipFullAdmitOrPartAdmitTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenFullDefenceAndResponseClaimMediationSpecRequired() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .responseClaimMediationSpecRequired(YES)
            .build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getRespondentLipFullDefenceWithMediationTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenFullDefenceAndNoResponseClaimMediationSpecRequired() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .responseClaimMediationSpecRequired(NO)
            .build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getRespondentLipFullDefenceNoMediationTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("defendant-response-applicant-notification-%s");
    }

    @Test
    void shouldReturnCorrectCustomProperties() {
        Party party = Party.builder().build();
        CaseData caseData = CaseData.builder().respondent1(party).build();

        String legalOrg = "legal org";
        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn(legalOrg);

        String partyName = "party name";
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(party)).thenReturn(partyName);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> actualResults = emailDTOGenerator.addCustomProperties(properties, caseData);

        notificationUtilsMockedStatic.close();
        partyUtilsMockedStatic.close();

        assertThat(actualResults.size()).isEqualTo(2);
        assertThat(actualResults).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, legalOrg);
        assertThat(actualResults).containsEntry(RESPONDENT_NAME, partyName);
    }

    @Test
    void shouldNotifyWhenApplicantIsRepresentedAndRespondentResponseIsNotBilingual() {
        CaseData caseData = CaseData.builder().applicant1Represented(YES).build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldNotNotifyWhenRespondentResponseIsBilingual() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YES)
            .caseDataLiP(
                CaseDataLiP.builder()
                    .respondent1LiPResponse(
                        RespondentLiPResponse.builder()
                            .respondent1ResponseLanguage(BOTH.toString())
                            .build()
                    ).build()
            ).build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotifyWhenApplicantIsNotRepresented() {
        CaseData caseData = CaseData.builder().applicant1Represented(NO).build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotifyWhenApplicantIsNotRepresentedAndRespondentResponseIsBilingual() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(NO)
            .caseDataLiP(
                CaseDataLiP.builder()
                    .respondent1LiPResponse(
                        RespondentLiPResponse.builder()
                            .respondent1ResponseLanguage(BOTH.toString())
                            .build()
                    ).build()
            ).build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }
}
