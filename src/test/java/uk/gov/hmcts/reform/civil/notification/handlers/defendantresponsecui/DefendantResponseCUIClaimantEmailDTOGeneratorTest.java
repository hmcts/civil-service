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
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

public class DefendantResponseCUIClaimantEmailDTOGeneratorTest {

    @InjectMocks
    private DefendantResponseCUIClaimantEmailDTOGenerator emailDTOGenerator;

    @Mock
    private NotificationsProperties notificationsProperties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplate() {
        CaseData caseData = CaseData.builder().build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getNotifyLiPClaimantDefendantResponded()).thenReturn(expectedTemplateId);

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
        String legacyCaseReference = "legacy case reference";
        CaseData caseData = CaseData.builder()
            .legacyCaseReference(legacyCaseReference)
            .applicant1(party)
            .respondent1(party)
            .build();

        String partyName = "party name";
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(party)).thenReturn(partyName);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> actualResults = emailDTOGenerator.addCustomProperties(properties, caseData);

        partyUtilsMockedStatic.close();

        assertThat(actualResults.size()).isEqualTo(3);
        assertThat(actualResults).containsEntry(CLAIM_REFERENCE_NUMBER, legacyCaseReference);
        assertThat(actualResults).containsEntry(RESPONDENT_NAME, partyName);
        assertThat(actualResults).containsEntry(CLAIMANT_NAME, partyName);
    }

    @Test
    void shouldNotifyWhenApplicantIsLipAndRespondentResponseNotBilingual() {
        CaseData caseData = CaseData.builder().applicant1Represented(NO).build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldNotNotifyWhenRespondentResponseIsBilingual() {
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

    @Test
    void shouldNotNotifyWhenApplicantIsNotLip() {
        CaseData caseData = CaseData.builder().applicant1Represented(YES).build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotifyWhenApplicantIsNotLipAndRespondentResponseIsBilingual() {
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
}
