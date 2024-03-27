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
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_FOR_RECORD_JUDGMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;

@SpringBootTest(classes = {
    RecordJudgmentDeterminationMeansApplicantNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class RecordJudgmentDeterminationMeansApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private RecordJudgmentDeterminationMeansApplicantNotificationHandler handler;
    @MockBean
    private OrganisationService organisationService;
    private static final String ORG_NAME = "Org1";
    public static final String TASK_ID = "RecordJudgmentApplicantSolicitor1";

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyLrRecordJudgmentDeterminationMeansTemplate()).thenReturn("template-id");
            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithDeterminationMeans();
            caseData = caseData.toBuilder().applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                                                            .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                                                                            .email("applicantsolicitor@example.com")
                                                                                            .build())
                .addApplicant2(YesOrNo.NO)
                .legacyCaseReference("000DC001")
                .addRespondent2(YesOrNo.NO)
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL)
                                 .partyName("Test")
                                 .individualLastName("Test Lastname")
                                 .individualFirstName("Test Firstname").build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                                            .organisationID(ORG_NAME).build()).build()).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "record-judgment-determination-means-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                LEGAL_ORG_NAME, ORG_NAME,
                DEFENDANT_NAME, NotificationUtils.getDefendantNameBasedOnCaseType(caseData)
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_APPLICANT_FOR_RECORD_JUDGMENT.name()).build()).build()))
            .isEqualTo(TASK_ID);
    }
}
