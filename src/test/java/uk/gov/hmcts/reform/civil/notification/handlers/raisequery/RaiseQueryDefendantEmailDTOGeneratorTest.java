package uk.gov.hmcts.reform.civil.notification.handlers.raisequery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RaiseQueryDefendantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private RaiseQueryDefendantEmailDTOGenerator emailDTOGenerator;

    @Test
    void shouldReturnCorrectEmailTemplate_whenDefendantIsBilingual() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseDataLip(new CaseDataLiP()
                             .setRespondent1LiPResponse(new RespondentLiPResponse()
                                                         .setRespondent1ResponseLanguage(Language.BOTH.toString())
                             )
            )
            .build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getQueryRaisedLipBilingual()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplate_whenDefendantIsNotBilingual() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getQueryRaisedLip()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("query-raised-notification-%s");
    }
}
