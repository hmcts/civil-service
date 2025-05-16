package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;

public class DefendantTwoEmailDTOGeneratorTest {

    private NotificationsProperties notificationsProperties;
    private DefendantTwoEmailDTOGenerator emailDTOGenerator;

    @BeforeEach
    void setUp() {
        emailDTOGenerator = new DefendantTwoEmailDTOGenerator(notificationsProperties) {
            @Override
            public String getEmailTemplateId(CaseData caseData) {
                return "template-id";
            }

            @Override
            protected String getReferenceTemplate() {
                return "reference-template";
            }
        };
    }

    @Test
    void shouldReturnCorrectEmailAddress() {
        String email = "test@example.com";
        CaseData caseData = CaseData.builder()
            .respondent2(Party.builder().partyEmail(email).build())
            .build();

        String outputEmail = emailDTOGenerator.getEmailAddress(caseData);
        assertThat(outputEmail).isEqualTo(email);
    }

    @Test
    void shouldAddCustomProperties() {
        Party party = Party.builder().build();
        CaseData caseData = CaseData.builder().respondent2(party).build();
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);

        String allPartyNames = "all party names";
        partyUtilsMockedStatic.when(() -> PartyUtils.getAllPartyNames(caseData)).thenReturn(allPartyNames);

        String partyName = "party name";
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(party, false)).thenReturn(partyName);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        partyUtilsMockedStatic.close();

        assertThat(updatedProperties.size()).isEqualTo(2);
        assertThat(updatedProperties).containsEntry(PARTY_NAME, partyName);
        assertThat(updatedProperties).containsEntry(CLAIMANT_V_DEFENDANT, allPartyNames);
    }

    @Test
    void shouldReturnNotifyAsTrueWhenRespondent2IsLip() {
        CaseData caseData = CaseData.builder().respondent2Represented(NO).build();
        Boolean shouldNotify = emailDTOGenerator.getShouldNotify(caseData);
        assertThat(shouldNotify).isTrue();
    }

    @Test
    void shouldReturnNotifyAsFalseWhenRespondent2Represented() {
        CaseData caseData = CaseData.builder().respondent2Represented(YES).build();
        Boolean shouldNotify = emailDTOGenerator.getShouldNotify(caseData);
        assertThat(shouldNotify).isFalse();
    }
}
