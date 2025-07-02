package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.nonhmc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator.HEARING_TIME;

@ExtendWith(MockitoExtension.class)
class GenerateHearingNoticeClaimantEmailDTOGeneratorTest {

    private static final String TEMPLATE_REF            = "notification-of-hearing-lip-%s";
    private static final String ENGLISH_TEMPLATE_ID     = "english-template";
    private static final String WELSH_TEMPLATE_ID       = "welsh-template";

    private static final String TIME_1015               = "1015";
    private static final String TIME_0800               = "0800";
    private static final LocalDate DATE_2025_07_04      = LocalDate.of(2025, 7, 4);
    private static final LocalDate DATE_2025_12_01      = LocalDate.of(2025, 12, 1);

    private static final String FORMATTED_TIME_10_15    = "10:15am";
    private static final String FORMATTED_DATE_04_07_2025 = "04-07-2025";
    private static final String FORMATTED_TIME_8_00     = "8:00am";
    private static final String FORMATTED_DATE_01_12_2025 = "01-12-2025";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private GenerateHearingNoticeClaimantEmailDTOGenerator generator;

    @Test
    void shouldReturnEnglishTemplate_whenNotBilingual() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build().toBuilder()
                .claimantBilingualLanguagePreference(null)
                .build();

        when(notificationsProperties.getHearingNotificationLipDefendantTemplate())
                .thenReturn(ENGLISH_TEMPLATE_ID);

        String actual = generator.getEmailTemplateId(caseData);

        assertEquals(ENGLISH_TEMPLATE_ID, actual);
    }

    @Test
    void shouldReturnWelshTemplate_whenBilingual() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build().toBuilder()
                .claimantBilingualLanguagePreference(Language.BOTH.getDisplayedValue())
                .build();

        when(notificationsProperties.getHearingNotificationLipDefendantTemplateWelsh())
                .thenReturn(WELSH_TEMPLATE_ID);

        String actual = generator.getEmailTemplateId(caseData);

        assertEquals(WELSH_TEMPLATE_ID, actual);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String actual = generator.getReferenceTemplate();
        assertEquals(TEMPLATE_REF, actual);
    }

    @Test
    void shouldAddAndOverrideDateTimeProperties() {
        Map<String, String> initial = Map.of("foo", "bar");
        Map<String, String> props = new HashMap<>(initial);

        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.getFormattedHearingTime(TIME_1015))
                    .thenReturn(FORMATTED_TIME_10_15);
            utils.when(() -> NotificationUtils.getFormattedHearingDate(DATE_2025_07_04))
                    .thenReturn(FORMATTED_DATE_04_07_2025);

            CaseData caseData1 = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .build().toBuilder()
                    .hearingTimeHourMinute(TIME_1015)
                    .hearingDate(DATE_2025_07_04)
                    .build();

            Map<String, String> result1 = generator.addCustomProperties(props, caseData1);

            assertAll("add-custom-keys",
                    () -> assertEquals("bar",             result1.get("foo")),
                    () -> assertEquals(FORMATTED_TIME_10_15,    result1.get(HEARING_TIME)),
                    () -> assertEquals(FORMATTED_DATE_04_07_2025, result1.get(HEARING_DATE))
            );

            Map<String, String> props2 = new HashMap<>();
            props2.put(HEARING_TIME, "oldTime");
            props2.put(HEARING_DATE, "oldDate");

            utils.when(() -> NotificationUtils.getFormattedHearingTime(TIME_0800))
                    .thenReturn(FORMATTED_TIME_8_00);
            utils.when(() -> NotificationUtils.getFormattedHearingDate(DATE_2025_12_01))
                    .thenReturn(FORMATTED_DATE_01_12_2025);

            CaseData caseData2 = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .build().toBuilder()
                    .hearingTimeHourMinute(TIME_0800)
                    .hearingDate(DATE_2025_12_01)
                    .build();

            Map<String, String> result2 = generator.addCustomProperties(props2, caseData2);

            assertAll("override-keys",
                    () -> assertEquals(FORMATTED_TIME_8_00,     result2.get(HEARING_TIME)),
                    () -> assertEquals(FORMATTED_DATE_01_12_2025, result2.get(HEARING_DATE))
            );
        }
    }
}
