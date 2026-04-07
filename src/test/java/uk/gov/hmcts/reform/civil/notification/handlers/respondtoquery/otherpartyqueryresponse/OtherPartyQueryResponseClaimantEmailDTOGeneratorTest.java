package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.RespondToQueryHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtherPartyQueryResponseClaimantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private RespondToQueryHelper respondToQueryHelper;

    @InjectMocks
    private OtherPartyQueryResponseClaimantEmailDTOGenerator generator;

    @Test
    void shouldReturnWelshTemplateWhenClaimantBilingual() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setClaimantBilingualLanguagePreference(Language.WELSH.toString());
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

        when(respondToQueryHelper.addLipOtherPartyProperties(properties, caseData, "Applicant Test"))
            .thenReturn(properties);

        generator.addCustomProperties(properties, caseData);

        verify(respondToQueryHelper).addLipOtherPartyProperties(properties, caseData, "Applicant Test");
    }

    @Test
    void getShouldNotifyShouldMatchRespondentRoles() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setApplicant1Represented(YesOrNo.NO);
        when(respondToQueryHelper.getResponseQueryContext(caseData))
            .thenReturn(Optional.of(context(CaseRole.RESPONDENTSOLICITORONE)));

        assertThat(generator.getShouldNotify(caseData)).isTrue();

        when(respondToQueryHelper.getResponseQueryContext(caseData))
            .thenReturn(Optional.of(context(CaseRole.APPLICANTSOLICITORONE)));

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    private Party createParty(String name) {
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        party.setIndividualFirstName(name);
        party.setIndividualLastName("Test");
        party.setPartyName(name);
        return party;
    }

    private RespondToQueryHelper.ResponseQueryContext context(CaseRole role) {
        CaseMessage parent = new CaseMessage();
        parent.setId("parent");
        CaseMessage response = new CaseMessage();
        response.setParentId("parent");
        return new RespondToQueryHelper.ResponseQueryContext(parent, response, List.of(role.getFormattedName()));
    }
}
