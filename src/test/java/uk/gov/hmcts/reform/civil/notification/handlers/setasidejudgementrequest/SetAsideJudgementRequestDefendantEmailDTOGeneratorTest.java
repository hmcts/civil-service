package uk.gov.hmcts.reform.civil.notification.handlers.setasidejudgementrequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetAsideJudgementRequestDefendantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private SetAsideJudgementRequestDefendantEmailDTOGenerator generator;

    @Test
    void shouldReturnLipTemplate() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(notificationsProperties.getNotifyUpdateTemplate()).thenReturn("lip-template");

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo("lip-template");
        assertThat(generator.getReferenceTemplate()).isEqualTo("set-aside-judgment-defendant-notification-%s");
    }
}
