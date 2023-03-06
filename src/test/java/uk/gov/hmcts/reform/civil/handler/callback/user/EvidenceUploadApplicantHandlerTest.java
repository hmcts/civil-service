package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.commons.util.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    EvidenceUploadApplicantHandler.class,
    JacksonAutoConfiguration.class
})
class EvidenceUploadApplicantHandlerTest extends BaseCallbackHandlerTest {

    private static final String TEST_URL = "url";
    private static final String TEST_FILE_NAME = "testFileName.pdf";

    @Autowired
    private EvidenceUploadApplicantHandler handler;

    @MockBean
    private Time time;

    @MockBean
    private  CoreCaseUserService coreCaseUserService;
    @MockBean
    CaseData.CaseDataBuilder caseDataBuilder;
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    private final UploadEvidenceExpert uploadEvidenceDate = new UploadEvidenceExpert();
    private final UploadEvidenceWitness uploadEvidenceDate2 = new UploadEvidenceWitness();
    private final UploadEvidenceDocumentType uploadEvidenceDate3 = new UploadEvidenceDocumentType();

    private static final String PAGE_ID = "validateValuesApplicant";

    @BeforeEach
    void setup() {
        given(time.now()).willReturn(LocalDateTime.now());
    }

    @Test
    void givenAboutToStart_1v2SameSolicitorWillNotChangeToRespondentTwoFlag() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(YES)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        assertThat(response.getData()).extracting("caseTypeFlag").isNotEqualTo("RespondentTwoFields");
    }

    @Test
    void givenAboutToStart_1v1WillNotChangeToRespondentTwoFlag() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        assertThat(response.getData()).extracting("caseTypeFlag").isNotEqualTo("RespondentTwoFields");
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosure",
        "documentIssuedDate,documentReferredInStatement",
        "documentIssuedDate,documentEvidenceForTrial",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePast(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time.now()
            .toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosure",
        "documentIssuedDate,documentReferredInStatement",
        "documentIssuedDate,documentEvidenceForTrial",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePresent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time.now()
            .toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosure, Invalid date: \"Documents for disclosure\""
            + " date entered must not be in the future (1).",
        "documentIssuedDate,documentReferredInStatement, Invalid date: \"Documents referred to in the statement\""
            + " date entered must not be in the future (4).",
        "documentIssuedDate,documentEvidenceForTrial, Invalid date: \"Documentary evidence for trial\""
            + " date entered must not be in the future (9).",
    })
    void shouldReturnError_whenDocumentTypeUploadDateFuture(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time.now()
            .toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReport",
        "expertOptionUploadDate,documentJointStatement",
        "expertOptionUploadDate,documentQuestions",
        "expertOptionUploadDate,documentAnswers"
    })
    void shouldNotReturnError_whenExpertOptionUploadDatePast(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now()
            .toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReport",
        "expertOptionUploadDate,documentJointStatement",
        "expertOptionUploadDate,documentQuestions",
        "expertOptionUploadDate,documentAnswers"
    })
    void shouldNotReturnError_whenExpertOptionUploadDatePresent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now()
            .toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
                                       .build().toBuilder(), collectionField, date)
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReport,Invalid date: \"Expert's report\""
        + " date entered must not be in the future (5).",
        "expertOptionUploadDate,documentJointStatement,Invalid date: \"Joint statement of experts\" "
            + "date entered must not be in the future (6).",
        "expertOptionUploadDate,documentQuestions,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (7).",
        "expertOptionUploadDate,documentAnswers,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (8)."
    })
    void shouldReturnError_whenExpertOptionUploadDateFuture(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now()
            .toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
                                   collectionField, date)
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatement,Invalid date: \"witness statement\" "
            + "date entered must not be in the future (2).",
        "witnessOptionUploadDate,documentHearsayNotice,Invalid date: \"Notice of the intention to rely on"
            + " hearsay evidence\" date entered must not be in the future (3)."
    })
    void shouldReturnError_whenWitnessOptionUploadDateInFuture(String dateField, String collectionField,
                                                               String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
                                   time.now().toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
                                   collectionField, date)
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatement",
        "witnessOptionUploadDate,documentHearsayNotice"
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePresent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
                                   time.now().toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
                                   collectionField, date)
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatement",
        "witnessOptionUploadDate,documentHearsayNotice"
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePast(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
                                   time.now().toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
                                   collectionField, date)
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatement,Invalid date: \"witness statement\" date entered must not be in the future (2).",
        "witnessOptionUploadDate,documentHearsayNotice,Invalid date: \"Notice of the intention to rely on hearsay evidence\" " +
            "date entered must not be in the future (3)."
    })
    void shouldReturnError_whenOneDateIsInFutureForWitnessStatements(String dateField, String collectionField, String errorMessage) {
        //documentUploadWitness1 represents a collection so can have multiple dates entered at any time,
        // these dates should all be in the past, otherwise an error will be populated

        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField, time.now().toLocalDate().minusWeeks(1)).build()));
        date.add(1, element(invoke(uploadEvidenceDate2.toBuilder(), dateField, time.now().toLocalDate().plusWeeks(1)).build()));
        //dates above represent valid past dates, date below represents invalid future date.
        date.add(2, element(invoke(uploadEvidenceDate2.toBuilder(), dateField, time.now().toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(), collectionField, date).build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(errorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReport,Invalid date: \"Expert's report\""
            + " date entered must not be in the future (5).",
        "expertOptionUploadDate,documentJointStatement,Invalid date: \"Joint statement of experts\" "
            + "date entered must not be in the future (6).",
        "expertOptionUploadDate,documentQuestions,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (7).",
        "expertOptionUploadDate,documentAnswers,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (8)."
    })
    void shouldReturnError_whenOneDateIsInFutureForExpertStatements(String dateField, String collectionField, String errorMessage) {
        //documentUploadWitness1 represents a collection so can have multiple dates entered at any time,
        // these dates should all be in the past, otherwise an error will be populated

        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now().toLocalDate().minusWeeks(1)).build()));
        date.add(1, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now().toLocalDate().plusWeeks(1)).build()));
        //dates above represent valid past dates, date below represents invalid future date.
        date.add(2, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now().toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(), collectionField, date).build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(errorMessage);
    }

    @Test
    void shouldCallExternalTask_whenAboutToSubmit() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then
        assertThat(updatedData.getCaseDocumentUploadDate()).isEqualTo(time.now());
    }

    @Test
    void shouldAssignCategoryID_whenDocumentExists() {
        Document testDocument = new Document("testurl",
                                             "testBinUrl", "A Fancy Name",
                                             "hash", null);
        var documentUpload = UploadEvidenceWitness.builder().witnessOptionDocument(testDocument).build();
        List<Element<UploadEvidenceWitness>> documentList = new ArrayList<>();
        documentList.add(Element.<UploadEvidenceWitness>builder().value(documentUpload).build());
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .documentHearsayNotice(documentList)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getDocumentHearsayNotice().get(0).getValue().getWitnessOptionDocument()
                       .getCategoryID()).isEqualTo("ApplicantWitnessHearsay");
    }

    @Test
    void shouldNotAssignCategoryID_whenDocumentNotExists() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(YES)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getDocumentHearsayNotice()).isNull();
    }

    @Test
    void givenSubmittedThenReturnsSubmittedCallbackResponse() {
        // Given
        String header = "# Documents uploaded";
        String body = "You can continue uploading documents or return later. To upload more documents, "
            + "go to Next step and select \"Document Upload\".";
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .build();
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

        // When
        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

        // Then
        assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                      .confirmationHeader(header)
                                                                      .confirmationBody(body)
                                                                      .build());
    }

    @Test
    void whenRegisterCalledThenReturnEvidenceUploadCaseEvent() {
        // Given
        Map<String, CallbackHandler> registerTarget = new HashMap<>();

        // When
        handler.register(registerTarget);

        // Then
        assertThat(registerTarget).containsExactly(entry(EVIDENCE_UPLOAD_APPLICANT.name(), handler));
    }

    private <T, A> T invoke(T target, String method, A argument) {
        ReflectionUtils.invokeMethod(ReflectionUtils.getRequiredMethod(target.getClass(),
                                                                       method, argument.getClass()), target, argument);
        return target;
    }

    @Test
    void shouldAddApplicantEvidenceDocWhenBundleCreatedDateIsBeforeEvidenceUploaded() {
        // Given caseBundles with bundle created date is before witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .documentQuestions(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .documentWitnessSummary(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .caseBundles(prepareCaseBundles(LocalDateTime.of(2022, 05, 10, 12, 12, 12))).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);

        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then applicant docs uploaded after bundle should return size 2
        assertThat(updatedData.getApplicantDocsUploadedAfterBundle().size()).isEqualTo(2);
    }

    @Test
    void shouldNotAddApplicantEvidenceDocWhenBundleCreatedDateIsAfterEvidenceUploaded() {
        // Given caseBundles with bundle created date is after witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .documentQuestions(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .documentWitnessSummary(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .caseBundles(prepareCaseBundles(LocalDateTime.of(2022, 05, 15, 12, 12, 12))).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);

        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then applicant docs uploaded after bundle should return size 0
        assertThat(updatedData.getApplicantDocsUploadedAfterBundle().size()).isEqualTo(0);
    }

    @Test
    void shouldBreakWhenThereIsAnyCaseBundlesWithoutCreatedDate() {
        // Given: No caseBundles exists with CreatedDate and new evidence is uploaded
        List<IdValue<Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
            .createdOn(Optional.of(LocalDateTime.of(2022, 05, 15, 12, 12, 12)))
            .build()));
        caseBundles.add(new IdValue<>("1", Bundle.builder().id("1")
            .title("Trial Bundle")
            .build()));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .documentQuestions(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .documentWitnessSummary(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .caseBundles(caseBundles)
            .build();

        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When: handler is called
        // Then: an exception is thrown
        assertThrows(NullPointerException.class, () -> {
            handler.handle(params);
        });
    }

    @Test
    void shouldBreakWhenThereIsAnyCaseBundlesWithNullCreatedDate() {
        // Given: No caseBundles exists with CreatedDate and new evidence is uploaded
        List<IdValue<Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
            .createdOn(Optional.ofNullable(null))
            .build()));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .documentQuestions(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .documentWitnessSummary(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .caseBundles(caseBundles)
            .build();

        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When: handler is called
        // Then: an exception is thrown
        assertThrows(NullPointerException.class, () -> {
            handler.handle(params);
        });
    }
    private List<IdValue<Bundle>> prepareCaseBundles(LocalDateTime bundleCreatedDate) {
        List<IdValue<Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
            .createdOn(Optional.of(bundleCreatedDate))
            .build()));
        return caseBundles;
    }

    private List<Element<UploadEvidenceWitness>> getWitnessDocs(LocalDateTime uploadedDate) {
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessEvidenceDocs.add(ElementUtils.element(UploadEvidenceWitness
                                                         .builder()
                                                         .witnessOptionDocument(Document.builder().documentBinaryUrl(
                                                                 TEST_URL)
                                                                                    .documentFileName(TEST_FILE_NAME).build())
                                                         .witnessOptionName("FirstName LastName")
                                                         .createdDatetime(uploadedDate)
                                                         .witnessOptionUploadDate(LocalDate.of(2023, 2, 10)).build()));
        return witnessEvidenceDocs;
    }

    private List<Element<UploadEvidenceExpert>> getExpertDocs(LocalDateTime uploadedDate) {
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                                                        .builder()
                                                        .createdDatetime(uploadedDate)
                                                        .expertDocument(Document.builder().documentBinaryUrl(TEST_URL)
                                                                            .documentFileName(TEST_FILE_NAME).build()).build()));

        return  expertEvidenceDocs;
    }

}

