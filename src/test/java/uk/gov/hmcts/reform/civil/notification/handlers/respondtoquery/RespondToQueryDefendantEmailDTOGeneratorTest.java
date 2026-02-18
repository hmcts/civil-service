package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@ExtendWith(MockitoExtension.class)
class RespondToQueryDefendantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private RespondToQueryDateHelper respondToQueryDateHelper;

    @InjectMocks
    private RespondToQueryDefendantEmailDTOGenerator generator;

    @Test
    void shouldUseWelshTemplateWhenRespondentBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isRespondentResponseBilingual()).thenReturn(true);
        when(notificationsProperties.getQueryLipWelshPublicResponseReceived()).thenReturn("welsh-id");

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo("welsh-id");
    }

    @Test
    void shouldUseEnglishTemplateWhenRespondentNotBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getQueryLipPublicResponseReceived()).thenReturn("english-id");

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo("english-id");
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo("response-to-query-notification-%s");
    }

    @Test
    void shouldPopulatePartyDetailsAndInvokeHelper() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setApplicant1(createParty("Applicant"));
        caseData.setRespondent1(createParty("Respondent"));
        Map<String, String> properties = new HashMap<>();

        generator.addCustomProperties(properties, caseData);

        assertThat(properties)
            .containsEntry(PARTY_NAME, "Respondent Test")
            .containsEntry(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
        verify(respondToQueryDateHelper).addQueryDateProperty(properties, caseData);
    }

    private Party createParty(String name) {
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        party.setIndividualFirstName(name);
        party.setIndividualLastName("Test");
        party.setPartyName(name);
        return party;
    }
}
