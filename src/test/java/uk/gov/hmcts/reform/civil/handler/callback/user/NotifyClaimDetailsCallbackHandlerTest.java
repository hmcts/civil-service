package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.civil.config.TestJacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.model.DocumentWithRegex;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.ServiceOfDateValidationMessageUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
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
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@SpringBootTest(classes = {
    NotifyClaimDetailsCallbackHandler.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    TestJacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    AssignCategoryId.class
})
class NotifyClaimDetailsCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockitoBean
    private Time time;

    @MockitoBean
    private WorkingDayIndicator workingDayIndicator;

    @MockitoBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockitoBean
    private ServiceOfDateValidationMessageUtils serviceOfDateValidationMessageUtils;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private NotifyClaimDetailsCallbackHandler handler;

    @Autowired
    private ExitSurveyContentService exitSurveyContentService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AssignCategoryId assignCategoryId;

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

            assertThat(response.getData()).containsKey("defendantSolicitorNotifyClaimDetailsOptions");
        }

        @Test
        void shouldIncludeDefendantTwoOption_whenRespondentTwoExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            List<String> optionLabels = updatedData.getDefendantSolicitorNotifyClaimDetailsOptions().getListItems()
                .stream()
                .map(DynamicListElement::getLabel)
                .toList();

            assertThat(optionLabels).contains("Defendant Two: " + caseData.getRespondent2().getPartyName());
        }

        @Test
        void shouldNotIncludeDefendantTwoOption_whenRespondentTwoDoesNotExist() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v1()
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            List<String> optionLabels = updatedData.getDefendantSolicitorNotifyClaimDetailsOptions().getListItems()
                .stream()
                .map(DynamicListElement::getLabel)
                .toList();

            assertThat(optionLabels).noneMatch(label -> label.startsWith("Defendant Two: "));
        }
    }

    @Nested
    class MidEventValidateOptionsCallback {

        private static final String PAGE_ID = "validateNotificationOption";

        @Test
        void shouldThrowWarning_whenNotifyingOnlyOneRespondentSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getWarnings()).contains(NotifyClaimDetailsCallbackHandler.WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR);
        }

        @Test
        void shouldThrowWarningWhenSelectedOptionIsNotBoth() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build();
            String defendantOneOption = "Defendant One: " + caseData.getRespondent1().getPartyName();
            caseData.setDefendantSolicitorNotifyClaimDetailsOptions(
                DynamicList.fromList(
                    List.of("Both", defendantOneOption),
                    label -> label,
                    defendantOneOption,
                    false
                )
            );

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getWarnings()).contains(NotifyClaimDetailsCallbackHandler.WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR);
        }

        @Test
        void shouldNotThrowWarningWhenSelectedOptionIsBoth() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build();
            caseData.setDefendantSolicitorNotifyClaimDetailsOptions(
                DynamicList.fromList(
                    List.of("Both", "Defendant One: " + caseData.getRespondent1().getPartyName()),
                    label -> label,
                    "Both",
                    false
                )
            );

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getWarnings()).isEmpty();
        }
    }

    @Nested
    class MidEventParticularsOfClaimCallback {

        private final String pageId = "particulars-of-claim";

        @Test
        void shouldReturnErrors_whenNoDocuments() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnErrors_whenNoDocumentsBackwardsCompatible() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setUploadParticularsOfClaim(YES);
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(caseData.getUploadParticularsOfClaim()).isEqualTo(YES);
            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnErrors_whenParticularsOfClaimFieldsAreInErrorState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setServedDocumentFiles(new ServedDocumentFiles());
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnErrors_whenParticularsOfClaimFieldsAreInErrorStateBackwardsCompatible() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setUploadParticularsOfClaim(YES);
            caseData.setServedDocumentFiles(new ServedDocumentFiles());
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(caseData.getUploadParticularsOfClaim()).isEqualTo(YES);
            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnNoErrors_whenParticularOfClaimsFieldsAreValid() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            ServedDocumentFiles servedDocs = new ServedDocumentFiles();
            servedDocs.setParticularsOfClaimText("Some string");
            caseData.setServedDocumentFiles(servedDocs);
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoErrors_whenParticularOfClaimsFieldsAreValidBackwardsCompatible() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            ServedDocumentFiles servedDocs = new ServedDocumentFiles();
            caseData.setUploadParticularsOfClaim(YES);
            List<Element<Document>> particularsOfClaimDocuments = List.of(
                element(new Document()
                            .setDocumentUrl("legacy-url")
                            .setDocumentFileName("legacy-particulars.pdf")
                            .setDocumentBinaryUrl("legacy-binary-url"))
            );
            servedDocs.setParticularsOfClaimDocument(particularsOfClaimDocuments);
            caseData.setServedDocumentFiles(servedDocs);
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
            when(deadlinesCalculator.plus14DaysDeadline(localDateTime)).thenReturn(newDate);
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(localDateTime)).thenReturn(newDate);
            when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(6, localDateTime.toLocalDate()))
                .thenReturn(sixMonthDate);
            when(workingDayIndicator.isWeekend(any(LocalDate.class))).thenReturn(true);
            when(deadlinesCalculator.plusWorkingDays(localDateTime.toLocalDate(), 2))
                .thenReturn(LocalDate.of(2023, 10, 16));
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1()
                .setCoSClaimDetailsWithDate(true, false, localDateTime.toLocalDate(), localDateTime.toLocalDate(), null, null, true, false)
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
                .containsEntry("claimDismissedDeadline", sixMonthDate.format(ISO_DATE_TIME));
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked1v2DifferentSolicitor() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(new PartyBuilder().individual().build())
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
        void shouldUseLipPathFor1v2DifferentRep_whenOnlyDefendant2IsLip() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lr_1Lip()
                .respondent1Represented(YES)
                .respondent2Represented(YES)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getRespondent1ResponseDeadline()).isEqualTo(newDate);
            assertThat(updatedData.getRespondent2ResponseDeadline()).isEqualTo(newDate);
            assertThat(updatedData.getAddLegalRepDeadlineRes1()).isNull();
            assertThat(updatedData.getAddLegalRepDeadlineRes2()).isNull();
        }

        @Test
        void shouldNotSetRespondentDeadlines_whenRespondent1MissingAndRespondent2NotAdded() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
            caseData.setRespondent1(null);
            caseData.setRespondent2(new PartyBuilder().individual().build());
            caseData.setAddRespondent2(NO);
            caseData.setDefendant1LIPAtClaimIssued(YES);

            LocalDateTime originalAddLegalRepDeadlineRes1 = caseData.getAddLegalRepDeadlineRes1();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getRespondent1ResponseDeadline()).isNull();
            assertThat(updatedData.getRespondent2ResponseDeadline()).isNull();
            assertThat(updatedData.getAddLegalRepDeadlineRes1()).isEqualTo(originalAddLegalRepDeadlineRes1);
            assertThat(updatedData.getAddLegalRepDeadlineRes2()).isNull();
        }

        @Test
        void shouldUpdateCertificateOfService_and_documents_cos1_whenSubmitted() {
            LocalDate cosDate = localDateTime.minusDays(2).toLocalDate();
            LocalDate deemedDate = localDateTime.minusDays(2).toLocalDate();
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plus14DaysDeadline(deemedDate.atTime(15, 05)))
                .thenReturn(newDate.minusDays(2));
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                    .setCoSClaimDetailsWithDate(true, false, cosDate, deemedDate, null,  null, true, false)
                    .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getCosNotifyClaimDetails1()
                           .getCosSenderStatementOfTruthLabel()).contains("CERTIFIED");
            assertThat(updatedData.getServedDocumentFiles().getOther()).hasSize(1);
            assertThat(updatedData.getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(YES);
            assertThat(updatedData.getRespondent1ResponseDeadline()).isEqualTo(newDate.minusDays(2));
            assertThat(updatedData.getClaimDetailsNotificationDate()).isEqualTo(time.now());
        }

        @Test
        void shouldUpdateCertificateOfService_and_documents_cos2_whenSubmitted() {
            LocalDate cosDate = localDateTime.minusDays(2).toLocalDate();
            LocalDate deemedDate = localDateTime.minusDays(2).toLocalDate();
            LocalDate currentDate = LocalDate.now();

            when(deadlinesCalculator.plusWorkingDays(currentDate, 2))
                .thenReturn(LocalDate.now().plusDays(2));
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plus14DaysDeadline(cosDate.atTime(15, 05)))
                .thenReturn(newDate.minusDays(2));
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                    .setCoSClaimDetailsWithDate(false, true, null, null, cosDate, deemedDate, false, true)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getCosNotifyClaimDetails2()
                           .getCosSenderStatementOfTruthLabel()).contains("CERTIFIED");
            assertThat(updatedData.getServedDocumentFiles().getOther()).hasSize(1);
            assertThat(updatedData.getCosNotifyClaimDetails2().getCosDocSaved()).isEqualTo(YES);
            assertThat(updatedData.getNextDeadline()).isEqualTo(newDate.minusDays(2).toLocalDate());
            assertThat(updatedData.getClaimDetailsNotificationDate()).isEqualTo(time.now());
        }

        @Test
        void shouldUpdate_to_earliest_day_cos2_is_earliest_whenSubmitted() {
            LocalDate cos1Date = localDateTime.minusDays(2).toLocalDate();
            LocalDate cos2Date = localDateTime.minusDays(3).toLocalDate();
            LocalDate deemed1Date = localDateTime.minusDays(2).toLocalDate();
            LocalDate deemed2Date = localDateTime.minusDays(3).toLocalDate();
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plus14DaysDeadline(deemed1Date.atTime(15, 05)))
                    .thenReturn(newDate.minusDays(2));
            when(deadlinesCalculator.plus14DaysDeadline(deemed2Date.atTime(15, 05)))
                    .thenReturn(newDate.minusDays(3));

            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                    .setCoSClaimDetailsWithDate(true, true, cos1Date, deemed1Date, cos2Date, deemed2Date, true, true)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getServedDocumentFiles().getOther()).hasSize(2);
            assertThat(updatedData.getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(YES);
            assertThat(updatedData.getCosNotifyClaimDetails2().getCosDocSaved()).isEqualTo(YES);
            assertThat(updatedData.getRespondent1ResponseDeadline()).isEqualTo(newDate.minusDays(3));
            assertThat(updatedData.getClaimDetailsNotificationDate()).isEqualTo(time.now());
        }

        @Test
        void shouldUpdate_to_earliest_day_cos1_is_earliest_whenSubmitted() {
            LocalDate cos1Date = localDateTime.minusDays(3).toLocalDate();
            LocalDate cos2Date = localDateTime.minusDays(2).toLocalDate();
            LocalDate deemed1Date = localDateTime.minusDays(2).toLocalDate();
            LocalDate deemed2Date = localDateTime.minusDays(3).toLocalDate();
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plus14DaysDeadline(cos1Date.atTime(15, 05)))
                .thenReturn(newDate.minusDays(3));
            when(deadlinesCalculator.plus14DaysDeadline(cos2Date.atTime(15, 05)))
                .thenReturn(newDate.minusDays(2));

            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                    .setCoSClaimDetailsWithDate(true, true, cos1Date, deemed1Date, cos2Date, deemed2Date, true, true)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getServedDocumentFiles().getOther()).hasSize(2);
            assertThat(updatedData.getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(YES);
            assertThat(updatedData.getCosNotifyClaimDetails2().getCosDocSaved()).isEqualTo(YES);
            assertThat(updatedData.getRespondent1ResponseDeadline()).isEqualTo(newDate.minusDays(3));
            assertThat(updatedData.getClaimDetailsNotificationDate()).isEqualTo(time.now());
        }

        static Stream<Arguments> caseDataStream() {
            DocumentWithRegex documentRegex = new DocumentWithRegex(new Document()
                                                                        .setDocumentUrl("fake-url")
                                                                        .setDocumentFileName("file-name")
                                                                        .setDocumentBinaryUrl("binary-url"));
            List<Element<DocumentWithRegex>> documentList = new ArrayList<>();
            List<Element<Document>> documentList2 = new ArrayList<>();
            documentList.add(element(documentRegex));
            documentList2.add(element(new Document()
                                          .setDocumentUrl("fake-url")
                                          .setDocumentFileName("file-name")
                                          .setDocumentBinaryUrl("binary-url")));

            ServedDocumentFiles documentToUpload = new ServedDocumentFiles();
            documentToUpload.setParticularsOfClaimDocument(documentList2);
            documentToUpload.setMedicalReport(documentList);
            documentToUpload.setScheduleOfLoss(documentList);
            documentToUpload.setCertificateOfSuitability(documentList);
            documentToUpload.setOther(documentList);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setUploadParticularsOfClaim(YES);
            caseData.setServedDocumentFiles(documentToUpload);
            return Stream.of(
                arguments(caseData)
            );
        }

        @ParameterizedTest
        @MethodSource("caseDataStream")
        void shouldAssignCategoryIds_whenDocumentExist(CaseData caseData) {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
            // When
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getServedDocumentFiles().getParticularsOfClaimDocument().get(0).getValue()
                           .getCategoryID()).isEqualTo("particularsOfClaim");
            assertThat(updatedData.getServedDocumentFiles().getMedicalReport().get(0).getValue().getDocument()
                           .getCategoryID()).isEqualTo("particularsOfClaim");
            assertThat(updatedData.getServedDocumentFiles().getScheduleOfLoss().get(0).getValue().getDocument()
                           .getCategoryID()).isEqualTo("particularsOfClaim");
            assertThat(updatedData.getServedDocumentFiles().getCertificateOfSuitability().get(0).getValue().getDocument()
                           .getCategoryID()).isEqualTo("particularsOfClaim");
            assertThat(updatedData.getServedDocumentFiles().getOther().get(0).getValue().getDocument()
                           .getCategoryID()).isEqualTo("particularsOfClaim");

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
        void shouldReturnExpectedResponse_whenInvokedWithNullDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setRespondent1ResponseDeadline(null);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String confirmationBody = format(CONFIRMATION_SUMMARY, "N/A")
                + exitSurveyContentService.applicantSurvey();

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# Defendant notified%n## Claim number: 000DC001"))
                    .confirmationBody(confirmationBody)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_with_cos_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lip_1Lr()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationHeader()).contains("Certificate of Service");
            assertThat(response.getConfirmationBody()).doesNotContain(exitSurveyContentService.applicantSurvey());
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
            when(time.now()).thenReturn(LocalDateTime.now());
            when(deadlinesCalculator.plus14DaysDeadline(any()))
                .thenReturn(past.plusDays(14).atTime(16, 0));
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, true, past, past, past, past, true, true)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationHeader()).contains("Certificate of Service");
        }

        @Test
        void shouldReturnCoSConfirmation_1Lip1Lr_whenCosNotifyDetailsSuccess() {
            LocalDate past = LocalDate.now().minusDays(1);
            when(time.now()).thenReturn(LocalDateTime.now());
            when(deadlinesCalculator.plus14DaysDeadline(any()))
                .thenReturn(past.plusDays(14).atTime(16, 0));
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lip_1Lr()
                .setCoSClaimDetailsWithDate(true, false, past, past, null, null, true, false)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationHeader()).contains("Certificate of Service");
        }
    }

    @Nested
    class MidEventValidateCos {
        public static final String DATE_OF_SERVICE_NOT_GREATER_THAN_2_WORKING_DAYS =
            "The date of service must be no greater than 2 working days in the future";

        public static final String DATE_OF_SERVICE_DATE_OLDER_THAN_14DAYS =
            "The date of service should not be more than 14 days old";

        public static final String DATE_OF_SERVICE_DATE_IS_WORKING_DAY =
            "For the date of service please enter a working day";

        @Test
        void shouldPassValidateCertificateOfService_whenDateIsPast() {

            LocalDate past = LocalDate.now().minusDays(1);
            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 2))
                .thenReturn(LocalDate.now().plusDays(2));
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, true, past, past, past, past, true, true)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails2");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(successResponse.getErrors()).isEmpty();
            assertThat(params.getCaseData().getCosNotifyClaimDetails2().getCosDocSaved()).isEqualTo(NO);
        }

        @Test
        void shouldPassValidateCertificateOfService_1Lip1Lr_whenDateIsPast() {
            LocalDate past = LocalDate.now().minusDays(1);

            when(time.now()).thenReturn(LocalDate.now().atTime(16, 05));
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 2))
                .thenReturn(LocalDate.now().plusDays(2));
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lip_1Lr()
                .setCoSClaimDetailsWithDate(true, false, past, past, null, null, true, false)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(successResponse.getErrors()).isEmpty();
            assertThat(params.getCaseData().getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(NO);
        }

        @Test
        void shouldPassValidateCertificateOfService_1Lr1Lip_whenServiceDateIsPast_notOlderThan14Days() {
            LocalDate past = LocalDate.now().minusDays(1);

            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 2))
                .thenReturn(LocalDate.now().plusDays(2));
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lr_1Lip()
                .setCoSClaimDetailsWithDate(true, false, past, past, null, null, true, false)
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

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lr_1Lip()
                .setCoSClaimDetailsWithDate(true, false, past, past, null, null, true, false)
                .build();
            CertificateOfService certificateOfServiceDef = caseData.getCosNotifyClaimDetails1();

            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(serviceOfDateValidationMessageUtils.getServiceOfDateValidationMessages(certificateOfServiceDef))
                .thenReturn(List.of(ServiceOfDateValidationMessageUtils.DATE_OF_SERVICE_DATE_OLDER_THAN_14DAYS,
                                    ServiceOfDateValidationMessageUtils.DOC_SERVED_DATE_OLDER_THAN_14DAYS));

            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors()).hasSize(2);
            assertThat(params.getCaseData().getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(NO);
        }

        @Test
        void shouldNotPassValidateCertificateOfService_1Lr1Lip_whenServiceDateIsPast_deadlineTodayDate_After16hrs() {
            LocalDate past = LocalDate.now().minusDays(14);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lr_1Lip()
                .setCoSClaimDetailsWithDate(true, false, past, past, null, null, true, false)
                .build();
            CertificateOfService certificateOfServiceDef = caseData.getCosNotifyClaimDetails1();

            when(time.now()).thenReturn(LocalDate.now().atTime(17, 05));
            when(serviceOfDateValidationMessageUtils.getServiceOfDateValidationMessages(certificateOfServiceDef))
                .thenReturn(List.of(ServiceOfDateValidationMessageUtils.DATE_OF_SERVICE_DATE_OLDER_THAN_14DAYS,
                                    ServiceOfDateValidationMessageUtils.DOC_SERVED_DATE_OLDER_THAN_14DAYS));

            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors()).hasSize(2);
            assertThat(params.getCaseData().getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(NO);
        }

        @Test
        void shouldFailValidateCertificateOfService_When1v2LIP_BothDefendant_DifferentDateOfService() {
            LocalDate def1pastDate = LocalDate.now().minusDays(1);
            LocalDate def2pastDate = LocalDate.now().minusDays(2);

            when(time.now()).thenReturn(LocalDateTime.now());
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 2))
                .thenReturn(LocalDate.now().plusDays(2));
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, true, def1pastDate, def1pastDate, def2pastDate, def2pastDate, true, true)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails2");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors()).hasSize(1);
        }

        @Test
        void shouldNotFailValidateCertificateOfService_When1v2LIP_BothDefendant_SameDateOfService() {
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 2))
                .thenReturn(LocalDate.now().plusDays(2));
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);
            when(time.now()).thenReturn(LocalDateTime.now());

            LocalDate def1pastDate = LocalDate.now().minusDays(1);
            LocalDate def2pastDate = LocalDate.now().minusDays(1);

            when(deadlinesCalculator.plus14DaysDeadline(any()))
                .thenReturn(def1pastDate.plusDays(14).atTime(16, 0));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, true, def1pastDate, def1pastDate, def2pastDate, def2pastDate, true, true)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails2");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors()).isEmpty();
        }

        @Test
        void shouldNotApplyBothDefendantLipValidation_whenOnlyOneDefendantIsLip() {
            LocalDate past = LocalDate.now().minusDays(1);
            when(time.now()).thenReturn(LocalDateTime.now());
            when(serviceOfDateValidationMessageUtils.getServiceOfDateValidationMessages(any()))
                .thenReturn(List.of());

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lr_1Lip()
                .setCoSClaimDetailsWithDate(true, true, past, past, past.minusDays(1), past.minusDays(1), true, true)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails2");

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getErrors()).doesNotContain(NotifyClaimDetailsCallbackHandler.BOTH_CERTIFICATE_SERVED_SAME_DATE);
        }

        @Test
        void shouldFailBothDefendantLipValidation_whenDefendant1DateIsMissing() {
            LocalDate past = LocalDate.now().minusDays(1);
            when(time.now()).thenReturn(LocalDateTime.now());
            when(serviceOfDateValidationMessageUtils.getServiceOfDateValidationMessages(any()))
                .thenReturn(List.of());

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, true, past, past, past, past, true, true)
                .build();
            caseData.setCosNotifyClaimDetails1(null);

            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails2");
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains(NotifyClaimDetailsCallbackHandler.BOTH_CERTIFICATE_SERVED_SAME_DATE);
        }

        @Test
        void shouldPassValidateCertificateOfService_whenHasFile() {
            LocalDate past = LocalDate.now().minusDays(1);
            when(time.now()).thenReturn(LocalDateTime.now());
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 2))
                .thenReturn(LocalDate.now().plusDays(2));
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, false, past, past, null, null, true, false)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors()).isEmpty();
        }

        @Test
        void shouldFailValidateCertificateOfService_whenHasNoFile() {
            LocalDate past = LocalDate.now().minusDays(1);
            when(time.now()).thenReturn(LocalDateTime.now());
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 2))
                .thenReturn(LocalDate.now().plusDays(2));
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothCoS()
                .setCoSClaimDetailsWithDate(true, false, past, past, null, null, false, false)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors()).hasSize(1);
        }

        @Test
        void shouldNotPassValidateCertificateOfService_1Lr1Lip_whenDeemedDateIsPast_deadline() {
            LocalDate currentDate = LocalDate.now();
            LocalDate deemedServedDate = currentDate.minusDays(15);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lip_1Lr()
                .setCoSClaimDetailsWithDate(true, false, currentDate, deemedServedDate, null, null, true, false)
                .build();
            CertificateOfService certificateOfServiceDef = caseData.getCosNotifyClaimDetails1();

            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(serviceOfDateValidationMessageUtils.getServiceOfDateValidationMessages(certificateOfServiceDef))
                .thenReturn(List.of(ServiceOfDateValidationMessageUtils.DATE_OF_SERVICE_DATE_OLDER_THAN_14DAYS));

            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors()).hasSize(1);
            assertThat(successResponse.getErrors()).contains(DATE_OF_SERVICE_DATE_OLDER_THAN_14DAYS);
            assertThat(params.getCaseData().getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(NO);
        }

        @Test
        void shouldNotPassValidateCertificateOfService_1Lr1Lip_whenDeemedDateNotInWorkingDay() {
            LocalDate currentDate = LocalDate.now();
            LocalDate deemedServedDate = currentDate;

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lip_1Lr()
                .setCoSClaimDetailsWithDate(true, false, currentDate, deemedServedDate, null, null, true, false)
                .build();
            CertificateOfService certificateOfServiceDef = caseData.getCosNotifyClaimDetails1();

            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(serviceOfDateValidationMessageUtils.getServiceOfDateValidationMessages(certificateOfServiceDef))
                .thenReturn(List.of(ServiceOfDateValidationMessageUtils.DATE_OF_SERVICE_DATE_IS_WORKING_DAY));

            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors()).hasSize(1);
            assertThat(successResponse.getErrors()).contains(DATE_OF_SERVICE_DATE_IS_WORKING_DAY);
            assertThat(params.getCaseData().getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(NO);
        }

        @Test
        void shouldNotPassValidateCertificateOfService_1Lr1Lip_whenDeemedDateExceeds2WorkingDays() {
            LocalDate currentDate = LocalDate.now();
            LocalDate deemedServedDate = currentDate.plusDays(5);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_1Lip_1Lr()
                .setCoSClaimDetailsWithDate(true, false, currentDate, deemedServedDate, null, null, true, false)
                .build();
            CertificateOfService certificateOfServiceDef = caseData.getCosNotifyClaimDetails1();

            when(time.now()).thenReturn(LocalDate.now().atTime(15, 05));
            when(serviceOfDateValidationMessageUtils.getServiceOfDateValidationMessages(certificateOfServiceDef))
                .thenReturn(List.of(ServiceOfDateValidationMessageUtils.DATE_OF_SERVICE_NOT_GREATER_THAN_2_WORKING_DAYS));

            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse successResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(successResponse.getErrors()).hasSize(1);
            assertThat(successResponse.getErrors()).contains(DATE_OF_SERVICE_NOT_GREATER_THAN_2_WORKING_DAYS);
            assertThat(params.getCaseData().getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(NO);
        }

        @Test
        void shouldSetCosDocSavedToNoAndAddMandatoryError_whenDefendant1CosExistsWithoutEvidence() {
            when(serviceOfDateValidationMessageUtils.getServiceOfDateValidationMessages(any()))
                .thenReturn(List.of());

            CaseData caseData = CaseData.builder()
                .cosNotifyClaimDetails1(new CertificateOfService().setCosDocSaved(YES))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(caseData.getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(NO);
            assertThat(response.getErrors()).contains(NotifyClaimDetailsCallbackHandler.DOC_SERVED_MANDATORY);
        }

        @Test
        void shouldNotUpdateCosForDefendant1_whenCosIsMissing() {
            when(serviceOfDateValidationMessageUtils.getServiceOfDateValidationMessages(any()))
                .thenReturn(List.of());

            CaseData caseData = CaseData.builder().build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails1");
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(caseData.getCosNotifyClaimDetails1()).isNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldSetCosDocSavedToNoAndAddMandatoryError_whenDefendant2CosExistsWithoutEvidence() {
            when(serviceOfDateValidationMessageUtils.getServiceOfDateValidationMessages(any()))
                .thenReturn(List.of());

            CaseData caseData = CaseData.builder()
                .defendant1LIPAtClaimIssued(NO)
                .defendant2LIPAtClaimIssued(NO)
                .cosNotifyClaimDetails2(new CertificateOfService().setCosDocSaved(YES))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails2");
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(caseData.getCosNotifyClaimDetails2().getCosDocSaved()).isEqualTo(NO);
            assertThat(response.getErrors()).contains(NotifyClaimDetailsCallbackHandler.DOC_SERVED_MANDATORY);
        }

        @Test
        void shouldNotUpdateCosForDefendant2_whenCosIsMissing() {
            when(serviceOfDateValidationMessageUtils.getServiceOfDateValidationMessages(any()))
                .thenReturn(List.of());

            CaseData caseData = CaseData.builder()
                .defendant1LIPAtClaimIssued(NO)
                .defendant2LIPAtClaimIssued(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validateCosNotifyClaimDetails2");
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(caseData.getCosNotifyClaimDetails2()).isNull();
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class PrivateMethodBranchCoverage {

        @Test
        void shouldReturnFalseForConfirmationForLip_whenBothLipFlagsAreNo() {
            boolean confirmationForLip = ReflectionTestUtils.invokeMethod(
                handler,
                "isConfirmationForLip",
                CaseData.builder()
                    .defendant1LIPAtClaimIssued(NO)
                    .defendant2LIPAtClaimIssued(NO)
                    .build()
            );

            assertThat(confirmationForLip).isFalse();
        }

        @Test
        void shouldReturnFalseForBothDefendantLip_whenDefendant1LipFlagIsMissing() {
            boolean bothDefendantLip = ReflectionTestUtils.invokeMethod(
                handler,
                "isBothDefendantLip",
                CaseData.builder()
                    .defendant2LIPAtClaimIssued(YES)
                    .build()
            );

            assertThat(bothDefendantLip).isFalse();
        }

        @Test
        void shouldReturnFalseForBothDefendantLip_whenDefendant2LipFlagIsNo() {
            boolean bothDefendantLip = ReflectionTestUtils.invokeMethod(
                handler,
                "isBothDefendantLip",
                CaseData.builder()
                    .defendant1LIPAtClaimIssued(YES)
                    .defendant2LIPAtClaimIssued(NO)
                    .build()
            );

            assertThat(bothDefendantLip).isFalse();
        }

        @Test
        void shouldReturnFalseForBothDefendantLip_whenDefendant2LipFlagIsMissing() {
            boolean bothDefendantLip = ReflectionTestUtils.invokeMethod(
                handler,
                "isBothDefendantLip",
                CaseData.builder()
                    .defendant1LIPAtClaimIssued(YES)
                    .build()
            );

            assertThat(bothDefendantLip).isFalse();
        }

        @Test
        void shouldReturnFalseForSameDateOfService_whenOnlyDefendant1CosExists() {
            LocalDate today = LocalDate.now();
            boolean sameDateOfService = ReflectionTestUtils.invokeMethod(
                handler,
                "isBothDefendantWithSameDateOfService",
                CaseData.builder()
                    .cosNotifyClaimDetails1(new CertificateOfService()
                                                .setCosDateDeemedServedForDefendant(today))
                    .build()
            );

            assertThat(sameDateOfService).isFalse();
        }

        @Test
        void shouldReturnCurrentTimeWhenDeemedDatesAreMissing_onBothCosObjects() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 10, 10, 0);
            when(time.now()).thenReturn(now);

            CaseData caseData = CaseData.builder()
                .cosNotifyClaimDetails1(new CertificateOfService())
                .cosNotifyClaimDetails2(new CertificateOfService())
                .build();

            LocalDateTime earliestDate = ReflectionTestUtils.invokeMethod(
                handler,
                "getEarliestDateOfService",
                caseData
            );

            assertThat(earliestDate).isEqualTo(now);
        }

        @Test
        void shouldReturnSecondDeemedDateWhenFirstIsAfterSecond() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 10, 9, 30);
            when(time.now()).thenReturn(now);

            LocalDate defendant1DeemedDate = LocalDate.of(2024, 1, 12);
            LocalDate defendant2DeemedDate = LocalDate.of(2024, 1, 11);

            CaseData caseData = CaseData.builder()
                .cosNotifyClaimDetails1(new CertificateOfService()
                                            .setCosDateDeemedServedForDefendant(defendant1DeemedDate))
                .cosNotifyClaimDetails2(new CertificateOfService()
                                            .setCosDateDeemedServedForDefendant(defendant2DeemedDate))
                .build();

            LocalDateTime earliestDate = ReflectionTestUtils.invokeMethod(
                handler,
                "getEarliestDateOfService",
                caseData
            );

            assertThat(earliestDate).isEqualTo(defendant2DeemedDate.atTime(now.toLocalTime()));
        }

        @Test
        void shouldReturnFirstDeemedDateWhenFirstIsBeforeSecond() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 10, 9, 30);
            when(time.now()).thenReturn(now);

            LocalDate defendant1DeemedDate = LocalDate.of(2024, 1, 10);
            LocalDate defendant2DeemedDate = LocalDate.of(2024, 1, 11);

            CaseData caseData = CaseData.builder()
                .cosNotifyClaimDetails1(new CertificateOfService()
                                            .setCosDateDeemedServedForDefendant(defendant1DeemedDate))
                .cosNotifyClaimDetails2(new CertificateOfService()
                                            .setCosDateDeemedServedForDefendant(defendant2DeemedDate))
                .build();

            LocalDateTime earliestDate = ReflectionTestUtils.invokeMethod(
                handler,
                "getEarliestDateOfService",
                caseData
            );

            assertThat(earliestDate).isEqualTo(defendant1DeemedDate.atTime(now.toLocalTime()));
        }

        @Test
        void shouldInitialiseServedDocumentFilesWhenMissingInSaveCosDetailsDoc() {
            Document evidenceDocument = new Document()
                .setDocumentUrl("fake-url")
                .setDocumentFileName("evidence.pdf")
                .setDocumentBinaryUrl("binary-url");

            List<Element<Document>> evidence = new ArrayList<>();
            evidence.add(element(evidenceDocument));

            CaseData caseData = CaseData.builder()
                .cosNotifyClaimDetails1(new CertificateOfService().setCosEvidenceDocument(evidence))
                .servedDocumentFiles(null)
                .build();

            ReflectionTestUtils.invokeMethod(
                handler,
                "saveCoSDetailsDoc",
                caseData,
                1
            );

            assertThat(caseData.getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(YES);
            assertThat(caseData.getServedDocumentFiles()).isNotNull();
            assertThat(caseData.getServedDocumentFiles().getOther()).hasSize(1);
        }
    }
}
