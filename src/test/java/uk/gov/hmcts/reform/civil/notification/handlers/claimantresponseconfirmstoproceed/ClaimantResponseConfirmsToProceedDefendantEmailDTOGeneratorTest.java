package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

public class ClaimantResponseConfirmsToProceedDefendantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private ClaimantResponseConfirmsToProceedDefendantEmailDTOGenerator emailDTOGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getRespondent1LipClaimUpdatedTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("claimant-confirms-to-proceed-respondent-notification-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = CaseData.builder().build();
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);

        String partyName = "party name";
        partyUtilsMockedStatic.when(() -> getPartyNameBasedOnType(any())).thenReturn(partyName);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        assertThat(updatedProperties.size()).isEqualTo(1);
        assertThat(updatedProperties).containsEntry(RESPONDENT_NAME, partyName);
    }
}
