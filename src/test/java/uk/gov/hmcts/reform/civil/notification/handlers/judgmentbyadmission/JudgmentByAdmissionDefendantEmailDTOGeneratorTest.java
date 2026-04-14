package uk.gov.hmcts.reform.civil.notification.handlers.judgmentbyadmission;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JudgmentByAdmissionDefendantEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "defendant-judgment-by-admission-template-id";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private JudgmentByAdmissionDefendantEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo("defendant-judgment-by-admission-%s");
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(notificationsProperties.getNotifyDefendantLIPJudgmentByAdmissionTemplate()).thenReturn(TEMPLATE_ID);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnRespondent1EmailAddress() {
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(new Party().setPartyEmail("defendant@example.com"))
            .build();

        assertThat(generator.getEmailAddress(caseData)).isEqualTo("defendant@example.com");
    }
}

