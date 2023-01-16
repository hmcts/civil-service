package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.DEADLINE;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.RESPONSE_DEADLINE;

@SpringBootTest(classes = {
    NotifyClaimDetailsCallbackHandler.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class NotifyClaimDetailsCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private NotifyClaimDetailsCallbackHandler handler;

    @Autowired
    private ExitSurveyContentService exitSurveyContentService;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldPrepopulateDynamicListWithOptions_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertTrue(response.getData().containsKey("defendantSolicitorNotifyClaimDetailsOptions"));
        }
    }

    @Nested
    class MidEventValidateOptionsCallback {

        private static final String PAGE_ID = "validateNotificationOption";
        public static final String WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR =
            "Your claim will progress offline if you only notify one Defendant of the claim details.";

        @Test
        void shouldThrowWarning_whenNotifyingOnlyOneRespondentSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getWarnings()).contains(WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR);
        }
    }

    @Nested
    class MidEventParticularsOfClaimCallback {

        private final String pageId = "particulars-of-claim";
        private final CaseData.CaseDataBuilder caseDataBuilder =
            CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder();

        @Test
        void shouldReturnErrors_whenNoDocuments() {
            CaseData caseData = caseDataBuilder.build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnErrors_whenNoDocumentsBackwardsCompatible() {
            CaseData caseData = caseDataBuilder.build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnErrors_whenParticularsOfClaimFieldsAreInErrorState() {
            CaseData caseData = caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder().build()).build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnErrors_whenParticularsOfClaimFieldsAreInErrorStateBackwardsCompatible() {
            CaseData caseData = caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder().build()).build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnNoErrors_whenParticularOfClaimsFieldsAreValid() {
            CaseData caseData = caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder()
                                                                        .particularsOfClaimText("Some string")
                                                                        .build()).build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoErrors_whenParticularOfClaimsFieldsAreValidBackwardsCompatible() {
            CaseData caseData = caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder()
                                                                        .particularsOfClaimText("Some string")
                                                                        .build()).build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmit {
        private LocalDateTime localDateTime;
        private LocalDateTime newDate;
        private LocalDateTime sixMonthDate;

        @BeforeEach
        void setup() {
            localDateTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0);
            newDate = LocalDateTime.of(2020, 1, 15, 16, 0, 0);
            sixMonthDate = LocalDateTime.of(2020, 7, 1, 0, 0, 0);
            when(time.now()).thenReturn(localDateTime);
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(localDateTime)).thenReturn(newDate);
            when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(6, localDateTime.toLocalDate()))
                .thenReturn(sixMonthDate);
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS.name(), "READY");

            assertThat(response.getData())
                .containsEntry("claimDetailsNotificationDate", localDateTime.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDeadline", newDate.format(ISO_DATE_TIME))
                .containsEntry("claimDismissedDeadline", sixMonthDate.format(ISO_DATE_TIME));
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked1v2DifferentSolicitor() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS.name(), "READY");

            assertThat(response.getData())
                .containsEntry("claimDetailsNotificationDate", localDateTime.format(ISO_DATE_TIME))
                .containsEntry("respondent1ResponseDeadline", newDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDeadline", newDate.format(ISO_DATE_TIME))
                .containsEntry("claimDismissedDeadline", sixMonthDate.format(ISO_DATE_TIME));
        }

        @Test
        void shouldUpdateCertificateOfService_and_documents_cos1_whenSubmitted() {
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            LocalDate cosDate = localDateTime.minusDays(2).toLocalDate();
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(cosDate.atTime(15, 05)))
                    .thenReturn(newDate.minusDays(2));
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                    .setCoSClaimDetailsWithDate(true, false, cosDate, null, true, false)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getCosNotifyClaimDetails1()
                           .getCosSenderStatementOfTruthLabel().contains("CERTIFIED"));
            assertThat(updatedData.getCosNotifyClaimDetails1()
                           .getCosUISenderStatementOfTruthLabel() == null);
            assertThat(updatedData.getServedDocumentFiles().getOther().size()).isEqualTo(1);
            assertThat(updatedData.getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(YES);
            assertThat(updatedData.getRespondent1ResponseDeadline()).isEqualTo(newDate.minusDays(2));
        }

        @Test
        void shouldUpdateCertificateOfService_and_documents_cos2_whenSubmitted() {
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            LocalDate cosDate = localDateTime.minusDays(2).toLocalDate();
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(cosDate.atTime(15, 05)))
                    .thenReturn(newDate.minusDays(2));
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                    .setCoSClaimDetailsWithDate(false, true, null, cosDate, false, true)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getCosNotifyClaimDetails2()
                           .getCosSenderStatementOfTruthLabel().contains("CERTIFIED"));
            assertThat(updatedData.getCosNotifyClaimDetails2()
                           .getCosUISenderStatementOfTruthLabel() == null);
            assertThat(updatedData.getServedDocumentFiles().getOther().size()).isEqualTo(1);
            assertThat(updatedData.getCosNotifyClaimDetails2().getCosDocSaved()).isEqualTo(YES);
            assertThat(updatedData.getNextDeadline()).isEqualTo(newDate.minusDays(2).toLocalDate());
        }

        @Test
        void shouldUpdate_to_earliest_day_cos2_is_earliest_whenSubmitted() {
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            LocalDate cos1Date = localDateTime.minusDays(2).toLocalDate();
            LocalDate cos2Date = localDateTime.minusDays(3).toLocalDate();
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(cos1Date.atTime(15, 05)))
                    .thenReturn(newDate.minusDays(2));
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(cos2Date.atTime(15, 05)))
                    .thenReturn(newDate.minusDays(3));
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                    .setCoSClaimDetailsWithDate(true, true, cos1Date, cos2Date, true, true)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getServedDocumentFiles().getOther().size()).isEqualTo(2);
            assertThat(updatedData.getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(YES);
            assertThat(updatedData.getCosNotifyClaimDetails2().getCosDocSaved()).isEqualTo(YES);
            assertThat(updatedData.getRespondent1ResponseDeadline()).isEqualTo(newDate.minusDays(3));
        }

        @Test
        void shouldUpdate_to_earliest_day_cos1_is_earliest_whenSubmitted() {
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            LocalDate cos1Date = localDateTime.minusDays(3).toLocalDate();
            LocalDate cos2Date = localDateTime.minusDays(2).toLocalDate();
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(cos1Date.atTime(15, 05)))
                    .thenReturn(newDate.minusDays(3));
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(cos2Date.atTime(15, 05)))
                    .thenReturn(newDate.minusDays(2));
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                    .setCoSClaimDetailsWithDate(true, true, cos1Date, cos2Date, true, true)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getServedDocumentFiles().getOther().size()).isEqualTo(2);
            assertThat(updatedData.getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(YES);
            assertThat(updatedData.getCosNotifyClaimDetails2().getCosDocSaved()).isEqualTo(YES);
            assertThat(updatedData.getRespondent1ResponseDeadline()).isEqualTo(newDate.minusDays(3));
        }
    }

    @Nested
    class SubmittedCallback {

        private static final String CONFIRMATION_SUMMARY = "<br />The defendant legal representative's organisation"
            + " has been notified of the claim details.%n%n"
            + "They must respond by %s. Your account will be updated and you will be sent an email.";

        public static final String CONFIRMATION_NOTIFICATION_ONE_PARTY_SUMMARY = "<br />Notification of claim "
            + "details sent to 1 Defendant legal representative only.%n%n"
            + "Your claim will proceed offline.";

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String formattedDeadline = formatLocalDateTime(RESPONSE_DEADLINE, DATE_TIME_AT);
            String confirmationBody = format(CONFIRMATION_SUMMARY, formattedDeadline)
                + exitSurveyContentService.applicantSurvey();

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# Defendant notified%n## Claim number: 000DC001"))
                    .confirmationBody(confirmationBody)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_with_cos_whenInvoked() {
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String formattedDeadline = formatLocalDateTime(RESPONSE_DEADLINE, DATE_TIME_AT);
            String confirmationBody = format(CONFIRMATION_SUMMARY, formattedDeadline)
                    + exitSurveyContentService.applicantSurvey();

            assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                            .confirmationHeader(format("# Defendant notified%n## Claim number: 000DC001"))
                            .confirmationBody(confirmationBody)
                            .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenNotifyingBothParties_whenInvoked() {

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String formattedDeadline = formatLocalDateTime(DEADLINE, DATE_TIME_AT);
            String confirmationBody = String.format(CONFIRMATION_SUMMARY, formattedDeadline)
                + exitSurveyContentService.applicantSurvey();

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# Defendant notified%n## Claim number: 000DC001"))
                    .confirmationBody(confirmationBody)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenNotifyingOneParty_whenInvoked() {

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor()
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
                    .confirmationHeader(format("# Defendant notified%n## Claim number: 000DC001"))
                    .confirmationBody(confirmationBody)
                    .build());
        }

        @Test
        void shouldReturnCoSConfirmation_whenCosNotifyDetailsSuccess() {
            LocalDate past = LocalDate.now().minusDays(1);
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            when(time.now()).thenReturn(LocalDateTime.now());
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(any()))
                .thenReturn(past.plusDays(14).atTime(16, 0));
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, true, past, past, true, true)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationHeader()).contains("Certificate of Service");
        }

        @Test
        void shouldReturnCoSConfirmation_1Lip1Lr_whenCosNotifyDetailsSuccess() {
            LocalDate past = LocalDate.now().minusDays(1);
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            when(time.now()).thenReturn(LocalDateTime.now());
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(any()))
                .thenReturn(past.plusDays(14).atTime(16, 0));
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lip_1Lr()
                .setCoSClaimDetailsWithDate(true, false, past, null, true, false)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationHeader()).contains("Certificate of Service");
        }
    }

    @Nested
    class MidEventValidateCos {
        @Test
        void shouldPassValidateCertificateOfService_whenDateIsPast() {

            LocalDate past = LocalDate.now().minusDays(1);
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(any()))
                .thenReturn(past.plusDays(14).atTime(16, 0));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, true, past, past, true, true)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails2");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseData = mapper.convertValue(successResponse.getData(), CaseData.class);
            assertThat(responseData.getCosNotifyClaimDetails2()
                           .getCosUISenderStatementOfTruthLabel() == null);
            assertThat(successResponse.getErrors()).isEmpty();
            assertThat(params.getCaseData().getCosNotifyClaimDetails2().getCosDocSaved()).isEqualTo(NO);
        }

        @Test
        void shouldPassValidateCertificateOfService_1Lip1Lr_whenDateIsPast() {
            LocalDate past = LocalDate.now().minusDays(1);

            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            when(time.now()).thenReturn(LocalDate.now().atTime(16, 05));
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(any()))
                .thenReturn(past.plusDays(14).atTime(16, 0));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lip_1Lr()
                .setCoSClaimDetailsWithDate(true, false, past, null, true, false)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseData = mapper.convertValue(successResponse.getData(), CaseData.class);
            assertThat(responseData.getCosNotifyClaimDetails1()
                           .getCosUISenderStatementOfTruthLabel() == null);

            assertThat(successResponse.getErrors()).isEmpty();
            assertThat(params.getCaseData().getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(NO);
        }

        @Test
        void shouldPassValidateCertificateOfService_1Lr1Lip_whenServiceDateIsPast_notOlderThan14Days() {
            LocalDate past = LocalDate.now().minusDays(1);

            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(past.atTime(15, 05)))
                .thenReturn(past.plusDays(14).atTime(16, 0));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lr_1Lip()
                .setCoSClaimDetailsWithDate(true, false, past, null, true, false)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors()).isEmpty();
            assertThat(params.getCaseData().getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(NO);
        }

        @Test
        void shouldNotPassValidateCertificateOfService_1Lr1Lip_whenServiceDateIsPast_OlderThan14Days() {
            LocalDate past = LocalDate.now().minusDays(15);

            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(past.atTime(15, 05)))
                .thenReturn(past.plusDays(14).atTime(16, 0));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lr_1Lip()
                .setCoSClaimDetailsWithDate(true, false, past, null, true, false)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors().size()).isEqualTo(1);
            assertThat(params.getCaseData().getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(NO);
        }

        @Test
        void shouldNotPassValidateCertificateOfService_1Lr1Lip_whenServiceDateIsPast_deadlineTodayDate_After16hrs() {
            LocalDate past = LocalDate.now().minusDays(14);

            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            when(time.now()).thenReturn(LocalDate.now().atTime(16, 05));
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(any()))
                .thenReturn(past.plusDays(14).atTime(16, 0));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lr_1Lip()
                .setCoSClaimDetailsWithDate(true, false, past, null, true, false)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors().size()).isEqualTo(1);
            assertThat(params.getCaseData().getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(NO);
        }

        @Test
        void shouldIgnoreValidateCertificateOfService_whenDisabled() {
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(false);
            when(time.now()).thenReturn(LocalDateTime.now());
            LocalDate past = LocalDate.now().minusDays(1);
            LocalDate future = LocalDate.now().plusDays(1);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, true, past, future, true, true)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails2");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors().size()).isEqualTo(0);
        }

        @Test
        void shouldFailValidateCertificateOfService_whenDateIsFuture() {
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            when(time.now()).thenReturn(LocalDateTime.now());

            LocalDate past = LocalDate.now().minusDays(1);
            LocalDate future = LocalDate.now().plusDays(1);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, true, past, future, true, true)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails2");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors().size()).isEqualTo(2);
        }

        @Test
        void shouldFailValidateCertificateOfService_When1v2LIP_BothDefendant_DifferentDateOfService() {
            LocalDate def1pastDate = LocalDate.now().minusDays(1);
            LocalDate def2pastDate = LocalDate.now().minusDays(2);

            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            when(time.now()).thenReturn(LocalDateTime.now());
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(any()))
                .thenReturn(def2pastDate.plusDays(14).atTime(16, 0));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, true, def1pastDate, def2pastDate, true, true)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails2");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors().size()).isEqualTo(1);
        }

        @Test
        void shouldNotFailValidateCertificateOfService_When1v2LIP_BothDefendant_SameDateOfService() {
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            when(time.now()).thenReturn(LocalDateTime.now());

            LocalDate def1pastDate = LocalDate.now().minusDays(1);
            LocalDate def2pastDate = LocalDate.now().minusDays(1);

            when(deadlinesCalculator.plus14DaysAt4pmDeadline(any()))
                .thenReturn(def1pastDate.plusDays(14).atTime(16, 0));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, true, def1pastDate, def2pastDate, true, true)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails2");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors()).isEmpty();
        }

        @Test
        void shouldPassValidateCertificateOfService_whenHasFile() {
            LocalDate past = LocalDate.now().minusDays(1);
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            when(time.now()).thenReturn(LocalDateTime.now());
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(any()))
                .thenReturn(past.plusDays(14).atTime(16, 0));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, false, past, null, true, false)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors()).isEmpty();
        }

        @Test
        void shouldFailValidateCertificateOfService_whenHasNoFile() {
            LocalDate past = LocalDate.now().minusDays(1);
            when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);
            when(time.now()).thenReturn(LocalDateTime.now());
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(any()))
                .thenReturn(past.plusDays(14).atTime(16, 0));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, false, past, null, false, false)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors().size()).isEqualTo(1);
        }
    }
}
