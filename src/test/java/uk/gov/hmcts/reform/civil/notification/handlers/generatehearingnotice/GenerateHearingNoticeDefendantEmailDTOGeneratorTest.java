package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator.HEARING_TIME;

@ExtendWith(MockitoExtension.class)
class GenerateHearingNoticeDefendantEmailDTOGeneratorTest {

    private static final String TEMPLATE_REF        = "notification-of-hearing-lip-%s";
    private static final String ENGLISH_TEMPLATE    = "english-template";
    private static final String WELSH_TEMPLATE      = "welsh-template";
    private static final String PROCESS_ID          = "pid";
    private static final String CASE_REF            = "CASE-123";
    private static final LocalDate HEARING_DATE_VAL = LocalDate.of(2025, 6, 30);
    private static final LocalDateTime START_DT     = LocalDateTime.of(2025, 6, 30, 14, 45);
    private static final String FORMATTED_DATE      = "30-06-2025";
    private static final String FORMATTED_TIME      = "02:45pm";
    private static final String TIME_ARG            = "14:45";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private HearingNoticeCamundaService camundaService;

    @InjectMocks
    private GenerateHearingNoticeDefendantEmailDTOGenerator generator;

    @Test
    void getEmailTemplateId_returnsEnglish_whenNotBilingual() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(notificationsProperties.getHearingNotificationLipDefendantTemplate())
                .thenReturn(ENGLISH_TEMPLATE);

        String actual = generator.getEmailTemplateId(caseData);

        assertEquals(ENGLISH_TEMPLATE, actual);
        verify(notificationsProperties).getHearingNotificationLipDefendantTemplate();
        verifyNoMoreInteractions(notificationsProperties);
    }

    @Test
    void getEmailTemplateId_returnsWelsh_whenBilingual() {
        CaseData caseData = CaseDataBuilder.builder()
                .caseDataLip(CaseDataLiP.builder()
                        .respondent1LiPResponse(RespondentLiPResponse.builder()
                                .respondent1ResponseLanguage(Language.BOTH.toString())
                                .build())
                        .build())
                .build();
        when(notificationsProperties.getHearingNotificationLipDefendantTemplateWelsh())
                .thenReturn(WELSH_TEMPLATE);

        String actual = generator.getEmailTemplateId(caseData);

        assertEquals(WELSH_TEMPLATE, actual);
        verify(notificationsProperties).getHearingNotificationLipDefendantTemplateWelsh();
        verifyNoMoreInteractions(notificationsProperties);
    }

    @Test
    void getReferenceTemplate_returnsStatic() {
        assertEquals(TEMPLATE_REF, generator.getReferenceTemplate());
    }

    @Test
    void addCustomProperties_populatesCaseRefDateAndTime() {
        CaseData caseData = CaseData.builder()
                .legacyCaseReference(CASE_REF)
                .hearingDate(HEARING_DATE_VAL)
                .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_ID).build())
                .build();
        when(camundaService.getProcessVariables(PROCESS_ID))
                .thenReturn(HearingNoticeVariables.builder()
                        .hearingStartDateTime(START_DT)
                        .build());

        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.getFormattedHearingDate(HEARING_DATE_VAL))
                    .thenReturn(FORMATTED_DATE);
            utils.when(() -> NotificationUtils.getFormattedHearingTime(TIME_ARG))
                    .thenReturn(FORMATTED_TIME);

            Map<String, String> props = new HashMap<>();
            props.put("existing", "value");
            Map<String, String> result = generator.addCustomProperties(props, caseData);

            assertAll("properties",
                    () -> assertEquals("value",           result.get("existing")),
                    () -> assertEquals(CASE_REF,          result.get(CLAIM_REFERENCE_NUMBER)),
                    () -> assertEquals(FORMATTED_DATE,    result.get(HEARING_DATE)),
                    () -> assertEquals(FORMATTED_TIME,    result.get(HEARING_TIME))
            );

            utils.verify(() -> NotificationUtils.getFormattedHearingDate(HEARING_DATE_VAL));
            utils.verify(() -> NotificationUtils.getFormattedHearingTime(TIME_ARG));
        }
    }
}
