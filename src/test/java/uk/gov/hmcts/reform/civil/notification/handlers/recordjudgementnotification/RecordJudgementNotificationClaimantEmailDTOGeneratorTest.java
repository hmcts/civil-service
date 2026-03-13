package uk.gov.hmcts.reform.civil.notification.handlers.recordjudgementnotification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class RecordJudgementNotificationClaimantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private RecordJudgementNotificationClaimantEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnLipTemplateId() {
        CaseData caseData = new CaseDataBuilder().build();
        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("template-id");

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo("template-id");
    }

    @Test
    void shouldReturnBilingualTemplateIdWhenClaimantBilingual() {
        CaseData caseData = new CaseDataBuilder().claimantBilingualLanguagePreference(Language.BOTH.toString()).build();
        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn("template-id-welsh");

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo("template-id-welsh");
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
            .isEqualTo("record-judgment-determination-means-notification-%s");
    }
}
