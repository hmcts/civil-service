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
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_TIME;

@ExtendWith(MockitoExtension.class)
class GenerateHearingNoticeHMCDefendantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private HearingNoticeCamundaService camundaService;

    @InjectMocks
    private GenerateHearingNoticeHMCDefendantEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectTemplateId_whenDefendantIsBilingual() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseDataLip(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1ResponseLanguage(Language.BOTH.toString())
                                                         .build())
                             .build())
            .build();

        String expectedTemplate = "welsh-template-id";
        when(notificationsProperties.getHearingNotificationLipDefendantTemplateWelsh())
            .thenReturn(expectedTemplate);

        String actual = generator.getEmailTemplateId(caseData);

        assertEquals(expectedTemplate, actual);
    }

    @Test
    void shouldReturnCorrectTemplateId_whenDefendantIsNotBilingual() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseDataLip(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1ResponseLanguage(Language.ENGLISH.toString())
                                                         .build())
                             .build())
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
            .legacyCaseReference("000DC001")
            .hearingDate(hearingDate)
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId(processInstanceId)
                                 .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualFirstName("John")
                             .individualLastName("Doe")
                             .partyName("John Doe")
                             .build())
            .build();

        HearingNoticeVariables hearingNoticeVariables = HearingNoticeVariables.builder()
            .hearingStartDateTime(hearingStartDateTime)
            .build();

        when(camundaService.getProcessVariables(processInstanceId)).thenReturn(hearingNoticeVariables);

        Map<String, String> inputProps = new HashMap<>();
        Map<String, String> result = generator.addCustomProperties(inputProps, caseData);

        assertEquals("000DC001", result.get(CLAIM_REFERENCE_NUMBER));
        assertEquals("15-07-2025", result.get(HEARING_DATE));
        assertEquals("02:00pm", result.get(HEARING_TIME));
        assertEquals("John Doe", result.get(CLAIM_LEGAL_ORG_NAME_SPEC));
    }
}
