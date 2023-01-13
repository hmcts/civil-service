package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.DEADLINE;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;

@SpringBootTest(classes = {
    NotifyClaimCallbackHandler.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotifyClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private Time time;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private NotifyClaimCallbackHandler handler;

    @Autowired
    private ExitSurveyContentService exitSurveyContentService;

    private final LocalDateTime notificationDate = LocalDateTime.now();
    private final LocalDateTime deadline = notificationDate.toLocalDate().atTime(END_OF_BUSINESS_DAY);
    public static final String DOC_SERVED_DATE_IN_FUTURE = "Date you served the documents must be today or in the past";
    private static final String ERROR_PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT = "There is a problem"
        + "\n"
        + "This action cannot currently be performed because it has either already"
        + " been completed or another action must be completed first.";

    public static final String DOC_SERVED_DATE_OLDER_THAN_14DAYS =
        "Date of Service should not be more than 14 days old";

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldPrepopulateDynamicListWithOptions_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyBothSolicitors()
                .build();
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertTrue(response.getData().containsKey("defendantSolicitorNotifyClaimOptions"));
        }

        @Test
        void aboutToStart_ShouldReturnErrorMessageCallbackResponse_1v1_WhenDefendant1LiP_CosDisabled() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v1LiP()
                .build();

            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(false);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains(ERROR_PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT);
        }

        @Test
        void aboutToStart_ShouldReturnErrorMessageCallbackResponse_1v2WhenDefendant2LiP_CosDisabled() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2Respondent2LiP()
                .addRespondent2(YesOrNo.YES)
                .build();

            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(false);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains(ERROR_PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT);
        }

        @Test
        void aboutToStart_Should_Not_ReturnErrorMessageCallbackResponse_1v1_WhenDefendant1Represented_CosDisabled() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .addRespondent2(YesOrNo.NO)
                .build();

            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(false);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertNull(response.getErrors());
        }

        @Test
        void aboutToStart_ShouldNotReturnErrorMessageCallbackResponse_1v2_BothDefendant1Represented_CosDisabled() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmittedTwoRespondentRepresentatives()
                .build();

            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(false);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertNull(response.getErrors());
        }
    }

    @Nested
    class MidEventValidateOptionsCallback {

        private static final String PAGE_ID = "validateNotificationOption";
        public static final String WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR =
            "Your claim will progress offline if you only notify one Defendant of the claim details.";

        @Test
        void shouldThrowWarning_whenNotifyingOnlyOneRespondentSolicitorAndMultipartyToggleOn() {

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getWarnings()).contains(WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR);
        }

        @Test
        void shouldNotThrowWarning_whenNotifyingBothRespondentSolicitors() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyBothSolicitors()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getWarnings()).isEmpty();
        }
    }

    @Nested
    class MidEventValidateCosDefendant1Callback {

        private static final String PAGE_ID = "validateCosNotifyClaimDef1";

        @Test
        void shouldThrowError_whenNotifyingDate_futureDate() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified1v1LiP(CertificateOfService.builder()
                                                .cosDateOfServiceForDefendant(LocalDate.now().plusDays(2))
                                                .build())
                .build();
            when(time.now()).thenReturn(LocalDateTime.now());
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).contains(DOC_SERVED_DATE_IN_FUTURE);
        }

        @Test
        void shouldNot_ThrowError_whenNotifyingDate_isCurrentDate() {
            ArrayList<String> cosUIStatement = new ArrayList<>();
            cosUIStatement.add("CERTIFIED");
            LocalDate cosNotifyDate = LocalDate.now();
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified1v1LiP(CertificateOfService.builder()
                                                .cosDateOfServiceForDefendant(LocalDate.now())
                                                .build())
                .build();
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(cosNotifyDate.atTime(15, 05)))
                .thenReturn(cosNotifyDate.plusDays(14).atTime(END_OF_BUSINESS_DAY));

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNot_ThrowError_whenNotifyingDate_isPastDate_notOlderThan14days() {
            LocalDate cosNotifyDate = LocalDate.now().minusDays(3);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified1v1LiP(CertificateOfService.builder()
                                                .cosDateOfServiceForDefendant(cosNotifyDate)
                                                .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(cosNotifyDate.atTime(15, 05)))
                .thenReturn(cosNotifyDate.plusDays(14).atTime(16, 0));
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

    }

    @Nested
    class MidEventValidateCosDefendant2Callback {

        private static final String PAGE_ID = "validateCosNotifyClaimDef2";
        LocalDateTime claimDetailsNotificationDeadline = LocalDateTime.of(2021, 5, 15, 16, 0, 0);

        @Test
        void shouldThrowError_whenNotifyingDate_futureDate() {

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified1v2RespondentLiP()
                .cosNotifyClaimDefendant2(CertificateOfService.builder()
                                                .cosDateOfServiceForDefendant(LocalDate.now().plusDays(2))
                                                .build())
                .build();
            when(time.now()).thenReturn(LocalDateTime.now());
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).contains(DOC_SERVED_DATE_IN_FUTURE);
        }

        @Test
        void should_ThrowError_whenCosServiceDate_is14thDay_afterBusinessDayEndTime() {

            LocalDate cosNotifyDate = LocalDate.of(2021, 5, 1);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified1v2RespondentLiP()
                .cosNotifyClaimDefendant2(CertificateOfService.builder()
                                              .cosDateOfServiceForDefendant(cosNotifyDate)
                                              .build())
                .build();

            when(time.now()).thenReturn(LocalDateTime.of(2021, 5, 15, 16, 05));
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);

            when(deadlinesCalculator.plus14DaysAt4pmDeadline(cosNotifyDate.atTime(16, 05)))
                .thenReturn(claimDetailsNotificationDeadline);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains(DOC_SERVED_DATE_OLDER_THAN_14DAYS);
        }

        @Test
        void shouldNot_ThrowError_whenNotifyingDate_isCurrentDate() {
            ArrayList<String> cosUIStatement = new ArrayList<>();
            cosUIStatement.add("CERTIFIED");
            LocalDate cosNotifyDate = LocalDate.now();
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified1v2RespondentLiP()
                .cosNotifyClaimDefendant2(CertificateOfService.builder()
                                                .cosDateOfServiceForDefendant(cosNotifyDate)
                                                .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(cosNotifyDate.atTime(15, 05)))
                .thenReturn(cosNotifyDate.plusDays(14).atTime(END_OF_BUSINESS_DAY));
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNot_ThrowError_whenCosServiceDate_notOlderThan14Days() {
            LocalDate cosNotifyDate = LocalDate.now().minusDays(5);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified1v2RespondentLiP()
                .cosNotifyClaimDefendant2(CertificateOfService.builder()
                                                .cosDateOfServiceForDefendant(cosNotifyDate)
                                                .build())
                .build();

            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));

            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);

            when(deadlinesCalculator.plus14DaysAt4pmDeadline(cosNotifyDate.atTime(15, 05)))
                .thenReturn(cosNotifyDate.plusDays(14).atTime(END_OF_BUSINESS_DAY));

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNot_ThrowError_whenCosServiceDate_is14thDay_beforeBusinessDayEndTime() {

            LocalDate cosNotifyDate = LocalDate.of(2021, 5, 1);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified1v2RespondentLiP()
                .cosNotifyClaimDefendant2(CertificateOfService.builder()
                                              .cosDateOfServiceForDefendant(cosNotifyDate)
                                              .build())
                .build();

            when(time.now()).thenReturn(LocalDateTime.of(2021, 5, 15, 15, 05));
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);

            when(deadlinesCalculator.plus14DaysAt4pmDeadline(cosNotifyDate.atTime(15, 05)))
                .thenReturn(claimDetailsNotificationDeadline);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

    }

    @Nested
    class AboutToSubmit {

        @Nested
        class SubmittedAtCurrentTime {

            @BeforeEach
            void setup() {
                when(time.now()).thenReturn(notificationDate);
                when(deadlinesCalculator.plus14DaysAt4pmDeadline(any(LocalDateTime.class))).thenReturn(deadline);
            }

            @Test
            void shouldUpdateBusinessProcessAndAddNotificationDeadline_when14DaysIsBeforeThe4MonthDeadline() {
                LocalDateTime claimNotificationDeadline = notificationDate.plusMonths(4);
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1()
                    .claimNotificationDeadline(claimNotificationDeadline)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(
                    CallbackType.ABOUT_TO_SUBMIT,
                    caseData
                ).build();
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .extracting("businessProcess")
                    .extracting("camundaEvent", "status")
                    .containsOnly(NOTIFY_DEFENDANT_OF_CLAIM.name(), "READY");

                assertThat(response.getData())
                    .containsEntry("claimNotificationDate", notificationDate.format(ISO_DATE_TIME))
                    .containsEntry("claimDetailsNotificationDeadline", deadline.format(ISO_DATE_TIME))
                    .containsEntry("nextDeadline", deadline.format(ISO_DATE));
            }

            @Test
            void shouldSetClaimNotificationAsNotificationDeadlineAt_when14DaysIsAfterThe4MonthDeadline() {
                LocalDateTime claimNotificationDeadline = notificationDate.minusDays(5);
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1()
                    .claimNotificationDeadline(claimNotificationDeadline)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(
                    CallbackType.ABOUT_TO_SUBMIT,
                    caseData
                ).build();
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .containsEntry("claimDetailsNotificationDeadline", claimNotificationDeadline.format(ISO_DATE_TIME))
                    .containsEntry("nextDeadline", claimNotificationDeadline.format(ISO_DATE));
            }

            @Test
            void shouldSetClaimDetailsNotificationAsClaimNotificationDeadline_when14DaysIsSameDayAs4MonthDeadline() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1()
                    .claimNotificationDeadline(deadline)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(
                    CallbackType.ABOUT_TO_SUBMIT,
                    caseData
                ).build();
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .containsEntry("claimDetailsNotificationDeadline", deadline.format(ISO_DATE_TIME))
                    .containsEntry("nextDeadline", deadline.format(ISO_DATE));
            }
        }

        @Nested
        class SubmittedOnDeadlineDay {

            LocalDateTime claimNotificationDeadline = LocalDateTime.of(2021, 4, 16, 23, 59, 59);
            LocalDateTime claimDetailsNotificationDeadline = LocalDateTime.of(2021, 4, 15, 15, 15, 59);
            LocalDateTime expectedDeadline = claimDetailsNotificationDeadline;

            LocalDateTime notifyClaimDateTime = LocalDateTime.of(2021, 4, 5, 17, 0);

            @BeforeEach
            void setup() {
                when(time.now()).thenReturn(notifyClaimDateTime);
            }

            @Test
            void shouldSetDetailsNotificationDeadlineTo4pmDeadline_whenNotifyClaimBefore4pm() {
                when(deadlinesCalculator.plus14DaysAt4pmDeadline(any(LocalDateTime.class)))
                    .thenReturn(claimDetailsNotificationDeadline);

                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1()
                    .claimNotificationDeadline(claimNotificationDeadline)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(
                    CallbackType.ABOUT_TO_SUBMIT,
                    caseData
                ).build();
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .containsEntry("claimDetailsNotificationDeadline", expectedDeadline.format(ISO_DATE_TIME));
            }

            @Test
            void shouldSetDetailsNotificationDeadlineTo4pmDeadline_whenNotifyClaimAfter4pm() {

                when(deadlinesCalculator.plus14DaysAt4pmDeadline(any(LocalDateTime.class)))
                    .thenReturn(claimDetailsNotificationDeadline);

                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1()
                    .claimNotificationDeadline(claimNotificationDeadline)
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder().of(
                    CallbackType.ABOUT_TO_SUBMIT,
                    caseData
                ).build();
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .containsEntry("claimDetailsNotificationDeadline", expectedDeadline.format(ISO_DATE_TIME));
            }

            @Test
            void shouldSetDetailsNotificationDeadline_Cos_1v2_whenLipDefendant1() {

                LocalDate cosNotifyDate = LocalDate.of(2021, 4, 2);

                when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
                when(time.now()).thenReturn(LocalDateTime.of(2021, 5, 3, 15, 05));
                when(deadlinesCalculator.plus14DaysAt4pmDeadline(cosNotifyDate.atTime(15, 05)))
                    .thenReturn(claimDetailsNotificationDeadline);

                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified1v2RespondentLiP()
                    .cosNotifyClaimDefendant1(CertificateOfService.builder()
                                                  .cosDateOfServiceForDefendant(cosNotifyDate)
                                                  .build())
                    .claimNotificationDeadline(claimNotificationDeadline)
                    .addRespondent2(YesOrNo.YES)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(
                    CallbackType.ABOUT_TO_SUBMIT,
                    caseData
                ).build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(responseData.getCosNotifyClaimDefendant1()
                               .getCosSenderStatementOfTruthLabel().contains("CERTIFIED"));
                assertThat(response.getData())
                    .containsEntry(
                        "claimDetailsNotificationDeadline",
                        expectedDeadline.format(ISO_DATE_TIME)
                    );
            }

            @Test
            void shouldSetDetailsNotificationDeadline_Cos_1v2_whenLipDefendant2() {

                LocalDate cosNotifyDate = LocalDate.of(2021, 4, 2);
                when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
                when(time.now()).thenReturn(LocalDateTime.of(2021, 5, 3, 15, 05));
                when(deadlinesCalculator.plus14DaysAt4pmDeadline(cosNotifyDate.atTime(15, 05)))
                    .thenReturn(claimDetailsNotificationDeadline);

                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified1v2RespondentLiP()
                    .cosNotifyClaimDefendant2(CertificateOfService.builder()
                                                  .cosDateOfServiceForDefendant(cosNotifyDate)
                                                  .build())
                    .respondent1Represented(YesOrNo.YES)
                    .respondent2Represented(YesOrNo.NO)
                    .addRespondent2(YesOrNo.YES)
                    .claimNotificationDeadline(claimNotificationDeadline)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(
                    CallbackType.ABOUT_TO_SUBMIT,
                    caseData
                ).build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(responseData.getCosNotifyClaimDefendant2()
                               .getCosSenderStatementOfTruthLabel().contains("CERTIFIED"));

                assertThat(response.getData())
                    .containsEntry("claimDetailsNotificationDeadline", expectedDeadline.format(ISO_DATE_TIME));
            }

            @Test
            void shouldSetDetailsNotificationDeadline_Cos_1v2_bothDefendantsLip_def1NotifiedEarlier() {

                LocalDate cosDef1NotifyDate = LocalDate.of(2021, 5, 1);
                LocalDate cosDef2NotifyDate = LocalDate.of(2021, 5, 2);
                when(time.now()).thenReturn(LocalDateTime.of(2021, 5, 3, 15, 05));

                when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
                when(deadlinesCalculator.plus14DaysAt4pmDeadline(cosDef1NotifyDate.atTime(15, 05)))
                    .thenReturn(claimDetailsNotificationDeadline);

                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified1v2RespondentLiP()
                    .cosNotifyClaimDefendant1(CertificateOfService.builder()
                                                  .cosDateOfServiceForDefendant(cosDef1NotifyDate)
                                                  .build())
                    .cosNotifyClaimDefendant2(CertificateOfService.builder()
                                                  .cosDateOfServiceForDefendant(cosDef2NotifyDate)
                                                  .build())
                    .respondent1Represented(YesOrNo.NO)
                    .respondent2Represented(YesOrNo.NO)
                    .addRespondent2(YesOrNo.YES)
                    .claimNotificationDeadline(claimNotificationDeadline)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(
                    CallbackType.ABOUT_TO_SUBMIT,
                    caseData
                ).build();
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .containsEntry("claimDetailsNotificationDeadline", expectedDeadline.format(ISO_DATE_TIME));
            }

            @Test
            void shouldSetDetailsNotificationDeadline_Cos_1v2_bothDefendantsLip_def2NotifiedEarlier() {

                LocalDate cosDef1NotifyDate = LocalDate.of(2021, 5, 2);
                LocalDate cosDef2NotifyDate = LocalDate.of(2021, 4, 28);

                when(time.now()).thenReturn(LocalDateTime.of(2021, 5, 3, 15, 05));
                when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
                when(deadlinesCalculator.plus14DaysAt4pmDeadline(cosDef2NotifyDate.atTime(15, 05)))
                    .thenReturn(claimDetailsNotificationDeadline);

                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified1v2RespondentLiP()
                    .cosNotifyClaimDefendant1(CertificateOfService.builder()
                                                  .cosDateOfServiceForDefendant(cosDef1NotifyDate)
                                                  .build())
                    .cosNotifyClaimDefendant2(CertificateOfService.builder()
                                                  .cosDateOfServiceForDefendant(cosDef2NotifyDate)
                                                  .build())
                    .respondent1Represented(YesOrNo.NO)
                    .respondent2Represented(YesOrNo.NO)
                    .addRespondent2(YesOrNo.YES)
                    .claimNotificationDeadline(claimNotificationDeadline)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(
                    CallbackType.ABOUT_TO_SUBMIT,
                    caseData
                ).build();
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .containsEntry("claimDetailsNotificationDeadline", expectedDeadline.format(ISO_DATE_TIME));
            }

            @Test
            void shouldSetDetailsNotificationDeadline_Cos_1v2_bothDefendantsLip_sameDates() {

                LocalDate cosDef1NotifyDate = LocalDate.of(2021, 4, 2);
                LocalDate cosDef2NotifyDate = LocalDate.of(2021, 4, 2);

                when(time.now()).thenReturn(LocalDateTime.of(2021, 5, 3, 15, 05));
                when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
                when(deadlinesCalculator.plus14DaysAt4pmDeadline(cosDef1NotifyDate.atTime(15, 05)))
                    .thenReturn(claimDetailsNotificationDeadline);

                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified1v2RespondentLiP()
                    .cosNotifyClaimDefendant1(CertificateOfService.builder()
                                                  .cosDateOfServiceForDefendant(cosDef1NotifyDate)
                                                  .build())
                    .cosNotifyClaimDefendant2(CertificateOfService.builder()
                                                  .cosDateOfServiceForDefendant(cosDef2NotifyDate)
                                                  .build())
                    .respondent1Represented(YesOrNo.NO)
                    .respondent2Represented(YesOrNo.NO)
                    .addRespondent2(YesOrNo.YES)
                    .claimNotificationDeadline(claimNotificationDeadline)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(
                    CallbackType.ABOUT_TO_SUBMIT,
                    caseData
                ).build();
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .containsEntry("claimDetailsNotificationDeadline", expectedDeadline.format(ISO_DATE_TIME));
            }

            @Test
            void shouldSetDetailsNotificationDeadline_Cos_1v1_whenLipDefendant() {

                LocalDate cosNotifyDate = LocalDate.of(2021, 4, 26);
                when(time.now()).thenReturn(LocalDateTime.of(2021, 5, 3, 15, 05));
                when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);

                when(deadlinesCalculator.plus14DaysAt4pmDeadline(cosNotifyDate.atTime(15, 05)))
                    .thenReturn(claimDetailsNotificationDeadline);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimNotified1v1LiP(CertificateOfService.builder()
                                                    .cosDateOfServiceForDefendant(cosNotifyDate)
                                                    .build())
                    .claimNotificationDeadline(claimNotificationDeadline)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(
                    CallbackType.ABOUT_TO_SUBMIT,
                    caseData
                ).build();
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .containsEntry("claimDetailsNotificationDeadline", expectedDeadline.format(ISO_DATE_TIME));
            }

            @Test
            void shouldSetDetailsNotificationDeadline_Cos_disabled_1v1_whenLipDefendant() {

                LocalDate cosNotifyDate = LocalDate.of(2021, 4, 2);

                when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(false);

                when(deadlinesCalculator.plus14DaysAt4pmDeadline(notifyClaimDateTime))
                    .thenReturn(claimDetailsNotificationDeadline);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimNotified1v1LiP(CertificateOfService.builder()
                                                    .cosDateOfServiceForDefendant(cosNotifyDate)
                                                    .build())
                    .claimNotificationDeadline(claimNotificationDeadline)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(
                    CallbackType.ABOUT_TO_SUBMIT,
                    caseData
                ).build();
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .containsEntry("claimDetailsNotificationDeadline", expectedDeadline.format(ISO_DATE_TIME));
            }
        }

        @Nested
        class SetOrganisationPolicy {
            OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                                  .organisationID(null)
                                  .build())
                .orgPolicyReference("orgreference")
                .orgPolicyCaseAssignedRole("orgassignedrole")
                .build();

            @BeforeEach
            void setup() {
                when(time.now()).thenReturn(notificationDate);
                when(deadlinesCalculator.plus14DaysAt4pmDeadline(any(LocalDateTime.class))).thenReturn(deadline);
            }

            @Test
            void shouldSetOrganisationPolicy_1v1() {
                LocalDateTime claimNotificationDeadline = notificationDate.plusMonths(4);
                OrganisationPolicy expectedOrganisationPolicy = OrganisationPolicy.builder()
                    .organisation(Organisation.builder()
                                      .organisationID("QWERTY R")
                                      .build())
                    .orgPolicyReference("orgreference")
                    .orgPolicyCaseAssignedRole("orgassignedrole")
                    .build();

                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1()
                    .respondent1OrganisationIDCopy("QWERTY R")
                    .respondent1OrganisationPolicy(organisationPolicy)
                    .claimNotificationDeadline(claimNotificationDeadline)
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder().of(
                    CallbackType.ABOUT_TO_SUBMIT,
                    caseData
                ).build();

                assertThat(caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo(null);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(updatedData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo("QWERTY R");
                assertThat(updatedData.getRespondent1OrganisationPolicy()).isEqualTo(expectedOrganisationPolicy);
            }

            @Test
            void shouldSetOrganisationPolicy_1v2() {
                OrganisationPolicy expectedOrganisationPolicy1 = OrganisationPolicy.builder()
                    .organisation(Organisation.builder()
                                      .organisationID("QWERTY R")
                                      .build())
                    .orgPolicyReference("orgreference")
                    .orgPolicyCaseAssignedRole("orgassignedrole")
                    .build();
                OrganisationPolicy expectedOrganisationPolicy2 = OrganisationPolicy.builder()
                    .organisation(Organisation.builder()
                                      .organisationID("QWERTY R2")
                                      .build())
                    .orgPolicyReference("orgreference")
                    .orgPolicyCaseAssignedRole("orgassignedrole")
                    .build();
                LocalDateTime claimNotificationDeadline = notificationDate.plusMonths(4);

                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors()
                    .respondent1OrganisationIDCopy("QWERTY R")
                    .respondent1OrganisationPolicy(organisationPolicy)
                    .respondent2OrganisationIDCopy("QWERTY R2")
                    .respondent2OrganisationPolicy(organisationPolicy)
                    .claimNotificationDeadline(claimNotificationDeadline)
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder().of(
                    CallbackType.ABOUT_TO_SUBMIT,
                    caseData
                ).build();

                assertThat(caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo(null);
                assertThat(caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo(null);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(updatedData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo("QWERTY R");
                assertThat(updatedData.getRespondent1OrganisationPolicy()).isEqualTo(expectedOrganisationPolicy1);
                assertThat(updatedData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo("QWERTY R2");
                assertThat(updatedData.getRespondent2OrganisationPolicy()).isEqualTo(expectedOrganisationPolicy2);
            }
        }
    }

    @Nested
    class SubmittedCallback {

        private static final String CONFIRMATION_BODY = "<br />The defendant legal representative's organisation has "
            + "been notified and granted access to this claim.%n%n"
            + "You must notify the defendant with the claim details by %s";

        private static final String CONFIRMATION_SUMMARY_COS = "<br /><h2 class=\"govuk-heading-m\">What happens"
            + " next</h2> %n%n You must serve the claim details and complete the certificate of service notify claim"
            + " details next step by 4:00pm on %s.%n%nThis is a new online process - you don't need to file any"
            + " further documents to the court";
        public static final String CONFIRMATION_NOTIFICATION_ONE_PARTY_SUMMARY = "<br />Notification of claim sent to "
            + "1 Defendant legal representative only.%n%n"
            + "Your claim will proceed offline.";

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String formattedDeadline = formatLocalDateTime(DEADLINE, DATE_TIME_AT);
            String confirmationBody = String.format(CONFIRMATION_BODY, formattedDeadline)
                + exitSurveyContentService.applicantSurvey();

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# Notification of claim sent%n## Claim number: 000DC001"))
                    .confirmationBody(confirmationBody)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenNotifyingBothParties_whenInvoked() {

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyBothSolicitors()
                .build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String formattedDeadline = formatLocalDateTime(DEADLINE, DATE_TIME_AT);
            String confirmationBody = String.format(CONFIRMATION_BODY, formattedDeadline)
                + exitSurveyContentService.applicantSurvey();

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# Notification of claim sent%n## Claim number: 000DC001"))
                    .confirmationBody(confirmationBody)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenNotifyingOneParty_whenInvoked() {

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String formattedDeadline = formatLocalDateTime(DEADLINE, DATE_TIME_AT);
            String confirmationBody = String.format(
                CONFIRMATION_NOTIFICATION_ONE_PARTY_SUMMARY,
                formattedDeadline
            ) + exitSurveyContentService.applicantSurvey();

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# Notification of claim sent%n## Claim number: 000DC001"))
                    .confirmationBody(confirmationBody)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_Defendant1Lip_whenInvoked() {
            CaseData caseData = CaseDataBuilder
                .builder().atStateClaimNotified1v1LiP(CertificateOfService
                                                          .builder().cosDateOfServiceForDefendant(LocalDate.now())
                                                          .build())
                .build();
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String formattedDeadline = formatLocalDate(DEADLINE.toLocalDate(), DATE);
            String confirmationBody = String.format(CONFIRMATION_SUMMARY_COS, formattedDeadline)
                + exitSurveyContentService.applicantSurvey();

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# Certificate of Service - notify claim successful %n## 000DC001"))
                    .confirmationBody(confirmationBody)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_Defendant2Lip_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified1v2RespondentLiP()
                .cosNotifyClaimDefendant2(CertificateOfService.builder()
                                                           .cosDateOfServiceForDefendant(LocalDate.now())
                                                           .build())
                .build();
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String formattedDeadline = formatLocalDate(DEADLINE.toLocalDate(), DATE);
            String confirmationBody = String.format(CONFIRMATION_SUMMARY_COS, formattedDeadline)
                + exitSurveyContentService.applicantSurvey();

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# Certificate of Service - notify claim successful %n## 000DC001"))
                    .confirmationBody(confirmationBody)
                    .build());
        }
    }

}
