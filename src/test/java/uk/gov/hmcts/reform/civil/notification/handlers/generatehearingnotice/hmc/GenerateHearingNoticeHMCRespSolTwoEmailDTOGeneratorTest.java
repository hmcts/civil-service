package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.hmc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator.DEFENDANT_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator.HEARING_TIME;

@ExtendWith(MockitoExtension.class)
class GenerateHearingNoticeHMCRespSolTwoEmailDTOGeneratorTest {

    private static final String REF_TEMPLATE = "notification-of-hearing-%s";
    private static final String HMC_TEMPLATE = "hmc-template-two";
    private static final String PROCESS_ID = "proc-456";
    private static final String DEF_REF = "SOL-REF-2";
    private static final LocalDate HEARING_DATE_VAL = LocalDate.of(2025, 8, 15);
    private static final LocalDateTime START_DT = LocalDateTime.of(2025, 8, 15, 9, 45);
    private static final String FORMATTED_DATE = "15-08-2025";
    private static final String FORMATTED_TIME = "09:45am";
    private static final String RESPONDENT_LEGAL_ORG_NAME = "respondent2-legal-org-name";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private HearingNoticeCamundaService camundaService;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private GenerateHearingNoticeHMCRespSolTwoEmailDTOGenerator generator;

    @Test
    void getEmailTemplateId_returnsHmcTemplate() {
        CaseData caseData = CaseData.builder().build();
        when(notificationsProperties.getHearingListedNoFeeDefendantLrTemplateHMC())
                .thenReturn(HMC_TEMPLATE);

        String actual = generator.getEmailTemplateId(caseData);

        assertEquals(HMC_TEMPLATE, actual);
        verify(notificationsProperties).getHearingListedNoFeeDefendantLrTemplateHMC();
        verifyNoMoreInteractions(notificationsProperties);
    }

    @Test
    void getReferenceTemplate_returnsStaticFormat() {
        assertEquals(REF_TEMPLATE, generator.getReferenceTemplate());
    }

    @Test
    void addCustomProperties_populatesDateTimeAndDefendantRef() {
        CaseData caseData = CaseData.builder()
                .hearingDate(HEARING_DATE_VAL)
                .businessProcess(new BusinessProcess().setProcessInstanceId(PROCESS_ID))
                .solicitorReferences(new SolicitorReferences()
                    .setRespondentSolicitor1Reference(DEF_REF))
                .build();

        when(camundaService.getProcessVariables(PROCESS_ID))
                .thenReturn(new HearingNoticeVariables()
                        .setHearingStartDateTime(START_DT));

        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.getFormattedHearingDate(HEARING_DATE_VAL))
                    .thenReturn(FORMATTED_DATE);
            utils.when(() -> NotificationUtils.getFormattedHearingTime("09:45"))
                    .thenReturn(FORMATTED_TIME);

            utils.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, Boolean.FALSE, organisationService))
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

            verify(camundaService).getProcessVariables(PROCESS_ID);
            utils.verify(() -> NotificationUtils.getFormattedHearingDate(HEARING_DATE_VAL));
            utils.verify(() -> NotificationUtils.getFormattedHearingTime("09:45"));
        }
    }
}
