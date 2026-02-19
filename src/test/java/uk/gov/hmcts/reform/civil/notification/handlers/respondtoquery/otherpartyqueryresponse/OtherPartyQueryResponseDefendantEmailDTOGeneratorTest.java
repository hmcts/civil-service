package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

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
import uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.RespondToQueryHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@ExtendWith(MockitoExtension.class)
class OtherPartyQueryResponseDefendantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private RespondToQueryHelper respondToQueryHelper;

    @InjectMocks
    private OtherPartyQueryResponseDefendantEmailDTOGenerator generator;

    @Test
    void shouldReturnWelshTemplateWhenRespondentBilingual() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        RespondentLiPResponse lipResponse = new RespondentLiPResponse();
        lipResponse.setRespondent1ResponseLanguage(Language.WELSH.toString());
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondent1LiPResponse(lipResponse);
        caseData.setCaseDataLiP(caseDataLiP);
        when(notificationsProperties.getQueryLipWelshPublicResponseReceived()).thenReturn("welsh-id");

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo("welsh-id");
    }

    @Test
    void shouldReturnEnglishTemplateWhenNotBilingual() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        when(notificationsProperties.getQueryLipPublicResponseReceived()).thenReturn("eng-id");

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo("eng-id");
    }

    @Test
    void shouldPopulatePartyDetailsAndDelegateToHelper() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setApplicant1(createParty("Applicant"));
        caseData.setRespondent1(createParty("Respondent"));
        Map<String, String> properties = new HashMap<>();

        generator.addCustomProperties(properties, caseData);

        assertThat(properties)
            .containsEntry(PARTY_NAME, "Respondent Test")
            .containsEntry(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
        verify(respondToQueryHelper).addCustomProperties(properties, caseData, "Respondent Test", true);
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
