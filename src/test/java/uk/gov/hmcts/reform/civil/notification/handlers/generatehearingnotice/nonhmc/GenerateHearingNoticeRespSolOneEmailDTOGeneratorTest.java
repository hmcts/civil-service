package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.nonhmc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator.DEFENDANT_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator.HEARING_TIME;

@ExtendWith(MockitoExtension.class)
class GenerateHearingNoticeRespSolOneEmailDTOGeneratorTest {

    private static final String REF_TEMPLATE = "notification-of-hearing-%s";
    private static final String CONFIGURED_TEMPLATE_ID = "expected-template";
    private static final LocalDate HEARING_DATE_VAL = LocalDate.of(2025, 7, 1);
    private static final String TIME_FIELD = "0830";
    private static final String FORMATTED_DATE = "01-07-2025";
    private static final String FORMATTED_TIME = "08:30am";
    private static final String DEF_REF = "SOLICITOR-REF-1";
    private static final String RESPONDENT_LEGAL_ORG_NAME = "respondent-legal-org-name";

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private GenerateHearingNoticeRespSolOneEmailDTOGenerator generator;

    @Test
    void getEmailTemplateId_returnsConfiguredValue() {
        CaseData caseData = CaseData.builder().build();
        when(notificationsProperties.getHearingListedNoFeeDefendantLrTemplate())
                .thenReturn(CONFIGURED_TEMPLATE_ID);

        String actual = generator.getEmailTemplateId(caseData);

        assertEquals(CONFIGURED_TEMPLATE_ID, actual);
        verify(notificationsProperties).getHearingListedNoFeeDefendantLrTemplate();
        verifyNoMoreInteractions(notificationsProperties);
    }

    @Test
    void getReferenceTemplate_returnsStaticFormat() {
        assertEquals(REF_TEMPLATE, generator.getReferenceTemplate());
    }

    @Test
    void addCustomProperties_addsDateTimeAndDefendantRef() {
        CaseData caseData = CaseData.builder()
                .hearingDate(HEARING_DATE_VAL)
                .hearingTimeHourMinute(TIME_FIELD)
                .solicitorReferences(new SolicitorReferences()
                    .setRespondentSolicitor1Reference(DEF_REF))
                .build();

        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.getFormattedHearingDate(HEARING_DATE_VAL))
                    .thenReturn(FORMATTED_DATE);
            utils.when(() -> NotificationUtils.getFormattedHearingTime(TIME_FIELD))
                    .thenReturn(FORMATTED_TIME);

            utils.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, Boolean.TRUE, organisationService))
                .thenReturn(RESPONDENT_LEGAL_ORG_NAME);

            Map<String, String> base = new HashMap<>();
            base.put("foo", "bar");

            Map<String, String> result = generator.addCustomProperties(base, caseData);

            assertAll("result",
                    () -> assertEquals("bar", result.get("foo")),
                    () -> assertEquals(FORMATTED_DATE, result.get(HEARING_DATE)),
                    () -> assertEquals(FORMATTED_TIME, result.get(HEARING_TIME)),
                    () -> assertEquals(DEF_REF, result.get(DEFENDANT_REFERENCE_NUMBER)),
                      () -> assertEquals(RESPONDENT_LEGAL_ORG_NAME, result.get(CLAIM_LEGAL_ORG_NAME_SPEC))
            );

            utils.verify(() -> NotificationUtils.getFormattedHearingDate(HEARING_DATE_VAL));
            utils.verify(() -> NotificationUtils.getFormattedHearingTime(TIME_FIELD));
        }
    }
}
