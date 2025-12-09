package uk.gov.hmcts.reform.civil.notification.handlers.raisequery.otherpartyqueryraised;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtherPartyQueryRaisedRespSolOneEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private OtherPartyQueryRaisedRespSolOneEmailDTOGenerator emailDTOGenerator;

    @Test
    void shouldReturnCorrectEmailTemplate() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getNotifyOtherPartyPublicQueryRaised()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("a-query-has-been-raised-notification-%s");
    }
}
