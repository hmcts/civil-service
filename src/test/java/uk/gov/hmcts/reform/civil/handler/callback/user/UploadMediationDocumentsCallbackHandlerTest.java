package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsReferredInStatement;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType;
import uk.gov.hmcts.reform.civil.model.mediation.MediationNonAttendanceStatement;
import uk.gov.hmcts.reform.civil.model.mediation.UploadMediationDocumentsForm;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.mediation.UploadMediationService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.NON_ATTENDANCE_STATEMENT;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.REFERRED_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.utils.DynamicListUtils.listFromDynamicList;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.CLAIMANTS_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.CLAIMANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.DEFENDANTS_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.DEFENDANT_TWO_ID;

@ExtendWith(MockitoExtension.class)
class UploadMediationDocumentsCallbackHandlerTest extends BaseCallbackHandlerTest {

    private UploadMediationDocumentsCallbackHandler handler;

    private ObjectMapper objectMapper;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private UploadMediationService uploadMediationService;

    @Mock
    private UserService userService;

    @Mock
    private Time time;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        AssignCategoryId assignCategoryId = new AssignCategoryId();
        handler = new UploadMediationDocumentsCallbackHandler(coreCaseUserService, userService, objectMapper, time,
                                                              assignCategoryId, uploadMediationService);
    }

    private static final String PARTY_OPTIONS_PAGE = "populate-party-options";
    private static final String VALIDATE_DATES = "validate-dates";
    private static final String APPLICANT_SOLICITOR_ROLE = "[APPLICANTSOLICITORONE]";
    private static final String RESPONDENT_SOLICITOR_ONE_ROLE = "[RESPONDENTSOLICITORONE]";
    private static final String RESPONDENT_SOLICITOR_TWO_ROLE = "[RESPONDENTSOLICITORTWO]";
    private static final String APP1_CATEGORY_ID = "ClaimantOneMediationDocs";
    private static final String APP2_CATEGORY_ID = "ClaimantTwoMediationDocs";
    private static final String RES1_CATEGORY_ID = "DefendantOneMediationDocs";
    private static final String RES2_CATEGORY_ID = "DefendantTwoMediationDocs";

    private static final List<MediationDocumentsType> MEDIATION_NON_ATTENDANCE_OPTION = List.of(NON_ATTENDANCE_STATEMENT);
    private static final List<MediationDocumentsType> DOCUMENTS_REFERRED_OPTION = List.of(REFERRED_DOCUMENTS);
    private static final List<MediationDocumentsType> BOTH_DOCUMENTS_OPTION = List.of(NON_ATTENDANCE_STATEMENT, REFERRED_DOCUMENTS);

    private static final LocalDateTime DOCUMENT_UPLOADED_DATE = LocalDateTime.of(2023, 1, 1, 1, 1, 1);

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidEventCallback {

        @Nested
        class PopulatePartyOptions {
            @BeforeEach
            void setUp() {
                when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            }

            @Nested
            class ApplicantSolicitorInvokesEvent {

                @BeforeEach
                void setUp() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(APPLICANT_SOLICITOR_ROLE));
                }

                @Test
                void shouldReturnExpectedList_WhenInvokedFor1v1AsClaimantLR() {
                    CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

                    CallbackParams params = callbackParamsOf(caseData, MID, PARTY_OPTIONS_PAGE);
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);
                    CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
                    List<String> actual = listFromDynamicList(updatedData.getUploadMediationDocumentsForm().getUploadMediationDocumentsPartyChosen());

                    List<String> expected = List.of("Claimant 1: Mr. John Rambo");

                    assertThat(actual).isEqualTo(expected);
                }

                @Test
                void shouldReturnExpectedList_WhenInvokedFor2v1AsClaimantLR() {
                    CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                        .multiPartyClaimTwoApplicants()
                        .build();

                    CallbackParams params = callbackParamsOf(caseData, MID, PARTY_OPTIONS_PAGE);
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
                    List<String> actual = listFromDynamicList(updatedData.getUploadMediationDocumentsForm().getUploadMediationDocumentsPartyChosen());

                    List<String> expected = List.of("Claimant 1: Mr. John Rambo", "Claimant 2: Mr. Jason Rambo",
                                                    "Claimants 1 and 2");

                    assertThat(actual).isEqualTo(expected);

                }
            }

            @Nested
            class RespondentSolicitorInvokesEvent {

                @Test
                void shouldReturnExpectedList_WhenInvokedFor1v1AsRespondent1LR() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(RESPONDENT_SOLICITOR_ONE_ROLE));
                    CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

                    CallbackParams params = callbackParamsOf(caseData, MID, PARTY_OPTIONS_PAGE);
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);
                    CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
                    List<String> actual = listFromDynamicList(updatedData.getUploadMediationDocumentsForm().getUploadMediationDocumentsPartyChosen());

                    List<String> expected = List.of("Defendant 1: Mr. Sole Trader");

                    assertThat(actual).isEqualTo(expected);
                }

                @Test
                void shouldReturnExpectedList_WhenInvokedFor1v2SameSolAsRespondent1LR() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(RESPONDENT_SOLICITOR_ONE_ROLE, RESPONDENT_SOLICITOR_TWO_ROLE));
                    CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                        .multiPartyClaimOneDefendantSolicitor()
                        .build();

                    CallbackParams params = callbackParamsOf(caseData, MID, PARTY_OPTIONS_PAGE);
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);
                    CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
                    List<String> actual = listFromDynamicList(updatedData.getUploadMediationDocumentsForm().getUploadMediationDocumentsPartyChosen());

                    List<String> expected = List.of("Defendant 1: Mr. Sole Trader", "Defendant 2: Mr. John Rambo",
                                                    "Defendants 1 and 2");

                    assertThat(actual).isEqualTo(expected);
                }

                @Test
                void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolAsRespondent1LR() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(RESPONDENT_SOLICITOR_ONE_ROLE));
                    CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .build();

                    CallbackParams params = callbackParamsOf(caseData, MID, PARTY_OPTIONS_PAGE);
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);
                    CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
                    List<String> actual = listFromDynamicList(updatedData.getUploadMediationDocumentsForm().getUploadMediationDocumentsPartyChosen());

                    List<String> expected = List.of("Defendant 1: Mr. Sole Trader");

                    assertThat(actual).isEqualTo(expected);
                }

                @Test
                void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolAsRespondent2LR() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(RESPONDENT_SOLICITOR_TWO_ROLE));
                    CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .build();

                    CallbackParams params = callbackParamsOf(caseData, MID, PARTY_OPTIONS_PAGE);
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);
                    CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
                    List<String> actual = listFromDynamicList(updatedData.getUploadMediationDocumentsForm().getUploadMediationDocumentsPartyChosen());

                    List<String> expected = List.of("Defendant 2: Mr. John Rambo");

                    assertThat(actual).isEqualTo(expected);
                }
            }
        }

        @Nested
        class ValidateDates {

            @Test
            void shouldReturnErrors_whenDateForMediationNonAttendanceIsInFuture() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(CLAIMANT_ONE_ID, MEDIATION_NON_ATTENDANCE_OPTION)
                    .build();

                when(time.now()).thenReturn(LocalDateTime.of(2023, 4, 1, 1, 1, 1));

                CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_DATES);
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                List<String> actualErrors = response.getErrors();

                assertThat(actualErrors).hasSize(1);
                assertThat(actualErrors).containsExactly("Document date cannot be in the future");
            }

            @Test
            void shouldReturnErrors_whenDateForMediationDocsReferredIsInFuture() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(CLAIMANT_ONE_ID, DOCUMENTS_REFERRED_OPTION)
                    .build();

                when(time.now()).thenReturn(LocalDateTime.of(2023, 4, 1, 1, 1, 1));

                CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_DATES);
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                List<String> actualErrors = response.getErrors();

                assertThat(actualErrors).hasSize(1);
                assertThat(actualErrors).containsExactly("Document date cannot be in the future");
            }

            @Test
            void shouldReturnErrors_whenDateForAllMediationDocsIsInFuture() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(CLAIMANT_ONE_ID, BOTH_DOCUMENTS_OPTION)
                    .build();

                when(time.now()).thenReturn(LocalDateTime.of(2023, 4, 1, 1, 1, 1));

                CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_DATES);
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                List<String> actualErrors = response.getErrors();

                assertThat(actualErrors).hasSize(2);
                assertThat(actualErrors).containsExactly("Document date cannot be in the future", "Document date cannot be in the future");
            }

            @Test
            void shouldReturnNoErrors_whenDateForAllMediationDocsIsInPresent() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(CLAIMANT_ONE_ID, BOTH_DOCUMENTS_OPTION)
                    .build();

                when(time.now()).thenReturn(LocalDateTime.of(2023, 4, 2, 1, 1, 1));

                CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_DATES);
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                List<String> actualErrors = response.getErrors();

                assertThat(actualErrors).isNull();
            }

            @Test
            void shouldReturnNoErrors_whenDateForAllMediationDocsIsInPast() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(CLAIMANT_ONE_ID, BOTH_DOCUMENTS_OPTION)
                    .build();

                when(time.now()).thenReturn(LocalDateTime.of(2024, 4, 2, 1, 1, 1));

                CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_DATES);
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                List<String> actualErrors = response.getErrors();

                assertThat(actualErrors).isNull();
            }
        }
    }

    @Nested
    class AboutToSubmitCallback {

        private static final UploadMediationDocumentsForm EXPECTED_FORM =
            UploadMediationDocumentsForm.builder().build();

        @Nested
        class MediationNonAttendanceDocumentOption {

            private MediationNonAttendanceStatement getExpectedMediationNonattendanceDocOne(String categoryId) {

                return MediationNonAttendanceStatement.builder()
                    .yourName("My name")
                    .documentDate(LocalDate.of(2023, 4, 2))
                    .document(Document.builder()
                                  .documentFileName("Mediation non attendance")
                                  .categoryID(categoryId)
                                  .build())
                    .documentUploadedDatetime(DOCUMENT_UPLOADED_DATE)
                    .build();
            }

            private MediationNonAttendanceStatement getExpectedMediationNonattendanceDocTwo(String categoryId) {

                return MediationNonAttendanceStatement.builder()
                    .yourName("name")
                    .document(Document.builder()
                                  .documentFileName("Mediation non attendance 2")
                                  .categoryID(categoryId)
                                  .build())
                    .documentUploadedDatetime(DOCUMENT_UPLOADED_DATE)
                    .build();
            }

            @Test
            void shouldUploadApplicant1Documents_whenInvokedForMediationNonAttendance() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(CLAIMANT_ONE_ID, MEDIATION_NON_ATTENDANCE_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<MediationNonAttendanceStatement> actual = unwrapElements(updatedData.getApp1MediationNonAttendanceDocs());

                assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(1);
                assertThat(actual).containsExactly(getExpectedMediationNonattendanceDocOne(APP1_CATEGORY_ID));

                // Run event again adding second document
                UploadMediationDocumentsForm uploadMediationDocumentsForm = buildSecondMediationNonattendanceDoc(
                    CLAIMANT_ONE_ID,
                    MEDIATION_NON_ATTENDANCE_OPTION
                );
                CaseData secondEventData = updatedData.toBuilder().uploadMediationDocumentsForm(uploadMediationDocumentsForm).build();

                params = callbackParamsOf(secondEventData, ABOUT_TO_SUBMIT);

                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                secondEventData = objectMapper.convertValue(response.getData(), CaseData.class);
                actual = unwrapElements(updatedData.getApp1MediationNonAttendanceDocs());

                assertThat(secondEventData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(2);
                assertThat(actual).containsExactly(getExpectedMediationNonattendanceDocOne(APP1_CATEGORY_ID), getExpectedMediationNonattendanceDocTwo(APP1_CATEGORY_ID));
            }

            @Test
            void shouldUploadApplicant2Documents_whenInvokedForMediationNonAttendance2v1() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .multiPartyClaimTwoApplicants()
                    .uploadMediationDocumentsChooseOptions(CLAIMANT_TWO_ID, MEDIATION_NON_ATTENDANCE_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<MediationNonAttendanceStatement> actual = unwrapElements(updatedData.getApp2MediationNonAttendanceDocs());

                assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(1);
                assertThat(actual).containsExactly(getExpectedMediationNonattendanceDocOne(APP2_CATEGORY_ID));

                // Run event again adding second document
                UploadMediationDocumentsForm uploadMediationDocumentsForm = buildSecondMediationNonattendanceDoc(
                    CLAIMANT_TWO_ID,
                    MEDIATION_NON_ATTENDANCE_OPTION
                );
                CaseData secondEventData = updatedData.toBuilder().uploadMediationDocumentsForm(uploadMediationDocumentsForm).build();

                params = callbackParamsOf(secondEventData, ABOUT_TO_SUBMIT);

                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                secondEventData = objectMapper.convertValue(response.getData(), CaseData.class);
                actual = unwrapElements(updatedData.getApp2MediationNonAttendanceDocs());

                assertThat(secondEventData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(2);
                assertThat(actual).containsExactly(getExpectedMediationNonattendanceDocOne(APP2_CATEGORY_ID), getExpectedMediationNonattendanceDocTwo(APP2_CATEGORY_ID));
            }

            @Test
            void shouldUploadToBothApplicantDocuments_whenInvokedForMediationNonAttendanceBothClaimants2v1() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(CLAIMANTS_ID, MEDIATION_NON_ATTENDANCE_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<MediationNonAttendanceStatement> actual = unwrapElements(updatedData.getApp1MediationNonAttendanceDocs());
                List<MediationNonAttendanceStatement> app2actual = unwrapElements(updatedData.getApp2MediationNonAttendanceDocs());

                assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(1);
                assertThat(actual).containsExactly(getExpectedMediationNonattendanceDocOne(APP1_CATEGORY_ID));
                assertThat(app2actual).hasSize(1);
                assertThat(app2actual).containsExactly(getExpectedMediationNonattendanceDocOne(APP2_CATEGORY_ID));

                // Run event again adding second document
                UploadMediationDocumentsForm uploadMediationDocumentsForm = buildSecondMediationNonattendanceDoc(
                    CLAIMANTS_ID,
                    MEDIATION_NON_ATTENDANCE_OPTION
                );
                CaseData secondEventData = updatedData.toBuilder().uploadMediationDocumentsForm(uploadMediationDocumentsForm).build();

                params = callbackParamsOf(secondEventData, ABOUT_TO_SUBMIT);

                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                secondEventData = objectMapper.convertValue(response.getData(), CaseData.class);
                actual = unwrapElements(updatedData.getApp1MediationNonAttendanceDocs());
                app2actual = unwrapElements(updatedData.getApp2MediationNonAttendanceDocs());

                assertThat(secondEventData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(2);
                assertThat(app2actual).hasSize(2);
                assertThat(actual).containsExactly(getExpectedMediationNonattendanceDocOne(APP1_CATEGORY_ID), getExpectedMediationNonattendanceDocTwo(APP1_CATEGORY_ID));
                assertThat(app2actual).containsExactly(getExpectedMediationNonattendanceDocOne(APP2_CATEGORY_ID), getExpectedMediationNonattendanceDocTwo(APP2_CATEGORY_ID));
            }

            @Test
            void shouldUploadRespondent1Documents_whenInvokedForMediationNonAttendance() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(DEFENDANT_ONE_ID, MEDIATION_NON_ATTENDANCE_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<MediationNonAttendanceStatement> actual = unwrapElements(updatedData.getRes1MediationNonAttendanceDocs());

                assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(1);
                assertThat(actual).containsExactly(getExpectedMediationNonattendanceDocOne(RES1_CATEGORY_ID));

                // Run event again adding second document
                UploadMediationDocumentsForm uploadMediationDocumentsForm = buildSecondMediationNonattendanceDoc(
                    DEFENDANT_ONE_ID,
                    MEDIATION_NON_ATTENDANCE_OPTION
                );
                CaseData secondEventData = updatedData.toBuilder().uploadMediationDocumentsForm(uploadMediationDocumentsForm).build();

                params = callbackParamsOf(secondEventData, ABOUT_TO_SUBMIT);

                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                secondEventData = objectMapper.convertValue(response.getData(), CaseData.class);
                actual = unwrapElements(updatedData.getRes1MediationNonAttendanceDocs());

                assertThat(secondEventData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(2);
                assertThat(actual).containsExactly(getExpectedMediationNonattendanceDocOne(RES1_CATEGORY_ID), getExpectedMediationNonattendanceDocTwo(RES1_CATEGORY_ID));
            }

            @Test
            void shouldUploadRespondent2Documents_whenInvokedForMediationNonAttendance1v2DiffSolicitor() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(DEFENDANT_TWO_ID, MEDIATION_NON_ATTENDANCE_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<MediationNonAttendanceStatement> actual = unwrapElements(updatedData.getRes2MediationNonAttendanceDocs());

                assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(1);
                assertThat(actual).containsExactly(getExpectedMediationNonattendanceDocOne(RES2_CATEGORY_ID));

                // Run event again adding second document
                UploadMediationDocumentsForm uploadMediationDocumentsForm = buildSecondMediationNonattendanceDoc(
                    DEFENDANT_TWO_ID,
                    MEDIATION_NON_ATTENDANCE_OPTION
                );
                CaseData secondEventData = updatedData.toBuilder().uploadMediationDocumentsForm(uploadMediationDocumentsForm).build();

                params = callbackParamsOf(secondEventData, ABOUT_TO_SUBMIT);

                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                secondEventData = objectMapper.convertValue(response.getData(), CaseData.class);
                actual = unwrapElements(updatedData.getRes2MediationNonAttendanceDocs());

                assertThat(secondEventData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(2);
                assertThat(actual).containsExactly(getExpectedMediationNonattendanceDocOne(RES2_CATEGORY_ID), getExpectedMediationNonattendanceDocTwo(RES2_CATEGORY_ID));
            }

            @Test
            void shouldUploadRespondent1Documents_whenInvokedForMediationNonAttendanceBothDefendants1v2SameSolicitor() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(DEFENDANTS_ID, MEDIATION_NON_ATTENDANCE_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<MediationNonAttendanceStatement> actual = unwrapElements(updatedData.getRes1MediationNonAttendanceDocs());
                List<MediationNonAttendanceStatement> res2actual = unwrapElements(updatedData.getRes2MediationNonAttendanceDocs());

                assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(1);
                assertThat(actual).containsExactly(getExpectedMediationNonattendanceDocOne(RES1_CATEGORY_ID));
                assertThat(res2actual).hasSize(1);
                assertThat(res2actual).containsExactly(getExpectedMediationNonattendanceDocOne(RES2_CATEGORY_ID));

                // Run event again adding second document
                UploadMediationDocumentsForm uploadMediationDocumentsForm = buildSecondMediationNonattendanceDoc(
                    DEFENDANTS_ID,
                    MEDIATION_NON_ATTENDANCE_OPTION
                );
                CaseData secondEventData = updatedData.toBuilder().uploadMediationDocumentsForm(uploadMediationDocumentsForm).build();

                params = callbackParamsOf(secondEventData, ABOUT_TO_SUBMIT);

                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                secondEventData = objectMapper.convertValue(response.getData(), CaseData.class);
                actual = unwrapElements(updatedData.getRes1MediationNonAttendanceDocs());
                res2actual = unwrapElements(updatedData.getRes2MediationNonAttendanceDocs());

                assertThat(secondEventData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(2);
                assertThat(res2actual).hasSize(2);
                assertThat(actual).containsExactly(getExpectedMediationNonattendanceDocOne(RES1_CATEGORY_ID), getExpectedMediationNonattendanceDocTwo(RES1_CATEGORY_ID));
                assertThat(res2actual).containsExactly(getExpectedMediationNonattendanceDocOne(RES2_CATEGORY_ID), getExpectedMediationNonattendanceDocTwo(RES2_CATEGORY_ID));
            }
        }

        @Nested
        class DocumentsReferredOption {
            private MediationDocumentsReferredInStatement getExpectedReferredDocsOne(String categoryId) {
                return MediationDocumentsReferredInStatement.builder()
                    .documentType("type")
                    .document(Document.builder()
                                  .documentFileName("Referred documents")
                                  .categoryID(categoryId)
                                  .build())
                    .documentDate(LocalDate.of(2023, 4, 2))
                    .documentUploadedDatetime(DOCUMENT_UPLOADED_DATE)
                    .build();
            }

            private MediationDocumentsReferredInStatement getExpectedReferredDocsTwo(String categoryId) {
                return MediationDocumentsReferredInStatement.builder()
                    .documentUploadedDatetime(DOCUMENT_UPLOADED_DATE)
                    .documentType("another type")
                    .document(Document.builder()
                                  .documentFileName("Referred documents 2")
                                  .categoryID(categoryId)
                                  .build()
                    )
                    .build();
            }

            @Test
            void shouldUploadApplicant1Documents_whenInvokedForDocumentsReferred() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(CLAIMANT_ONE_ID, DOCUMENTS_REFERRED_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<MediationDocumentsReferredInStatement> actual = unwrapElements(updatedData.getApp1MediationDocumentsReferred());

                assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(1);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(APP1_CATEGORY_ID));

                // Run event again adding second document
                UploadMediationDocumentsForm uploadMediationDocumentsForm = buildSecondMediationNonattendanceDoc(
                    CLAIMANT_ONE_ID,
                    DOCUMENTS_REFERRED_OPTION
                );
                CaseData secondEventData = updatedData.toBuilder().uploadMediationDocumentsForm(uploadMediationDocumentsForm).build();

                params = callbackParamsOf(secondEventData, ABOUT_TO_SUBMIT);

                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                secondEventData = objectMapper.convertValue(response.getData(), CaseData.class);
                actual = unwrapElements(updatedData.getApp1MediationDocumentsReferred());

                assertThat(secondEventData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(2);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(APP1_CATEGORY_ID), getExpectedReferredDocsTwo(APP1_CATEGORY_ID));

            }

            @Test
            void shouldUploadApplicant1Documents_whenInvokedForDocumentsReferredWhenCarmIsEnable() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(CLAIMANT_ONE_ID, DOCUMENTS_REFERRED_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<MediationDocumentsReferredInStatement> actual = unwrapElements(updatedData.getApp1MediationDocumentsReferred());

                assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(1);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(APP1_CATEGORY_ID));

                // Run event again adding second document
                UploadMediationDocumentsForm uploadMediationDocumentsForm = buildSecondMediationNonattendanceDoc(
                    CLAIMANT_ONE_ID,
                    DOCUMENTS_REFERRED_OPTION
                );
                CaseData secondEventData = updatedData.toBuilder().uploadMediationDocumentsForm(uploadMediationDocumentsForm).build();

                params = callbackParamsOf(secondEventData, ABOUT_TO_SUBMIT);

                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                secondEventData = objectMapper.convertValue(response.getData(), CaseData.class);
                actual = unwrapElements(updatedData.getApp1MediationDocumentsReferred());

                assertThat(secondEventData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(2);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(APP1_CATEGORY_ID), getExpectedReferredDocsTwo(APP1_CATEGORY_ID));
                verify(uploadMediationService, times(2)).uploadMediationDocumentsTaskList(any());
            }

            @Test
            void shouldUploadApplicant2Documents_whenInvokedForDocumentsReferred2v1() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(CLAIMANT_TWO_ID, DOCUMENTS_REFERRED_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<MediationDocumentsReferredInStatement> actual = unwrapElements(updatedData.getApp2MediationDocumentsReferred());

                assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(1);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(APP2_CATEGORY_ID));

                // Run event again adding second document
                UploadMediationDocumentsForm uploadMediationDocumentsForm = buildSecondMediationNonattendanceDoc(
                    CLAIMANT_TWO_ID,
                    DOCUMENTS_REFERRED_OPTION
                );
                CaseData secondEventData = updatedData.toBuilder().uploadMediationDocumentsForm(uploadMediationDocumentsForm).build();

                params = callbackParamsOf(secondEventData, ABOUT_TO_SUBMIT);

                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                secondEventData = objectMapper.convertValue(response.getData(), CaseData.class);
                actual = unwrapElements(updatedData.getApp2MediationDocumentsReferred());

                assertThat(secondEventData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(2);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(APP2_CATEGORY_ID), getExpectedReferredDocsTwo(APP2_CATEGORY_ID));
            }

            @Test
            void shouldUploadApplicant1Documents_whenInvokedForDocumentsReferredBothClaimants2v1() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(CLAIMANTS_ID, DOCUMENTS_REFERRED_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<MediationDocumentsReferredInStatement> actual = unwrapElements(updatedData.getApp1MediationDocumentsReferred());
                List<MediationDocumentsReferredInStatement> app2actual = unwrapElements(updatedData.getApp2MediationDocumentsReferred());

                assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(1);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(APP1_CATEGORY_ID));
                assertThat(app2actual).hasSize(1);
                assertThat(app2actual).containsExactly(getExpectedReferredDocsOne(APP2_CATEGORY_ID));

                // Run event again adding second document
                UploadMediationDocumentsForm uploadMediationDocumentsForm = buildSecondMediationNonattendanceDoc(
                    CLAIMANTS_ID,
                    DOCUMENTS_REFERRED_OPTION
                );
                CaseData secondEventData = updatedData.toBuilder().uploadMediationDocumentsForm(uploadMediationDocumentsForm).build();

                params = callbackParamsOf(secondEventData, ABOUT_TO_SUBMIT);

                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                secondEventData = objectMapper.convertValue(response.getData(), CaseData.class);
                actual = unwrapElements(updatedData.getApp1MediationDocumentsReferred());
                app2actual = unwrapElements(updatedData.getApp2MediationDocumentsReferred());

                assertThat(secondEventData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(2);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(APP1_CATEGORY_ID), getExpectedReferredDocsTwo(APP1_CATEGORY_ID));
                assertThat(app2actual).hasSize(2);
                assertThat(app2actual).containsExactly(getExpectedReferredDocsOne(APP2_CATEGORY_ID), getExpectedReferredDocsTwo(APP2_CATEGORY_ID));

            }

            @Test
            void shouldUploadRespondent1Documents_whenInvokedForDocumentsReferred() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(DEFENDANT_ONE_ID, DOCUMENTS_REFERRED_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<MediationDocumentsReferredInStatement> actual = unwrapElements(updatedData.getRes1MediationDocumentsReferred());

                assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(1);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(RES1_CATEGORY_ID));

                // Run event again adding second document
                UploadMediationDocumentsForm uploadMediationDocumentsForm = buildSecondMediationNonattendanceDoc(
                    DEFENDANT_ONE_ID,
                    DOCUMENTS_REFERRED_OPTION
                );
                CaseData secondEventData = updatedData.toBuilder().uploadMediationDocumentsForm(uploadMediationDocumentsForm).build();

                params = callbackParamsOf(secondEventData, ABOUT_TO_SUBMIT);

                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                secondEventData = objectMapper.convertValue(response.getData(), CaseData.class);
                actual = unwrapElements(updatedData.getRes1MediationDocumentsReferred());

                assertThat(secondEventData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(2);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(RES1_CATEGORY_ID), getExpectedReferredDocsTwo(RES1_CATEGORY_ID));
            }

            @Test
            void shouldUploadRespondent1Documents_whenInvokedForDocumentsReferred_WhenCarmIsEnabled() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(DEFENDANT_ONE_ID, DOCUMENTS_REFERRED_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<MediationDocumentsReferredInStatement> actual = unwrapElements(updatedData.getRes1MediationDocumentsReferred());

                assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(1);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(RES1_CATEGORY_ID));

                // Run event again adding second document
                UploadMediationDocumentsForm uploadMediationDocumentsForm = buildSecondMediationNonattendanceDoc(
                    DEFENDANT_ONE_ID,
                    DOCUMENTS_REFERRED_OPTION
                );
                CaseData secondEventData = updatedData.toBuilder().uploadMediationDocumentsForm(uploadMediationDocumentsForm).build();

                params = callbackParamsOf(secondEventData, ABOUT_TO_SUBMIT);

                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                secondEventData = objectMapper.convertValue(response.getData(), CaseData.class);
                actual = unwrapElements(updatedData.getRes1MediationDocumentsReferred());

                assertThat(secondEventData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(2);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(RES1_CATEGORY_ID), getExpectedReferredDocsTwo(RES1_CATEGORY_ID));
                verify(uploadMediationService, times(2)).uploadMediationDocumentsTaskList(any());
            }

            @Test
            void shouldUploadRespondent2Documents_whenInvokedForDocumentsReferred1v2DiffSolicitor() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(DEFENDANT_TWO_ID, DOCUMENTS_REFERRED_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<MediationDocumentsReferredInStatement> actual = unwrapElements(updatedData.getRes2MediationDocumentsReferred());

                assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(1);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(RES2_CATEGORY_ID));

                // Run event again adding second document
                UploadMediationDocumentsForm uploadMediationDocumentsForm = buildSecondMediationNonattendanceDoc(
                    DEFENDANT_TWO_ID,
                    DOCUMENTS_REFERRED_OPTION
                );
                CaseData secondEventData = updatedData.toBuilder().uploadMediationDocumentsForm(uploadMediationDocumentsForm).build();

                params = callbackParamsOf(secondEventData, ABOUT_TO_SUBMIT);

                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                secondEventData = objectMapper.convertValue(response.getData(), CaseData.class);
                actual = unwrapElements(updatedData.getRes2MediationDocumentsReferred());

                assertThat(secondEventData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(2);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(RES2_CATEGORY_ID), getExpectedReferredDocsTwo(RES2_CATEGORY_ID));
            }

            @Test
            void shouldUploadRespondent1Documents_whenInvokedForDocumentsReferredBothDefendants1v2SameSolicitor() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .uploadMediationDocumentsChooseOptions(DEFENDANTS_ID, DOCUMENTS_REFERRED_OPTION)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

                List<MediationDocumentsReferredInStatement> actual = unwrapElements(updatedData.getRes1MediationDocumentsReferred());
                List<MediationDocumentsReferredInStatement> res2actual = unwrapElements(updatedData.getRes2MediationDocumentsReferred());

                assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(1);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(RES1_CATEGORY_ID));
                assertThat(res2actual).hasSize(1);
                assertThat(res2actual).containsExactly(getExpectedReferredDocsOne(RES2_CATEGORY_ID));

                // Run event again adding second document
                UploadMediationDocumentsForm uploadMediationDocumentsForm = buildSecondMediationNonattendanceDoc(
                    DEFENDANTS_ID,
                    DOCUMENTS_REFERRED_OPTION
                );
                CaseData secondEventData = updatedData.toBuilder().uploadMediationDocumentsForm(uploadMediationDocumentsForm).build();

                params = callbackParamsOf(secondEventData, ABOUT_TO_SUBMIT);

                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                secondEventData = objectMapper.convertValue(response.getData(), CaseData.class);
                actual = unwrapElements(updatedData.getRes1MediationDocumentsReferred());
                res2actual = unwrapElements(updatedData.getRes2MediationDocumentsReferred());

                assertThat(secondEventData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
                assertThat(actual).hasSize(2);
                assertThat(actual).containsExactly(getExpectedReferredDocsOne(RES1_CATEGORY_ID), getExpectedReferredDocsTwo(RES1_CATEGORY_ID));
                assertThat(res2actual).hasSize(2);
                assertThat(res2actual).containsExactly(getExpectedReferredDocsOne(RES2_CATEGORY_ID), getExpectedReferredDocsTwo(RES2_CATEGORY_ID));
            }
        }

        @Test
        void shouldAssignCategoryIds_whenDocumentExist_ClaimantOne_NonAtt() {
            // When
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .uploadMediationDocumentsChooseOptions(CLAIMANT_ONE_ID, MEDIATION_NON_ATTENDANCE_OPTION)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getApp1MediationNonAttendanceDocs().get(0).getValue().getDocument().getCategoryID())
                .isEqualTo("ClaimantOneMediationDocs");
        }

        @Test
        void shouldAssignCategoryIds_whenDocumentExist_ClaimantOne_DocRef() {
            // When
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .uploadMediationDocumentsChooseOptions(CLAIMANT_ONE_ID, DOCUMENTS_REFERRED_OPTION)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getApp1MediationDocumentsReferred().get(0).getValue().getDocument().getCategoryID())
                .isEqualTo("ClaimantOneMediationDocs");
        }

        @Test
        void shouldAssignCategoryIds_whenDocumentExist_ClaimantTwo_NonAtt() {
            // When
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .uploadMediationDocumentsChooseOptions(CLAIMANT_TWO_ID, MEDIATION_NON_ATTENDANCE_OPTION)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getApp2MediationNonAttendanceDocs().get(0).getValue().getDocument().getCategoryID())
                .isEqualTo("ClaimantTwoMediationDocs");
        }

        @Test
        void shouldAssignCategoryIds_whenDocumentExist_ClaimantTwo_DocRef() {
            // When
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .uploadMediationDocumentsChooseOptions(CLAIMANT_TWO_ID, DOCUMENTS_REFERRED_OPTION)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getApp2MediationDocumentsReferred().get(0).getValue().getDocument().getCategoryID())
                .isEqualTo("ClaimantTwoMediationDocs");
        }

        @Test
        void shouldAssignCategoryIds_whenDocumentExist_DefendantOne_NonAtt() {
            // When
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .uploadMediationDocumentsChooseOptions(DEFENDANT_ONE_ID, MEDIATION_NON_ATTENDANCE_OPTION)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getRes1MediationNonAttendanceDocs().get(0).getValue().getDocument().getCategoryID())
                .isEqualTo("DefendantOneMediationDocs");
        }

        @Test
        void shouldAssignCategoryIds_whenDocumentExist_DefendantOne_DocRef() {
            // When
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .uploadMediationDocumentsChooseOptions(DEFENDANT_ONE_ID, DOCUMENTS_REFERRED_OPTION)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getRes1MediationDocumentsReferred().get(0).getValue().getDocument().getCategoryID())
                .isEqualTo("DefendantOneMediationDocs");
        }

        @Test
        void shouldAssignCategoryIds_whenDocumentExist_DefendantTwo_NonAtt() {
            // When
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .uploadMediationDocumentsChooseOptions(DEFENDANT_TWO_ID, MEDIATION_NON_ATTENDANCE_OPTION)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getRes2MediationNonAttendanceDocs().get(0).getValue().getDocument().getCategoryID())
                .isEqualTo("DefendantTwoMediationDocs");
        }

        @Test
        void shouldAssignCategoryIds_whenDocumentExist_DefendantTwo_DocRef() {
            // When
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .uploadMediationDocumentsChooseOptions(DEFENDANT_TWO_ID, DOCUMENTS_REFERRED_OPTION)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(updatedData.getRes2MediationDocumentsReferred().get(0).getValue().getDocument().getCategoryID())
                .isEqualTo("DefendantTwoMediationDocs");
        }

        @Test
        void shouldClearChosenOptions_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .uploadMediationDocumentsChooseOptions(CLAIMANT_ONE_ID, BOTH_DOCUMENTS_OPTION)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getUploadMediationDocumentsForm()).isEqualTo(EXPECTED_FORM);
        }

        @Test
        void shouldReturnNoError_WhenAboutToSubmitIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .uploadMediationDocumentsChooseOptions(CLAIMANT_ONE_ID, BOTH_DOCUMENTS_OPTION)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    private UploadMediationDocumentsForm buildSecondMediationNonattendanceDoc(String partyChosen, List<MediationDocumentsType> documentType) {
        List<Element<MediationNonAttendanceStatement>> mediationNonAttendanceStatement;
        List<Element<MediationDocumentsReferredInStatement>> documentsReferred;
        if (documentType.contains(NON_ATTENDANCE_STATEMENT)) {
            mediationNonAttendanceStatement = wrapElements(MediationNonAttendanceStatement.builder()
                                                               .documentUploadedDatetime(DOCUMENT_UPLOADED_DATE)
                                                               .yourName("name")
                                                               .document(Document.builder()
                                                                             .documentFileName("Mediation non attendance 2")
                                                                             .build())
                                                               .build());
        } else {
            mediationNonAttendanceStatement = null;
        }
        if (documentType.contains(REFERRED_DOCUMENTS)) {
            documentsReferred = wrapElements(MediationDocumentsReferredInStatement.builder()
                                                 .documentUploadedDatetime(DOCUMENT_UPLOADED_DATE)
                                                 .documentType("another type")
                                                 .document(Document.builder()
                                                               .documentFileName("Referred documents 2")
                                                               .build())
                                                 .build());
        } else {
            documentsReferred = null;
        }

        return UploadMediationDocumentsForm.builder()
            .mediationDocumentsType(documentType)
            .uploadMediationDocumentsPartyChosen(DynamicList.builder()
                                                     .value(
                                                         DynamicListElement.builder()
                                                             .code(partyChosen)
                                                             .build())
                                                     .build())
            .nonAttendanceStatementForm(mediationNonAttendanceStatement)
            .documentsReferredForm(documentsReferred)
            .build();
    }
}
