package uk.gov.hmcts.reform.civil.notification.handlers.discontinueclaimparties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

class DiscontinueClaimPartiesDefendantEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";

    @Mock
    private NotificationsProperties notificationsProperties;

    private DiscontinueClaimPartiesDefendantEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new DiscontinueClaimPartiesDefendantEmailDTOGenerator(notificationsProperties);
    }

    @Test
    void shouldReturnEmailTemplateId() {
        when(notificationsProperties.getNotifyClaimDiscontinuedLipTemplate()).thenReturn(TEMPLATE_ID);

        CaseData caseData = CaseData.builder().build();
        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String result = generator.getReferenceTemplate();

        assertThat(result).isEqualTo("defendant-claim-discontinued-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> properties = new HashMap<>();
        Party respondent1 = mock(Party.class);
        when(respondent1.getPartyName()).thenReturn("Respondent1 Name");
        CaseData caseData = mock(CaseData.class);

        when(caseData.getRespondent1()).thenReturn(respondent1);

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result.get(RESPONDENT_NAME)).isEqualTo("Respondent1 Name");
    }
}
