package uk.gov.hmcts.reform.civil.notification.handlers.extendresponsedeadline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
public class ExtendResponseDeadlineClaimantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private PinInPostConfiguration pipInPostConfiguration;

    @InjectMocks
    private ExtendResponseDeadlineClaimantEmailDTOGenerator emailDTOGenerator;

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenBilingual() {
        CaseData caseData = CaseData.builder().claimantBilingualLanguagePreference(BOTH.toString()).build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getClaimantLipDeadlineExtensionWelsh()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenNotBilingual() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getClaimantLipDeadlineExtension()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldAddCustomProperties() {
        Party claimant = Party.builder().companyName("Claimant Name").type(Party.Type.COMPANY).build();
        Party defendant = Party.builder().companyName("Defendant Name").type(Party.Type.COMPANY).build();
        LocalDateTime responseDeadline = LocalDateTime.of(2025, 6, 20, 0, 0);
        CaseData caseData = CaseData.builder()
            .applicant1(claimant)
            .respondent1(defendant)
            .respondent1ResponseDeadline(responseDeadline)
            .build();

        String claimantName = "Claimant Name";
        String defendantName = "Defendant Name";
        String frontEndUrl = "http://frontend.url";
        String formattedDate = "20 June 2025";

        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);
        MockedStatic<DateFormatHelper> dateFormatHelperMockedStatic = Mockito.mockStatic(DateFormatHelper.class);
        partyUtilsMockedStatic.when(() -> getPartyNameBasedOnType(claimant)).thenReturn(claimantName);
        partyUtilsMockedStatic.when(() -> getPartyNameBasedOnType(defendant)).thenReturn(defendantName);
        when(pipInPostConfiguration.getCuiFrontEndUrl()).thenReturn(frontEndUrl);
        dateFormatHelperMockedStatic.when(() -> formatLocalDate(responseDeadline.toLocalDate(), DATE)).thenReturn(formattedDate);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        partyUtilsMockedStatic.close();
        dateFormatHelperMockedStatic.close();

        assertThat(updatedProperties.size()).isEqualTo(4);
        assertThat(updatedProperties).containsEntry(CLAIMANT_NAME, claimantName);
        assertThat(updatedProperties).containsEntry(DEFENDANT_NAME, defendantName);
        assertThat(updatedProperties).containsEntry(FRONTEND_URL, frontEndUrl);
        assertThat(updatedProperties).containsEntry(RESPONSE_DEADLINE, formattedDate);
    }
}
