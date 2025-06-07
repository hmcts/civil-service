package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolipvlrorlrvliptolrvlr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

@ExtendWith(MockitoExtension.class)
class DefRepresentedDefendantLipEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private DefRepresentedDefendantLipEmailDTOGenerator generator;

    private CaseData baseCaseData;

    @BeforeEach
    void setup() {
        baseCaseData = CaseData.builder()
            .ccdCaseReference(1234567890123456L)
            .legacyCaseReference("LEGACY123456")
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mrs")
                             .individualFirstName("Jane")
                             .individualLastName("Defendant")
                             .partyName("Jane Defendant").build())
            .build();
    }

    @Test
    void shouldReturnBilingualTemplateIdIfRespondentResponseIsBilingual() {
        CaseData bilingualCaseData = baseCaseData.toBuilder()
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1ResponseLanguage(Language.WELSH.toString()).build())
                             .build())
            .build();

        when(notificationsProperties.getNotifyDefendantLipBilingualAfterDefendantNOC())
            .thenReturn("bilingual-template-id");

        String result = generator.getEmailTemplateId(bilingualCaseData);

        assertThat(result).isEqualTo("bilingual-template-id");
    }

    @Test
    void shouldReturnDefaultTemplateIdIfRespondentResponseIsNotBilingual() {
        CaseData monoCaseData = baseCaseData.toBuilder()
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1ResponseLanguage(Language.ENGLISH.toString()).build())
                             .build())
            .build();

        when(notificationsProperties.getNotifyDefendantLipForNoLongerAccessTemplate())
            .thenReturn("default-template-id");

        String result = generator.getEmailTemplateId(monoCaseData);

        assertThat(result).isEqualTo("default-template-id");
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
            .isEqualTo("notify-lip-after-defendant-noc-approval-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> result = generator.addCustomProperties(new HashMap<>(), baseCaseData);

        assertThat(result)
            .containsEntry(RESPONDENT_NAME, "Mrs Jane Defendant")
            .containsEntry(CLAIM_16_DIGIT_NUMBER, "1234567890123456")
            .containsEntry(CLAIM_NUMBER, "LEGACY123456");
    }
}
