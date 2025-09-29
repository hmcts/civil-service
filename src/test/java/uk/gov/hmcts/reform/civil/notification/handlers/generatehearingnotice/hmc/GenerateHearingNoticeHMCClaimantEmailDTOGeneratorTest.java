package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.hmc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_TIME;

@ExtendWith(MockitoExtension.class)
class GenerateHearingNoticeHMCClaimantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private HearingNoticeCamundaService camundaService;

    @InjectMocks
    private GenerateHearingNoticeHMCClaimantEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectTemplateId_whenClaimantIsBilingual() {
        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference(Language.BOTH.toString())
            .build();

        String expectedTemplate = "welsh-template-id";
        when(notificationsProperties.getHearingNotificationLipDefendantTemplateWelsh())
            .thenReturn(expectedTemplate);

        String actual = generator.getEmailTemplateId(caseData);

        assertEquals(expectedTemplate, actual);
    }

    @Test
    void shouldReturnCorrectTemplateId_whenClaimantIsNotBilingual() {
        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference(Language.ENGLISH.toString())
            .build();

        String expectedTemplate = "english-template-id";
        when(notificationsProperties.getHearingNotificationLipDefendantTemplate())
            .thenReturn(expectedTemplate);

        String actual = generator.getEmailTemplateId(caseData);

        assertEquals(expectedTemplate, actual);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertEquals("notification-of-hearing-lip-%s", generator.getReferenceTemplate());
    }

    @Test
    void shouldAddCustomProperties() {
        String processInstanceId = "process-id";
        LocalDate hearingDate = LocalDate.of(2025, 7, 15);
        LocalDateTime hearingStartDateTime = LocalDateTime.of(2025, 7, 15, 14, 0);

        CaseData caseData = CaseData.builder()
            .hearingDate(hearingDate)
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId(processInstanceId)
                                 .build())
            .applicant1(Party.builder().individualFirstName("Claimant")
                            .individualLastName("Org").type(Party.Type.INDIVIDUAL).build())
            .build();

        HearingNoticeVariables hearingNoticeVariables =  HearingNoticeVariables.builder()
            .build();

        hearingNoticeVariables.setHearingStartDateTime(hearingStartDateTime);

        when(camundaService.getProcessVariables(processInstanceId)).thenReturn(hearingNoticeVariables);

        Map<String, String> inputProps = new HashMap<>();
        Map<String, String> result = generator.addCustomProperties(inputProps, caseData);

        assertEquals("15-07-2025", result.get(HEARING_DATE));
        assertEquals("02:00pm", result.get(HEARING_TIME));
        assertEquals("Claimant Org", result.get(CLAIM_LEGAL_ORG_NAME_SPEC));
    }
}
