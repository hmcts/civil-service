package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RespondToQueryClaimantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private RespondToQueryHelper respondToQueryHelper;

    @InjectMocks
    private RespondToQueryClaimantEmailDTOGenerator generator;

    @Test
    void shouldUseWelshTemplateWhenClaimantBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isClaimantBilingual()).thenReturn(true);
        when(notificationsProperties.getQueryLipWelshPublicResponseReceived()).thenReturn("welsh-id");

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo("welsh-id");
    }

    @Test
    void shouldUseEnglishTemplateWhenClaimantNotBilingual() {
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

        when(respondToQueryHelper.addLipOtherPartyProperties(properties, caseData, "Applicant Test"))
            .thenReturn(properties);

        generator.addCustomProperties(properties, caseData);

        verify(respondToQueryHelper).addLipOtherPartyProperties(properties, caseData, "Applicant Test");
        verify(respondToQueryHelper).addQueryDateProperty(properties, caseData);
    }

    @Test
    void getShouldNotifyShouldMatchLipClaimantRole() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        when(respondToQueryHelper.getResponseQueryContext(caseData))
            .thenReturn(Optional.of(context(CaseRole.CLAIMANT)));

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
