package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator.DEFENDANT_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator.HEARING_TIME;

@ExtendWith(MockitoExtension.class)
class GenerateHearingNoticeRespSolTwoEmailDTOGeneratorTest {

    private static final String REF_TEMPLATE = "notification-of-hearing-%s";
    private static final String CONFIG_TEMPLATE_ID = "expected-template";
    private static final LocalDate HEARING_DATE_VAL = LocalDate.of(2025, 7, 1);
    private static final String TIME_FIELD = "0830";
    private static final String FORMATTED_DATE = "01-07-2025";
    private static final String FORMATTED_TIME = "08:30am";
    private static final String RESP2_REF = "RESP2-REF";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private GenerateHearingNoticeRespSolTwoEmailDTOGenerator generator;

    @Test
    void getEmailTemplateId_returnsConfiguredValue() {
        when(notificationsProperties.getHearingListedNoFeeDefendantLrTemplate())
                .thenReturn(CONFIG_TEMPLATE_ID);

        String actual = generator.getEmailTemplateId(CaseData.builder().build());

        assertEquals(CONFIG_TEMPLATE_ID, actual);
        verify(notificationsProperties).getHearingListedNoFeeDefendantLrTemplate();
        verifyNoMoreInteractions(notificationsProperties);
    }

    @Test
    void getReferenceTemplate_returnsStaticFormat() {
        assertEquals(REF_TEMPLATE, generator.getReferenceTemplate());
    }

    @Test
    void addCustomProperties_withReference_populatesAllFields() {
        CaseData caseData = CaseData.builder()
                .hearingDate(HEARING_DATE_VAL)
                .hearingTimeHourMinute(TIME_FIELD)
                .respondentSolicitor2Reference(RESP2_REF)
                .build();

        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.getFormattedHearingDate(HEARING_DATE_VAL))
                    .thenReturn(FORMATTED_DATE);
            utils.when(() -> NotificationUtils.getFormattedHearingTime(TIME_FIELD))
                    .thenReturn(FORMATTED_TIME);

            Map<String, String> base = new HashMap<>();
            base.put("foo", "bar");
            Map<String, String> result = generator.addCustomProperties(base, caseData);

            assertAll("result",
                    () -> assertEquals("bar", result.get("foo")),
                    () -> assertEquals(FORMATTED_DATE, result.get(HEARING_DATE)),
                    () -> assertEquals(FORMATTED_TIME, result.get(HEARING_TIME)),
                    () -> assertEquals(RESP2_REF, result.get(DEFENDANT_REFERENCE_NUMBER))
            );

            utils.verify(() -> NotificationUtils.getFormattedHearingDate(HEARING_DATE_VAL));
            utils.verify(() -> NotificationUtils.getFormattedHearingTime(TIME_FIELD));
        }
    }

    @Test
    void addCustomProperties_withoutReference_setsEmptyString() {
        CaseData caseData = CaseData.builder()
                .hearingDate(HEARING_DATE_VAL)
                .hearingTimeHourMinute(TIME_FIELD)
                .respondentSolicitor2Reference(null)
                .build();

        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.getFormattedHearingDate(HEARING_DATE_VAL))
                    .thenReturn(FORMATTED_DATE);
            utils.when(() -> NotificationUtils.getFormattedHearingTime(TIME_FIELD))
                    .thenReturn(FORMATTED_TIME);

            Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

            assertAll("result",
                    () -> assertEquals(FORMATTED_DATE, result.get(HEARING_DATE)),
                    () -> assertEquals(FORMATTED_TIME, result.get(HEARING_TIME)),
                    () -> assertEquals("", result.get(DEFENDANT_REFERENCE_NUMBER))
            );

            utils.verify(() -> NotificationUtils.getFormattedHearingDate(HEARING_DATE_VAL));
            utils.verify(() -> NotificationUtils.getFormattedHearingTime(TIME_FIELD));
        }
    }
}
