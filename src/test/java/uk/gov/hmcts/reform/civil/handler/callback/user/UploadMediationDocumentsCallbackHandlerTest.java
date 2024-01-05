package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    UploadMediationDocumentsCallbackHandler.class,
    JacksonAutoConfiguration.class,
    AssignCategoryId.class
})
class UploadMediationDocumentsCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private UploadMediationDocumentsCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssignCategoryId assignCategoryId;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private Time time;

    private static final String PARTY_OPTIONS_PAGE = "populate-party-options";
    private static final String VALIDATE_DATES = "validate-dates";
    private static final String APPLICANT_SOLICITOR_ROLE = "[APPLICANTSOLICITORONE]";
    private static final String RESPONDENT_SOLICITOR_ONE_ROLE = "[RESPONDENTSOLICITORONE]";
    private static final String RESPONDENT_SOLICITOR_TWO_ROLE = "[RESPONDENTSOLICITORTWO]";

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

            private static final MediationNonAttendanceStatement EXPECTED_MEDIATION_NONATTENDANCE_DOC_ONE =
                MediationNonAttendanceStatement.builder()
                    .documentUploadedDatetime(DOCUMENT_UPLOADED_DATE)
                    .yourName("My name")
                    .document(Document.builder()
                                  .documentFileName("Mediation non attendance")
                                  .build())
                    .documentDate(LocalDate.of(2023, 4, 2))
                    .build();

            private static final MediationNonAttendanceStatement EXPECTED_MEDIATION_NONATTENDANCE_DOC_TWO =
                MediationNonAttendanceStatement.builder()
                    .yourName("name")
                    .documentUploadedDatetime(DOCUMENT_UPLOADED_DATE)
                    .build();

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
                assertThat(actual).containsExactly(EXPECTED_MEDIATION_NONATTENDANCE_DOC_ONE);

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
                assertThat(actual).containsExactly(EXPECTED_MEDIATION_NONATTENDANCE_DOC_ONE, EXPECTED_MEDIATION_NONATTENDANCE_DOC_TWO);
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
                assertThat(actual).containsExactly(EXPECTED_MEDIATION_NONATTENDANCE_DOC_ONE);

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
                assertThat(actual).containsExactly(EXPECTED_MEDIATION_NONATTENDANCE_DOC_ONE, EXPECTED_MEDIATION_NONATTENDANCE_DOC_TWO);
            }

            @Test
            void shouldUploadApplicant1Documents_whenInvokedForMediationNonAttendanceBothClaimants2v1() {
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
                assertThat(actual).containsExactly(EXPECTED_MEDIATION_NONATTENDANCE_DOC_ONE);
                assertThat(app2actual).hasSize(0);

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
                assertThat(app2actual).hasSize(0);
                assertThat(actual).containsExactly(EXPECTED_MEDIATION_NONATTENDANCE_DOC_ONE, EXPECTED_MEDIATION_NONATTENDANCE_DOC_TWO);
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
                assertThat(actual).containsExactly(EXPECTED_MEDIATION_NONATTENDANCE_DOC_ONE);

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
                assertThat(actual).containsExactly(EXPECTED_MEDIATION_NONATTENDANCE_DOC_ONE, EXPECTED_MEDIATION_NONATTENDANCE_DOC_TWO);
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
                assertThat(actual).containsExactly(EXPECTED_MEDIATION_NONATTENDANCE_DOC_ONE);

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
                assertThat(actual).containsExactly(EXPECTED_MEDIATION_NONATTENDANCE_DOC_ONE, EXPECTED_MEDIATION_NONATTENDANCE_DOC_TWO);
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
                assertThat(actual).containsExactly(EXPECTED_MEDIATION_NONATTENDANCE_DOC_ONE);
                assertThat(res2actual).hasSize(0);

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
                assertThat(res2actual).hasSize(0);
                assertThat(actual).containsExactly(EXPECTED_MEDIATION_NONATTENDANCE_DOC_ONE, EXPECTED_MEDIATION_NONATTENDANCE_DOC_TWO);
            }
        }

        @Nested
        class DocumentsReferredOption {
            private static final MediationDocumentsReferredInStatement EXPECTED_REFFERED_DOCS_ONE =
                MediationDocumentsReferredInStatement.builder()
                    .documentType("type")
                    .document(Document.builder()
                                  .documentFileName("Referred documents")
                                  .build())
                    .documentDate(LocalDate.of(2023, 4, 2))
                    .documentUploadedDatetime(DOCUMENT_UPLOADED_DATE)
                    .build();

            private static final MediationDocumentsReferredInStatement EXPECTED_REFFERED_DOCS_TWO =
                MediationDocumentsReferredInStatement.builder()
                    .documentUploadedDatetime(DOCUMENT_UPLOADED_DATE)
                    .documentType("another type")
                    .build();

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
                assertThat(actual).containsExactly(EXPECTED_REFFERED_DOCS_ONE);

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
                assertThat(actual).containsExactly(EXPECTED_REFFERED_DOCS_ONE, EXPECTED_REFFERED_DOCS_TWO);
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
                assertThat(actual).containsExactly(EXPECTED_REFFERED_DOCS_ONE);

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
                assertThat(actual).containsExactly(EXPECTED_REFFERED_DOCS_ONE, EXPECTED_REFFERED_DOCS_TWO);
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
                assertThat(actual).containsExactly(EXPECTED_REFFERED_DOCS_ONE);
                assertThat(app2actual).hasSize(0);

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
                assertThat(actual).containsExactly(EXPECTED_REFFERED_DOCS_ONE, EXPECTED_REFFERED_DOCS_TWO);
                assertThat(app2actual).hasSize(0);
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
                assertThat(actual).containsExactly(EXPECTED_REFFERED_DOCS_ONE);

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
                assertThat(actual).containsExactly(EXPECTED_REFFERED_DOCS_ONE, EXPECTED_REFFERED_DOCS_TWO);
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
                assertThat(actual).containsExactly(EXPECTED_REFFERED_DOCS_ONE);

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
                assertThat(actual).containsExactly(EXPECTED_REFFERED_DOCS_ONE, EXPECTED_REFFERED_DOCS_TWO);
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
                assertThat(actual).containsExactly(EXPECTED_REFFERED_DOCS_ONE);
                assertThat(res2actual).hasSize(0);

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
                assertThat(actual).containsExactly(EXPECTED_REFFERED_DOCS_ONE, EXPECTED_REFFERED_DOCS_TWO);
                assertThat(res2actual).hasSize(0);
            }
        }

        @Test
        void shouldAssignCategoryIds_whenDocumentExist_ClaimantOne() {
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
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
        void shouldAssignCategoryIds_whenDocumentExist_ClaimantTwo() {
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
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
        void shouldAssignCategoryIds_whenDocumentExist_DefendantOne() {
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
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
        void shouldAssignCategoryIds_whenDocumentExist_DefendantTwo() {
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
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
                                                               .yourName("name").build());
        } else {
            mediationNonAttendanceStatement = null;
        }
        if (documentType.contains(REFERRED_DOCUMENTS)) {
            documentsReferred = wrapElements(MediationDocumentsReferredInStatement.builder()
                                                 .documentUploadedDatetime(DOCUMENT_UPLOADED_DATE)
                                                 .documentType("another type").build());
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
