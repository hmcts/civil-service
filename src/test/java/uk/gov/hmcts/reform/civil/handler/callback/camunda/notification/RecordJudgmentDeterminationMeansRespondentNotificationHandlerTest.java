package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT1_FOR_RECORD_JUDGMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT2_FOR_RECORD_JUDGMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;

@SpringBootTest(classes = {
    RecordJudgmentDeterminationMeansRespondentNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class RecordJudgmentDeterminationMeansRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private RecordJudgmentDeterminationMeansRespondentNotificationHandler handler;
    @MockBean
    private OrganisationService organisationService;
    private static final String ORG_NAME_RESPONDENT1 = "Org1";
    private static final String ORG_NAME_RESPONDENT2 = "Org2";
    public static final String TASK_ID_RESPONDENT1 = "RecordJudgmentNotifyRespondent1";
    public static final String TASK_ID_RESPONDENT2 = "RecordJudgmentNotifyRespondent2";

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyLrRecordJudgmentDeterminationMeansTemplate()).thenReturn("template-id");
        }

        @Test
        void shouldNotifyRespondentSolicitor1_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithDeterminationMeans();
            caseData = caseData.toBuilder()
                .respondentSolicitor1EmailAddress("respondent1@example.com")
                .respondentSolicitor2EmailAddress("respondent2@example.com")
                .legacyCaseReference("000DC001")
                .addRespondent2(YesOrNo.YES)
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL)
                                 .partyName("Test")
                                 .individualLastName("Test Lastname")
                                 .individualFirstName("Test Firstname").build())
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL)
                                 .partyName("Test2")
                                 .individualLastName("Test2 Lastname")
                                 .individualFirstName("Test2 Firstname").build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                                            .organisationID(ORG_NAME_RESPONDENT1).build()).build()).build();

            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder().name(ORG_NAME_RESPONDENT1).build()));

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT1_FOR_RECORD_JUDGMENT.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent1@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "record-judgment-determination-means-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithDeterminationMeans();
            caseData = caseData.toBuilder()
                .respondentSolicitor1EmailAddress("respondent1@example.com")
                .respondentSolicitor2EmailAddress("respondent2@example.com")
                .legacyCaseReference("000DC001")
                .addRespondent2(YesOrNo.YES)
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL)
                                 .partyName("Test")
                                 .individualLastName("Test Lastname")
                                 .individualFirstName("Test Firstname").build())
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL)
                                 .partyName("Test2")
                                 .individualLastName("Test2 Lastname")
                                 .individualFirstName("Test2 Firstname").build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                                             .organisationID(ORG_NAME_RESPONDENT2).build()).build()).build();
            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder().name(ORG_NAME_RESPONDENT2).build()));

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT2_FOR_RECORD_JUDGMENT.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent2@example.com",
                "template-id",
                getNotificationDataMapRespondent2(caseData),
                "record-judgment-determination-means-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                LEGAL_ORG_NAME, ORG_NAME_RESPONDENT1,
                DEFENDANT_NAME, NotificationUtils.getDefendantNameBasedOnCaseType(caseData)
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapRespondent2(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                LEGAL_ORG_NAME, ORG_NAME_RESPONDENT2,
                DEFENDANT_NAME, NotificationUtils.getDefendantNameBasedOnCaseType(caseData)
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_RESPONDENT1_FOR_RECORD_JUDGMENT.name()).build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT1);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_RESPONDENT2_FOR_RECORD_JUDGMENT.name()).build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT2);
    }
}
