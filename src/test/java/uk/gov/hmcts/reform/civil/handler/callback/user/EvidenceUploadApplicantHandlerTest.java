package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.math.BigDecimal;
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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.UserRoleCaching;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;
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
    private CaseDetailsConverter caseDetailsConverter;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    CaseData.CaseDataBuilder caseDataBuilder;

    @MockBean
    private UserRoleCaching userRoleCaching;
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    private final UploadEvidenceExpert uploadEvidenceDate = new UploadEvidenceExpert();
    private final UploadEvidenceWitness uploadEvidenceDate2 = new UploadEvidenceWitness();
    private final UploadEvidenceDocumentType uploadEvidenceDate3 = new UploadEvidenceDocumentType();
    private static final String NotificationWhenBothClaimant = "\n"
        + "Both claimants - Disclosure list\n"
        + "Both claimants - Documents for disclosure\n"
        + "Both claimants - Documents referred to in the statement\n"
        + "Both claimants - Expert's report\n"
        + "Both claimants - Joint Statement of Experts / Single Joint Expert Report\n"
        + "Both claimants - Questions for other party's expert or joint experts\n"
        + "Both claimants - Answer to questions asked\n"
        + "Both claimants - Case Summary\n"
        + "Both claimants - Skeleton argument\n"
        + "Both claimants - Authorities\n"
        + "Both claimants - Costs\n"
        + "Both claimants - Documentary evidence for trial";
    private static final String NotificationWhenClaimantTwo = "\n"
        + "Claimant 2 - Disclosure list\n"
        + "Claimant 2 - Documents for disclosure\n"
        + "Claimant 2 - Documents referred to in the statement\n"
        + "Claimant 2 - Expert's report\n"
        + "Claimant 2 - Joint Statement of Experts / Single Joint Expert Report\n"
        + "Claimant 2 - Questions for other party's expert or joint experts\n"
        + "Claimant 2 - Answer to questions asked\n"
        + "Claimant 2 - Case Summary\n"
        + "Claimant 2 - Skeleton argument\n"
        + "Claimant 2 - Authorities\n"
        + "Claimant 2 - Costs\n"
        + "Claimant 2 - Documentary evidence for trial";
    private static final String PAGE_ID = "validateValuesApplicant";

    @BeforeEach
    void setup() {
        given(time.now()).willReturn(LocalDateTime.now());
        given(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
            .willReturn(List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName()));
    }

    @Test
    void givenAboutToStart_assignCaseProgAllocatedTrackUnSpec() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .notificationText(null)
            .claimType(ClaimType.CLINICAL_NEGLIGENCE)
            .claimValue(ClaimValue.builder()
                            .statementOfValueInPennies(BigDecimal.valueOf(5000))
                            .build())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        assertThat(response.getData()).extracting("caseProgAllocatedTrack").isEqualTo("SMALL_CLAIM");
        assertThat(response.getData()).extracting("notificationText").isNull();
    }

    @Test
    void givenAboutToStart_assignCaseProgAllocatedTrackSpec() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .notificationText(null)
            .claimType(null)
            .totalClaimAmount(BigDecimal.valueOf(12500))
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        assertThat(response.getData()).extracting("caseProgAllocatedTrack").isEqualTo("FAST_CLAIM");
        assertThat(response.getData()).extracting("notificationText").isNull();
    }

    @Test
    void givenAboutToStart_2v1_shouldShowOptions() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .claimType(null)
                .totalClaimAmount(BigDecimal.valueOf(12500))
                .addApplicant2(YES)
                .applicant1(PartyBuilder.builder().individual().build())
                .applicant2(PartyBuilder.builder().individual().build())
                .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
        // Then
        assertThat(response.getData()).extracting("evidenceUploadOptions").isNotNull();
    }

    @ParameterizedTest
    @CsvSource({"0", "1", "2"})
    void givenCreateShow_2v1_ApplicantTwoFlag(String selected) {
        // Given
        List<String> options = List.of(EvidenceUploadHandlerBase.OPTION_APP1,
                EvidenceUploadHandlerBase.OPTION_APP2,
                EvidenceUploadHandlerBase.OPTION_APP_BOTH);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addApplicant2(YES)
                .applicant1(PartyBuilder.builder().individual().build())
                .applicant2(PartyBuilder.builder().individual().build())
                .evidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(Integer.parseInt(selected)), false))
                .build();
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
        // Then
        if (!selected.equals("1")) {
            assertThat(response.getData()).extracting("caseTypeFlag").isNotEqualTo("ApplicantTwoFields");
        } else {
            assertThat(response.getData()).extracting("caseTypeFlag").isEqualTo("ApplicantTwoFields");
        }
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
        var documentUpload = UploadEvidenceWitness.builder()
                .witnessOptionUploadDate(LocalDate.of(2023, 2, 10))
                .createdDatetime(LocalDateTime.of(2022, 05, 10, 12, 13, 12))
                .witnessOptionDocument(testDocument).build();
        List<Element<UploadEvidenceWitness>> documentList = new ArrayList<>();
        documentList.add(Element.<UploadEvidenceWitness>builder().value(documentUpload).build());
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .documentHearsayNotice(documentList)
            .build();

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
            .documentDisclosureList(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .caseBundles(prepareCaseBundles(LocalDateTime.of(2022, 05, 10, 12, 12, 12))).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then applicant docs uploaded after bundle should return size 2
        assertThat(updatedData.getApplicantDocsUploadedAfterBundle().size()).isEqualTo(3);
    }

    @Test
    void shouldNotAddApplicantEvidenceDocWhenBundleCreatedDateIsAfterEvidenceUploaded() {
        // Given caseBundles with bundle created date is after witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .documentQuestions(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .documentWitnessSummary(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12)))
            .caseBundles(prepareCaseBundles(LocalDateTime.of(2022, 05, 15, 12, 12, 12))).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

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

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When: handler is called
        // Then: an exception is thrown
        assertThrows(NullPointerException.class, () -> {
            handler.handle(params);
        });
    }

    @ParameterizedTest
    @CsvSource({"0", "2"})
    void should_do_naming_convention(String selected) {
        LocalDateTime createdDate = LocalDateTime.of(2022, 05, 10, 12, 13, 12);
        String witnessName = "AppWitness";
        LocalDate witnessDate = LocalDate.of(2023, 2, 10);
        List<String> options = List.of(EvidenceUploadHandlerBase.OPTION_APP1,
                EvidenceUploadHandlerBase.OPTION_APP2,
                EvidenceUploadHandlerBase.OPTION_APP_BOTH);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addApplicant2(YES)
                .applicant1(PartyBuilder.builder().individual().build())
                .applicant2(PartyBuilder.builder().individual().build())
                .evidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(Integer.parseInt(selected)), false))
                .documentWitnessSummary(
                        createWitnessDocs(witnessName, createdDate, witnessDate))
                .documentWitnessStatement(
                        createWitnessDocs(witnessName, createdDate, witnessDate))
                .documentHearsayNotice(createWitnessDocs(witnessName, createdDate, witnessDate))
                .documentExpertReport(createExpertDocs("expertName", witnessDate, "expertise", null, null, null, null))
                .documentJointStatement(createExpertDocs("expertsName", witnessDate, null, "expertises", null, null, null))
                .documentQuestions(createExpertDocs("expertName", witnessDate, null, null, "other", "question", null))
                .documentAnswers(createExpertDocs("expertName", witnessDate, null, null, "other", null, "answer"))
                .documentForDisclosure(createEvidenceDocs("typeDisclosure", witnessDate))
                .documentReferredInStatement(createEvidenceDocs("typeReferred", witnessDate))
                .documentEvidenceForTrial(createEvidenceDocs("typeForTrial", witnessDate))
                .documentDisclosureList(createEvidenceDocs(null, null))
                .documentCaseSummary(createEvidenceDocs(null, null))
                .documentSkeletonArgument(createEvidenceDocs(null, null))
                .documentAuthorities(createEvidenceDocs(null, null))
                .documentCosts(createEvidenceDocs(null, null))
                .build();
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addApplicant2(YES)
                .applicant1(PartyBuilder.builder().individual().build())
                .applicant2(PartyBuilder.builder().individual().build())
                .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(coreCaseDataService.getCase(anyLong())).willReturn(CaseDetails.builder().build());
        given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then applicant docs should have name changed
        assertThat(updatedData.getDocumentWitnessSummary().get(0).getValue()
                .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Summary of AppWitness.pdf");
        assertThat(updatedData.getDocumentWitnessStatement().get(0).getValue()
                .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Statement of AppWitness 10-02-2023.pdf");
        assertThat(updatedData.getDocumentHearsayNotice().get(0).getValue()
                .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Hearsay evidence AppWitness 10-02-2023.pdf");
        assertThat(updatedData.getDocumentExpertReport().get(0).getValue()
                .getExpertDocument().getDocumentFileName()).isEqualTo("Experts report expertName expertise 10-02-2023.pdf");
        assertThat(updatedData.getDocumentJointStatement().get(0).getValue()
                .getExpertDocument().getDocumentFileName()).isEqualTo("Joint report expertsName expertises 10-02-2023.pdf");
        assertThat(updatedData.getDocumentQuestions().get(0).getValue()
                .getExpertDocument().getDocumentFileName()).isEqualTo("expertName other question.pdf");
        assertThat(updatedData.getDocumentAnswers().get(0).getValue()
                .getExpertDocument().getDocumentFileName()).isEqualTo("expertName other answer.pdf");
        assertThat(updatedData.getDocumentForDisclosure().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo("Document for disclosure typeDisclosure 10-02-2023.pdf");
        assertThat(updatedData.getDocumentReferredInStatement().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo("Referred Document typeReferred 10-02-2023.pdf");
        assertThat(updatedData.getDocumentEvidenceForTrial().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo("Documentary Evidence typeForTrial 10-02-2023.pdf");
        assertThat(updatedData.getDocumentDisclosureList().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentCaseSummary().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentSkeletonArgument().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentAuthorities().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentCosts().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);

        String both = "2";
        if (selected.equals(both)) {
            assertThat(updatedData.getDocumentWitnessSummaryApp2().get(0).getValue()
                    .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Summary of AppWitness.pdf");
            assertThat(updatedData.getDocumentWitnessSummaryApp2().get(0).getValue()
                    .getWitnessOptionDocument().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_WITNESS_SUMMARY);
            assertThat(updatedData.getDocumentWitnessStatementApp2().get(0).getValue()
                    .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Statement of AppWitness 10-02-2023.pdf");
            assertThat(updatedData.getDocumentWitnessStatementApp2().get(0).getValue()
                    .getWitnessOptionDocument().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_WITNESS_STATEMENT);
            assertThat(updatedData.getDocumentHearsayNoticeApp2().get(0).getValue()
                    .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Hearsay evidence AppWitness 10-02-2023.pdf");
            assertThat(updatedData.getDocumentHearsayNoticeApp2().get(0).getValue()
                    .getWitnessOptionDocument().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_WITNESS_HEARSAY);
            assertThat(updatedData.getDocumentExpertReportApp2().get(0).getValue()
                    .getExpertDocument().getDocumentFileName()).isEqualTo("Experts report expertName expertise 10-02-2023.pdf");
            assertThat(updatedData.getDocumentExpertReportApp2().get(0).getValue()
                    .getExpertDocument().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_EXPERT_REPORT);
            assertThat(updatedData.getDocumentJointStatementApp2().get(0).getValue()
                    .getExpertDocument().getDocumentFileName()).isEqualTo("Joint report expertsName expertises 10-02-2023.pdf");
            assertThat(updatedData.getDocumentJointStatementApp2().get(0).getValue()
                    .getExpertDocument().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_EXPERT_JOINT_STATEMENT);
            assertThat(updatedData.getDocumentQuestionsApp2().get(0).getValue()
                    .getExpertDocument().getDocumentFileName()).isEqualTo("expertName other question.pdf");
            assertThat(updatedData.getDocumentQuestionsApp2().get(0).getValue()
                    .getExpertDocument().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_EXPERT_QUESTIONS);
            assertThat(updatedData.getDocumentAnswersApp2().get(0).getValue()
                    .getExpertDocument().getDocumentFileName()).isEqualTo("expertName other answer.pdf");
            assertThat(updatedData.getDocumentAnswersApp2().get(0).getValue()
                    .getExpertDocument().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_EXPERT_ANSWERS);
            assertThat(updatedData.getDocumentForDisclosureApp2().get(0).getValue()
                    .getDocumentUpload().getDocumentFileName()).isEqualTo("Document for disclosure typeDisclosure 10-02-2023.pdf");
            assertThat(updatedData.getDocumentForDisclosureApp2().get(0).getValue()
                    .getDocumentUpload().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_DISCLOSURE);
            assertThat(updatedData.getDocumentReferredInStatementApp2().get(0).getValue()
                    .getDocumentUpload().getDocumentFileName()).isEqualTo("Referred Document typeReferred 10-02-2023.pdf");
            assertThat(updatedData.getDocumentReferredInStatementApp2().get(0).getValue()
                    .getDocumentUpload().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_WITNESS_REFERRED);
            assertThat(updatedData.getDocumentEvidenceForTrialApp2().get(0).getValue()
                    .getDocumentUpload().getDocumentFileName()).isEqualTo("Documentary Evidence typeForTrial 10-02-2023.pdf");
            assertThat(updatedData.getDocumentEvidenceForTrialApp2().get(0).getValue()
                    .getDocumentUpload().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE);
            assertThat(updatedData.getDocumentDisclosureListApp2().get(0).getValue()
                    .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentDisclosureListApp2().get(0).getValue()
                    .getDocumentUpload().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_DISCLOSURE_LIST);
            assertThat(updatedData.getDocumentCaseSummaryApp2().get(0).getValue()
                    .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentCaseSummaryApp2().get(0).getValue()
                    .getDocumentUpload().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_PRE_TRIAL_SUMMARY);
            assertThat(updatedData.getDocumentSkeletonArgumentApp2().get(0).getValue()
                    .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentSkeletonArgumentApp2().get(0).getValue()
                    .getDocumentUpload().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_TRIAL_SKELETON);
            assertThat(updatedData.getDocumentAuthoritiesApp2().get(0).getValue()
                    .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentAuthoritiesApp2().get(0).getValue()
                    .getDocumentUpload().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_PRECEDENT_H);
            assertThat(updatedData.getDocumentCostsApp2().get(0).getValue()
                    .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentCostsApp2().get(0).getValue()
                    .getDocumentUpload().getCategoryID()).isEqualTo(EvidenceUploadHandlerBase.APPLICANT_TWO_ANY_PRECEDENT_H);
            assertThat(updatedData.getNotificationText()).isEqualTo(NotificationWhenBothClaimant);
        }
    }

    @Test
    void should_do_naming_convention_app2() {
        LocalDateTime createdDate = LocalDateTime.of(2022, 05, 10, 12, 13, 12);
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        String witnessName = "appTwoWitness";
        List<String> options = List.of(EvidenceUploadHandlerBase.OPTION_APP1,
                                       EvidenceUploadHandlerBase.OPTION_APP2,
                                       EvidenceUploadHandlerBase.OPTION_APP_BOTH);
        LocalDate witnessDate = LocalDate.of(2023, 2, 10);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addApplicant2(YES)
            .applicant1(PartyBuilder.builder().individual().build())
            .applicant2(PartyBuilder.builder().individual().build())
            .evidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(1), false))
            .documentWitnessSummaryApp2(createWitnessDocs(witnessName, createdDate, witnessDate))
            .documentWitnessStatementApp2(createWitnessDocs(witnessName, createdDate, witnessDate))
            .documentHearsayNoticeApp2(createWitnessDocs(witnessName, createdDate, witnessDate))
            .documentExpertReportApp2(createExpertDocs("expertName", witnessDate, "expertise", null, null, null, null))
            .documentJointStatementApp2(createExpertDocs("expertsName", witnessDate, null, "expertises", null, null, null))
            .documentQuestionsApp2(createExpertDocs("expertName", witnessDate, null, null, "other", "question", null))
            .documentAnswersApp2(createExpertDocs("expertName", witnessDate, null, null, "other", null, "answer"))
            .documentForDisclosureApp2(createEvidenceDocs("typeDisclosure", witnessDate))
            .documentReferredInStatementApp2(createEvidenceDocs("typeReferred", witnessDate))
            .documentEvidenceForTrialApp2(createEvidenceDocs("typeForTrial", witnessDate))
            .documentDisclosureListApp2(createEvidenceDocs(null, null))
            .documentCaseSummaryApp2(createEvidenceDocs(null, null))
            .documentSkeletonArgumentApp2(createEvidenceDocs(null, null))
            .documentAuthoritiesApp2(createEvidenceDocs(null, null))
            .documentCostsApp2(createEvidenceDocs(null, null))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then applicant docs should have name changed
        assertThat(updatedData.getDocumentWitnessSummaryApp2().get(0).getValue()
                       .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Summary of appTwoWitness.pdf");
        assertThat(updatedData.getDocumentWitnessStatementApp2().get(0).getValue()
                       .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Statement of appTwoWitness 10-02-2023.pdf");
        assertThat(updatedData.getDocumentHearsayNoticeApp2().get(0).getValue()
                       .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Hearsay evidence appTwoWitness 10-02-2023.pdf");
        assertThat(updatedData.getDocumentExpertReportApp2().get(0).getValue()
                       .getExpertDocument().getDocumentFileName()).isEqualTo("Experts report expertName expertise 10-02-2023.pdf");
        assertThat(updatedData.getDocumentJointStatementApp2().get(0).getValue()
                       .getExpertDocument().getDocumentFileName()).isEqualTo("Joint report expertsName expertises 10-02-2023.pdf");
        assertThat(updatedData.getDocumentQuestionsApp2().get(0).getValue()
                       .getExpertDocument().getDocumentFileName()).isEqualTo("expertName other question.pdf");
        assertThat(updatedData.getDocumentAnswersApp2().get(0).getValue()
                       .getExpertDocument().getDocumentFileName()).isEqualTo("expertName other answer.pdf");
        assertThat(updatedData.getDocumentForDisclosureApp2().get(0).getValue()
                       .getDocumentUpload().getDocumentFileName()).isEqualTo("Document for disclosure typeDisclosure 10-02-2023.pdf");
        assertThat(updatedData.getDocumentReferredInStatementApp2().get(0).getValue()
                       .getDocumentUpload().getDocumentFileName()).isEqualTo("Referred Document typeReferred 10-02-2023.pdf");
        assertThat(updatedData.getDocumentEvidenceForTrialApp2().get(0).getValue()
                       .getDocumentUpload().getDocumentFileName()).isEqualTo("Documentary Evidence typeForTrial 10-02-2023.pdf");
        assertThat(updatedData.getDocumentDisclosureListApp2().get(0).getValue()
                       .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentCaseSummaryApp2().get(0).getValue()
                       .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentSkeletonArgumentApp2().get(0).getValue()
                       .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentAuthoritiesApp2().get(0).getValue()
                       .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentCostsApp2().get(0).getValue()
                       .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getNotificationText()).isEqualTo(NotificationWhenClaimantTwo);
    }

    @Test
    void shouldNotAddSameNotificationIfAlreadyAdded_notificationText() {
        // If we populate notification string with an entry, we do not want to duplicate that on further uploads of same type.
        List<String> options = List.of(EvidenceUploadHandlerBase.OPTION_APP1,
                                       EvidenceUploadHandlerBase.OPTION_APP2,
                                       EvidenceUploadHandlerBase.OPTION_APP_BOTH);
        LocalDate witnessDate = LocalDate.of(2023, 2, 10);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .notificationText("Documentation that has been uploaded: \n\n Claimant 1 - Joint Statement of Experts / Single Joint Expert Report \n")
            .applicant1(PartyBuilder.builder().individual().build())
            .evidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(0), false))
            .documentJointStatement(createExpertDocs("expertsName", witnessDate, null, "expertises", null, null, null))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(coreCaseDataService.getCase(anyLong())).willReturn(CaseDetails.builder().build());
        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then Notificcation should not have a duplicate entry
        assertThat(updatedData.getNotificationText())
            .isEqualTo("Documentation that has been uploaded: \n\n Claimant 1 - Joint Statement of Experts / Single Joint Expert Report \n");
    }

    @Test
    void shouldNotPopulateNotificationWithOldDocument_whenNewDocumentUploadAdded() {
        // When evidence upload is retriggered we do not send a notification for old content i.e uploaded before midnight of current day
        List<String> options = List.of(EvidenceUploadHandlerBase.OPTION_APP1,
                                       EvidenceUploadHandlerBase.OPTION_APP2,
                                       EvidenceUploadHandlerBase.OPTION_APP_BOTH);
        LocalDate witnessDate = LocalDate.of(2023, 2, 10);
        String witnessName = "Witness";
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .notificationText(null)
            .evidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(0), false))
            .documentWitnessStatement(createWitnessDocs(witnessName, LocalDateTime.now().minusDays(2), witnessDate))
            .documentWitnessSummary(createWitnessDocs(witnessName, LocalDateTime.now(), witnessDate))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(coreCaseDataService.getCase(anyLong())).willReturn(CaseDetails.builder().build());
        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then Notification should not have old entry (witness statement)
        assertThat(updatedData.getNotificationText()).isEqualTo("\nClaimant 1 - Witness summary");
    }

    private List<Element<UploadEvidenceDocumentType>> createEvidenceDocs(String type, LocalDate issuedDate) {
        Document document = Document.builder().documentBinaryUrl(
                        TEST_URL)
                .documentFileName(TEST_FILE_NAME).build();
        List<Element<UploadEvidenceDocumentType>> evidenceDocs = new ArrayList<>();
        evidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                .builder()
                .typeOfDocument(type)
                .documentIssuedDate(issuedDate)
                .documentUpload(document)
                .build()));
        return evidenceDocs;
    }

    private List<Element<UploadEvidenceExpert>> createExpertDocs(String expertName,
                                                                 LocalDate dateUpload,
                                                                 String expertise,
                                                                 String expertises,
                                                                 String otherParty,
                                                                 String question,
                                                                 String answer) {
        Document document = Document.builder().documentBinaryUrl(
                        TEST_URL)
                .documentFileName(TEST_FILE_NAME).build();
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                .builder()
                .expertDocument(document)
                .expertOptionName(expertName)
                .expertOptionExpertise(expertise)
                .expertOptionExpertises(expertises)
                .expertOptionOtherParty(otherParty)
                .expertDocumentQuestion(question)
                .expertDocumentAnswer(answer)
                .expertOptionUploadDate(dateUpload).build()));
        return expertEvidenceDocs;
    }

    private List<Element<UploadEvidenceWitness>> createWitnessDocs(String witnessName,
                                                                   LocalDateTime createdDate,
                                                                   LocalDate dateMade) {
        Document document = Document.builder().documentBinaryUrl(
                        TEST_URL)
                .documentFileName(TEST_FILE_NAME).build();
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessEvidenceDocs.add(ElementUtils.element(UploadEvidenceWitness
                .builder()
                .witnessOptionDocument(document)
                .witnessOptionName(witnessName)
                .createdDatetime(createdDate)
                .witnessOptionUploadDate(dateMade).build()));
        return witnessEvidenceDocs;
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

    private List<Element<UploadEvidenceDocumentType>> getUploadEvidenceDocumentTypeDocs(LocalDateTime uploadedDate) {
        List<Element<UploadEvidenceDocumentType>> uploadEvidenceDocs = new ArrayList<>();
        uploadEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                        .builder()
                                                        .createdDatetime(uploadedDate)
                                                        .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                            .documentFileName(TEST_FILE_NAME).build()).build()));

        return  uploadEvidenceDocs;
    }

}

