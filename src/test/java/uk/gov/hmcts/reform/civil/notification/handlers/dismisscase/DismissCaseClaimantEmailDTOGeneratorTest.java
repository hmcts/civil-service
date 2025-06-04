package uk.gov.hmcts.reform.civil.notification.handlers.dismisscase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

public class DismissCaseClaimantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private DismissCaseClaimantEmailDTOGenerator emailDTOGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("dismiss-case-claimant-notification-%s");
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenBilingual() {
        CaseData caseData = CaseData.builder().claimantBilingualLanguagePreference(BOTH.toString()).build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenNotBilingual() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldAddCustomProperties() {
        Party party = Party.builder().build();
        CaseData caseData = CaseData.builder().applicant1(party).build();

        String allPartyNames = "all party names";
        String applicantName = "applicant name";
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);
        partyUtilsMockedStatic.when(() -> getAllPartyNames(any())).thenReturn(allPartyNames);
        partyUtilsMockedStatic.when(() -> getPartyNameBasedOnType(party, false)).thenReturn(applicantName);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        partyUtilsMockedStatic.close();

        assertThat(updatedProperties.size()).isEqualTo(2);
        assertThat(updatedProperties).containsEntry(PARTY_NAME, applicantName);
        assertThat(updatedProperties).containsEntry(CLAIMANT_V_DEFENDANT, allPartyNames);
    }

}
