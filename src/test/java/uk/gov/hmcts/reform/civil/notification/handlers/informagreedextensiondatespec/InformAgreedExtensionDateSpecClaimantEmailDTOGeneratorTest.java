package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondatespec;

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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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

@ExtendWith(MockitoExtension.class)
class InformAgreedExtensionDateSpecClaimantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private PinInPostConfiguration pinInPostConfiguration;

    @InjectMocks
    private InformAgreedExtensionDateSpecClaimantEmailDTOGenerator generator;

    @Test
    void shouldReturnWelshTemplateWhenClaimantBilingual() {
        CaseData caseData = CaseDataBuilder.builder().claimantBilingualLanguagePreference(BOTH.toString()).build();
        when(notificationsProperties.getClaimantLipDeadlineExtensionWelsh()).thenReturn("welsh-template");

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo("welsh-template");
    }

    @Test
    void shouldReturnEnglishTemplateWhenClaimantNotBilingual() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(notificationsProperties.getClaimantLipDeadlineExtension()).thenReturn("english-template");

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo("english-template");
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo("agreed-extension-date-applicant-notification-spec-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        Party claimant = new Party().setCompanyName("Claimant").setType(Party.Type.COMPANY);
        Party defendant = new Party().setCompanyName("Defendant").setType(Party.Type.COMPANY);
        LocalDateTime responseDeadline = LocalDateTime.of(2025, 6, 5, 0, 0);
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(claimant)
            .respondent1(defendant)
            .respondent1ResponseDeadline(responseDeadline)
            .build();

        try (MockedStatic<PartyUtils> partyUtils = Mockito.mockStatic(PartyUtils.class);
             MockedStatic<DateFormatHelper> dateFormatHelper = Mockito.mockStatic(DateFormatHelper.class)) {

            partyUtils.when(() -> PartyUtils.getPartyNameBasedOnType(claimant)).thenReturn("Claimant Name");
            partyUtils.when(() -> PartyUtils.getPartyNameBasedOnType(defendant)).thenReturn("Defendant Name");
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("http://front.end");
            dateFormatHelper.when(() -> DateFormatHelper.formatLocalDate(responseDeadline.toLocalDate(), DATE))
                .thenReturn("05 June 2025");

            Map<String, String> properties = new HashMap<>();
            Map<String, String> updatedProperties = generator.addCustomProperties(properties, caseData);

            assertThat(updatedProperties)
                .containsEntry(CLAIMANT_NAME, "Claimant Name")
                .containsEntry(DEFENDANT_NAME, "Defendant Name")
                .containsEntry(FRONTEND_URL, "http://front.end")
                .containsEntry(RESPONSE_DEADLINE, "05 June 2025");
        }
    }
}
