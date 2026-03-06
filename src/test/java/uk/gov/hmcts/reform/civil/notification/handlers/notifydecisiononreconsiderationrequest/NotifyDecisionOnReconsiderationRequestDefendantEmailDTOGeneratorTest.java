package uk.gov.hmcts.reform.civil.notification.handlers.notifydecisiononreconsiderationrequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getClaimantVDefendant;

@ExtendWith(MockitoExtension.class)
class NotifyDecisionOnReconsiderationRequestDefendantEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "templateId";
    private static final String REFERENCE_TEMPLATE = "reconsideration-upheld-applicant-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    private NotifyDecisionOnReconsiderationRequestDefendantEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new NotifyDecisionOnReconsiderationRequestDefendantEmailDTOGenerator(notificationsProperties);
    }

    @Test
    void shouldReturnTemplateId() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getNotifyClaimReconsiderationLRTemplate()).thenReturn(TEMPLATE_ID);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo(REFERENCE_TEMPLATE);
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        Map<String, String> properties = new HashMap<>();

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result)
            .containsEntry(CLAIMANT_V_DEFENDANT, getClaimantVDefendant(caseData))
            .containsEntry(PARTY_NAME, caseData.getRespondent1().getPartyName());
    }

    @Test
    void shouldNotifyForRespondentLipWithEmail() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .respondent1(PartyBuilder.builder().individual().partyEmail("defendant@example.com").build())
            .build();

        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldNotNotifyWhenEmailMissing() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .respondent1(PartyBuilder.builder().individual().partyEmail(null).build())
            .build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotifyWhenRespondentRepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .respondent1Represented(YesOrNo.YES)
            .respondent1(PartyBuilder.builder().individual().partyEmail("defendant@example.com").build())
            .build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }
}
