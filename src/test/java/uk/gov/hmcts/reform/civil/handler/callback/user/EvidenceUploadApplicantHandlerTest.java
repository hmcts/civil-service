package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.config.ApplicantEvidenceHandlerTestConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.ApplicantDocumentUploadTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.ApplicantSetOptionsTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOADED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    EvidenceUploadApplicantHandler.class,
    JacksonAutoConfiguration.class,
    ApplicantSetOptionsTask.class,
    ApplicantDocumentUploadTask.class,
    ApplicantEvidenceHandlerTestConfiguration.class
})
class EvidenceUploadApplicantHandlerTest extends BaseCallbackHandlerTest {

    private static final String TEST_URL = "url";
    private static final String TEST_FILE_NAME = "testFileName.pdf";
    protected static final String UPLOAD_TIMESTAMP = "14 Apr 2024 00:00:00";

    @Autowired
    private EvidenceUploadApplicantHandler handler;

    private final LocalDateTime time = LocalDateTime.now();

    @MockBean
    private CoreCaseUserService coreCaseUserService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    CaseData.CaseDataBuilder caseDataBuilder;
    @MockBean
    private FeatureToggleService featureToggleService;
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
        + "Both claimants - Documentary evidence for trial\n"
        + "Both claimants - Bundle";
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
        + "Claimant 2 - Documentary evidence for trial\n"
        + "Claimant 2 - Bundle";
    private static final String PAGE_ID = "validateValuesApplicant";

    @BeforeEach
    void setup() {
        given(featureToggleService.isCaseProgressionEnabled()).willReturn(false);
    }

    @Test
    void givenAboutToStart_assignCaseProgAllocatedTrackUnSpec() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .notificationText(null)
            .allocatedTrack(SMALL_CLAIM)
            .responseClaimTrack(null)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), any())).willReturn(false);
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
            .caseAccessCategory(SPEC_CLAIM)
            .notificationText(null)
            .allocatedTrack(null)
            .responseClaimTrack("FAST_CLAIM")
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), any())).willReturn(false);
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
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time
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
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time
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
            + " date entered must not be in the future (5).",
        "documentIssuedDate,documentEvidenceForTrial, Invalid date: \"Documentary evidence for trial\""
            + " date entered must not be in the future (10).",
    })
    void shouldReturnError_whenDocumentTypeUploadDateFuture(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time
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

    @Test
    void shouldReturnError_whenBundleUploadDatePast() {
        var documentUpload = UploadEvidenceDocumentType.builder()
            .documentIssuedDate(LocalDate.of(2022, 2, 10))
            .bundleName("test")
            .documentUpload(Document.builder().build()).build();
        List<Element<UploadEvidenceDocumentType>> documentList = new ArrayList<>();
        documentList.add(Element.<UploadEvidenceDocumentType>builder().value(documentUpload).build());

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .bundleEvidence(documentList)
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getErrors()).contains("Invalid date: \"Bundle Hearing date\" date entered must not be in the past (11).");
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosureApp2, Invalid date: \"Documents for disclosure\""
            + " date entered must not be in the future (1).",
        "documentIssuedDate,documentReferredInStatementApp2, Invalid date: \"Documents referred to in the statement\""
            + " date entered must not be in the future (5).",
        "documentIssuedDate,documentEvidenceForTrialApp2, Invalid date: \"Documentary evidence for trial\""
            + " date entered must not be in the future (10)."
    })
    void shouldReturnError_whenDocumentTypeUploadDateFutureApplicant2(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time.now()
            .toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                                       .addApplicant2(YES)
                                       .applicant1(PartyBuilder.builder().individual().build())
                                       .applicant2(PartyBuilder.builder().individual().build())
                                       .caseTypeFlag("ApplicantTwoFields")
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
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time
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
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time
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
            + " date entered must not be in the future (6).",
        "expertOptionUploadDate,documentJointStatement,Invalid date: \"Joint statement of experts\" "
            + "date entered must not be in the future (7).",
        "expertOptionUploadDate,documentQuestions,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (8).",
        "expertOptionUploadDate,documentAnswers,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (9)."
    })
    void shouldReturnError_whenExpertOptionUploadDateFuture(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time
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
        "expertOptionUploadDate,documentExpertReportApp2,Invalid date: \"Expert's report\""
            + " date entered must not be in the future (6).",
        "expertOptionUploadDate,documentJointStatementApp2,Invalid date: \"Joint statement of experts\" "
            + "date entered must not be in the future (7).",
        "expertOptionUploadDate,documentQuestionsApp2,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (8).",
        "expertOptionUploadDate,documentAnswersApp2,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (9)."
    })
    void shouldReturnError_whenExpertOptionUploadDateFutureApplicant2(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.now()
            .toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                                       .addApplicant2(YES)
                                       .applicant1(PartyBuilder.builder().individual().build())
                                       .applicant2(PartyBuilder.builder().individual().build())
                                       .caseTypeFlag("ApplicantTwoFields"),
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
        "witnessOptionUploadDate,documentWitnessSummary,Invalid date: \"witness summary\" "
            + "date entered must not be in the future (3).",
        "witnessOptionUploadDate,documentHearsayNotice,Invalid date: \"Notice of the intention to rely on"
            + " hearsay evidence\" date entered must not be in the future (4)."
    })
    void shouldReturnError_whenWitnessOptionUploadDateInFuture(String dateField, String collectionField,
                                                               String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
            time.toLocalDate().plusWeeks(1)).build()));

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
            time.toLocalDate()).build()));

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
            time.toLocalDate().minusWeeks(1)).build()));

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
            "date entered must not be in the future (4)."
    })
    void shouldReturnError_whenOneDateIsInFutureForWitnessStatements(String dateField, String collectionField, String errorMessage) {
        //documentUploadWitness1 represents a collection so can have multiple dates entered at any time,
        // these dates should all be in the past, otherwise an error will be populated

        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField, time.toLocalDate().minusWeeks(1)).build()));
        date.add(1, element(invoke(uploadEvidenceDate2.toBuilder(), dateField, time.toLocalDate().plusWeeks(1)).build()));
        //dates above represent valid past dates, date below represents invalid future date.
        date.add(2, element(invoke(uploadEvidenceDate2.toBuilder(), dateField, time.toLocalDate().minusWeeks(1)).build()));

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
            + " date entered must not be in the future (6).",
        "expertOptionUploadDate,documentJointStatement,Invalid date: \"Joint statement of experts\" "
            + "date entered must not be in the future (7).",
        "expertOptionUploadDate,documentQuestions,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (8).",
        "expertOptionUploadDate,documentAnswers,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (9)."
    })
    void shouldReturnError_whenOneDateIsInFutureForExpertStatements(String dateField, String collectionField, String errorMessage) {
        //documentUploadWitness1 represents a collection so can have multiple dates entered at any time,
        // these dates should all be in the past, otherwise an error will be populated

        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.toLocalDate().minusWeeks(1)).build()));
        date.add(1, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.toLocalDate().plusWeeks(1)).build()));
        //dates above represent valid past dates, date below represents invalid future date.
        date.add(2, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.toLocalDate().minusWeeks(1)).build()));

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
        assertThat(updatedData.getCaseDocumentUploadDate()).isCloseTo(time, within(30, ChronoUnit.SECONDS));
    }

    @Test
    void shouldAssignCategoryID_whenDocumentExists() {
        Document testDocument = new Document("testurl",
            "testBinUrl", "A Fancy Name",
            "hash", null, UPLOAD_TIMESTAMP);
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
    void shouldStartEvidenceUploadedBusinessProcess_whenCPIsEnabled() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(YES)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        given(featureToggleService.isCaseProgressionEnabled()).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(EVIDENCE_UPLOADED.name());
        assertThat(updatedData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
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
    void shouldAddApplicantEvidenceDocWhenBundleCreatedDateIsBeforeEvidenceUploaded_onNewCreatedBundle() {
        List<Element<UploadEvidenceDocumentType>> applicantDocsUploadedAfterBundle = new ArrayList<>();
        UploadEvidenceDocumentType document = new UploadEvidenceDocumentType();
        document.setCreatedDatetime(LocalDateTime.now(ZoneId.of("Europe/London")));
        applicantDocsUploadedAfterBundle.add(ElementUtils.element(document));
        // Given caseBundles with bundle created date is before witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            // populate applicantDocsUploadedAfterBundle with a default built element after a new bundle, it only
            // contains createdDatetime and no document, so will be removed from final list
            .applicantDocsUploadedAfterBundle(applicantDocsUploadedAfterBundle)
            // added after trial bundle, so will  be added
            .documentWitnessStatement(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url22"))
            .documentExpertReport(getExpertDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url44"))
            .caseBundles(prepareCaseBundles(LocalDateTime.of(2022, 04, 10, 12, 12, 12))).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then applicant docs uploaded after bundle should return size 2, 2 new docs and 1 being removed.
        assertThat(updatedData.getApplicantDocsUploadedAfterBundle()).hasSize(2);
    }

    @Test
    void shouldAddUniqueApplicantEvidenceDocWhenBundleCreatedDateIsBeforeEvidenceUploaded() {
        // Given caseBundles with bundle created date is before witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            // populate applicantDocsUploadedAfterBundle with an existing upload
            .applicantDocsUploadedAfterBundle(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url77"))
            // added before trial bundle, so will not be added
            .documentAnswers(getExpertDocs(LocalDateTime.of(2022, 03, 10, 12, 13, 12), "url11"))
            .documentWitnessSummary(getWitnessDocs(LocalDateTime.of(2022, 03, 10, 12, 13, 12), "url22"))
            .documentQuestions(getExpertDocs(LocalDateTime.of(2022, 03, 10, 12, 13, 12), "url33"))
            // added after trial bundle, so will  be added
            .documentAuthorities(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url44"))
            .documentCosts(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url55"))
            .documentWitnessStatement(getWitnessDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url99"))
            .documentDisclosureList(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url88"))
            // this should not be added, as it has duplicate URL, indicating it already is in list, and should be skipped.
            .documentExpertReport(getExpertDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url77"))
            .caseBundles(prepareCaseBundles(LocalDateTime.of(2022, 04, 10, 12, 12, 12))).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then applicant docs uploaded after bundle should return size 5, 4 new docs and 1 existing.
        assertThat(updatedData.getApplicantDocsUploadedAfterBundle()).hasSize(5);
    }

    @Test
    void shouldAddAdditionalBundleDocuments_EvidenceDocWhenBundleCreatedDateIsBeforeEvidenceUploaded() {
        // Given caseBundles with bundle created date is before witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .applicantDocsUploadedAfterBundle(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"))
            .documentWitnessSummary(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url22"))
            .caseBundles(prepareCaseBundles(LocalDateTime.of(2022, 05, 10, 12, 12, 12))).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then applicant docs uploaded after bundle should return size 2
        assertThat(updatedData.getApplicantDocsUploadedAfterBundle()).hasSize(2);
    }

    @Test
    void shouldNotAddApplicantEvidenceDocWhenBundleCreatedDateIsAfterEvidenceUploaded() {
        // Given caseBundles with bundle created date is after witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .documentDisclosureList(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"))
            .documentDisclosureListApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url2"))
            .documentForDisclosure(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url3"))
            .documentForDisclosureApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url4"))
            .documentReferredInStatement(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url5"))
            .documentReferredInStatementApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url6"))
            .documentCaseSummary(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url7"))
            .documentCaseSummaryApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url8"))
            .documentSkeletonArgument(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url9"))
            .documentSkeletonArgumentApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url10"))
            .documentAuthorities(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url11"))
            .documentAuthoritiesApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url12"))
            .documentCosts(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url13"))
            .documentCostsApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url14"))
            .documentHearsayNotice(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url15"))
            .documentHearsayNoticeApp2(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url16"))
            .documentWitnessSummary(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url17"))
            .documentWitnessSummaryApp2(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url18"))
            .documentQuestions(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url19"))
            .documentQuestionsApp2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url20"))
            .documentEvidenceForTrial(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url21"))
            .documentEvidenceForTrialApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url22"))
            .documentAnswers(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url23"))
            .documentAnswersApp2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url24"))
            .documentJointStatement(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url25"))
            .documentJointStatementApp2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url26"))
            .documentExpertReport(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url27"))
            .documentExpertReportApp2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url28"))
            .documentWitnessStatement(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url29"))
            .documentWitnessStatement(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url30"))
            .caseBundles(prepareCaseBundles(LocalDateTime.of(2022, 05, 15, 12, 12, 12))).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);

        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then applicant docs uploaded after bundle should return size 0
        assertThat(updatedData.getApplicantDocsUploadedAfterBundle()).isNull();
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
            .documentQuestions(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url3"))
            .documentWitnessSummary(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url3"))
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
            .documentQuestions(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url3"))
            .documentWitnessSummary(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url3"))
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

    @ParameterizedTest
    @CsvSource({"0", "2"})
    void should_do_naming_convention(String selected) {
        LocalDateTime createdDate = LocalDateTime.of(2022, 05, 10, 12, 13, 12);
        String witnessName = "AppWitness";
        LocalDate witnessDate = LocalDate.of(2023, 2, 10);
        LocalDate bundleDate = LocalDate.of(2023, 2, 10);
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
                .documentForDisclosure(createEvidenceDocs(null, null, "typeDisclosure", witnessDate))
                .documentReferredInStatement(createEvidenceDocs("witness", null, "typeReferred", witnessDate))
                .documentEvidenceForTrial(createEvidenceDocs(null, null, "typeForTrial", witnessDate))
                .documentDisclosureList(createEvidenceDocs(null, null, null, null))
                .documentCaseSummary(createEvidenceDocs(null, null, null, null))
                .documentSkeletonArgument(createEvidenceDocs(null, null, null, null))
                .documentAuthorities(createEvidenceDocs(null, null, null, null))
                .documentCosts(createEvidenceDocs(null, null, null, null))
                .bundleEvidence(createEvidenceDocs(null, "A bundle", null, bundleDate))
                .build();
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addApplicant2(YES)
            .applicant1(PartyBuilder.builder().individual().build())
            .applicant2(PartyBuilder.builder().individual().build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        given(coreCaseDataService.getCase(anyLong())).willReturn(CaseDetails.builder().build());
        // given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then applicant docs should have name changed
        assertThat(updatedData.getDocumentWitnessSummary().get(0).getValue()
            .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Summary of AppWitness 10-02-2023.pdf");
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
            .getDocumentUpload().getDocumentFileName()).isEqualTo("typeReferred referred to in the statement of witness 10-02-2023.pdf");
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
                .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Summary of AppWitness 10-02-2023.pdf");
            assertThat(updatedData.getDocumentWitnessSummaryApp2().get(0).getValue()
                .getWitnessOptionDocument().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_WITNESS_SUMMARY.getCategoryId());
            assertThat(updatedData.getDocumentWitnessStatementApp2().get(0).getValue()
                .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Statement of AppWitness 10-02-2023.pdf");
            assertThat(updatedData.getDocumentWitnessStatementApp2().get(0).getValue()
                .getWitnessOptionDocument().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_WITNESS_STATEMENT.getCategoryId());
            assertThat(updatedData.getDocumentHearsayNoticeApp2().get(0).getValue()
                .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Hearsay evidence AppWitness 10-02-2023.pdf");
            assertThat(updatedData.getDocumentHearsayNoticeApp2().get(0).getValue()
                .getWitnessOptionDocument().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_WITNESS_HEARSAY.getCategoryId());
            assertThat(updatedData.getDocumentExpertReportApp2().get(0).getValue()
                .getExpertDocument().getDocumentFileName()).isEqualTo("Experts report expertName expertise 10-02-2023.pdf");
            assertThat(updatedData.getDocumentExpertReportApp2().get(0).getValue()
                .getExpertDocument().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_EXPERT_REPORT.getCategoryId());
            assertThat(updatedData.getDocumentJointStatementApp2().get(0).getValue()
                .getExpertDocument().getDocumentFileName()).isEqualTo("Joint report expertsName expertises 10-02-2023.pdf");
            assertThat(updatedData.getDocumentJointStatementApp2().get(0).getValue()
                .getExpertDocument().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_EXPERT_JOINT_STATEMENT.getCategoryId());
            assertThat(updatedData.getDocumentQuestionsApp2().get(0).getValue()
                .getExpertDocument().getDocumentFileName()).isEqualTo("expertName other question.pdf");
            assertThat(updatedData.getDocumentQuestionsApp2().get(0).getValue()
                .getExpertDocument().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_EXPERT_QUESTIONS.getCategoryId());
            assertThat(updatedData.getDocumentAnswersApp2().get(0).getValue()
                .getExpertDocument().getDocumentFileName()).isEqualTo("expertName other answer.pdf");
            assertThat(updatedData.getDocumentAnswersApp2().get(0).getValue()
                .getExpertDocument().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_EXPERT_ANSWERS.getCategoryId());
            assertThat(updatedData.getDocumentForDisclosureApp2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo("Document for disclosure typeDisclosure 10-02-2023.pdf");
            assertThat(updatedData.getDocumentForDisclosureApp2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_DISCLOSURE.getCategoryId());
            assertThat(updatedData.getDocumentReferredInStatementApp2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo("typeReferred referred to in the statement of witness 10-02-2023.pdf");
            assertThat(updatedData.getDocumentReferredInStatementApp2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_WITNESS_REFERRED.getCategoryId());
            assertThat(updatedData.getDocumentEvidenceForTrialApp2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo("Documentary Evidence typeForTrial 10-02-2023.pdf");
            assertThat(updatedData.getDocumentEvidenceForTrialApp2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE.getCategoryId());
            assertThat(updatedData.getDocumentDisclosureListApp2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentDisclosureListApp2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_DISCLOSURE_LIST.getCategoryId());
            assertThat(updatedData.getDocumentCaseSummaryApp2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentCaseSummaryApp2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_PRE_TRIAL_SUMMARY.getCategoryId());
            assertThat(updatedData.getDocumentSkeletonArgumentApp2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentSkeletonArgumentApp2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_TRIAL_SKELETON.getCategoryId());
            assertThat(updatedData.getDocumentAuthoritiesApp2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentAuthoritiesApp2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_TRIAL_SKELETON.getCategoryId());
            assertThat(updatedData.getDocumentCostsApp2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentCostsApp2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.APPLICANT_TWO_SCHEDULE_OF_COSTS.getCategoryId());
            assertThat(updatedData.getNotificationText()).isEqualTo(NotificationWhenBothClaimant);
        }
    }

    @Test
    void should_do_naming_convention_app2() {
        LocalDateTime createdDate = LocalDateTime.of(2022, 05, 10, 12, 13, 12);
        String witnessName = "appTwoWitness";
        List<String> options = List.of(EvidenceUploadHandlerBase.OPTION_APP1,
            EvidenceUploadHandlerBase.OPTION_APP2,
            EvidenceUploadHandlerBase.OPTION_APP_BOTH);
        LocalDate witnessDate = LocalDate.of(2023, 2, 10);
        LocalDate bundleDate = LocalDate.of(2023, 2, 10);
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
            .documentForDisclosureApp2(createEvidenceDocs(null, null, "typeDisclosure", witnessDate))
            .documentReferredInStatementApp2(createEvidenceDocs("witness", null, "typeReferred", witnessDate))
            .documentEvidenceForTrialApp2(createEvidenceDocs(null, null, "typeForTrial", witnessDate))
            .documentDisclosureListApp2(createEvidenceDocs(null, null, null, null))
            .documentCaseSummaryApp2(createEvidenceDocs(null, null, null, null))
            .documentSkeletonArgumentApp2(createEvidenceDocs(null, null, null, null))
            .documentAuthoritiesApp2(createEvidenceDocs(null, null, null, null))
            .documentCostsApp2(createEvidenceDocs(null, null, null, null))
            .bundleEvidence(createEvidenceDocs(null, "A bundle", null, bundleDate))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());

        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);

        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then applicant docs should have name changed
        assertThat(updatedData.getDocumentWitnessSummaryApp2().get(0).getValue()
                       .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Summary of appTwoWitness 10-02-2023.pdf");
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
                       .getDocumentUpload().getDocumentFileName()).isEqualTo("typeReferred referred to in the statement of witness 10-02-2023.pdf");
        assertThat(updatedData.getDocumentEvidenceForTrialApp2().get(0).getValue()
                       .getDocumentUpload().getDocumentFileName()).isEqualTo("Documentary Evidence typeForTrial 10-02-2023.pdf");
        assertThat(updatedData.getBundleEvidence().get(0).getValue()
                       .getDocumentUpload().getDocumentFileName()).isEqualTo("10-02-2023-A bundle.pdf");
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
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
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
            .documentWitnessStatement(createWitnessDocs(witnessName, time.minusDays(2), witnessDate))
            .documentWitnessSummary(createWitnessDocs(witnessName, time, witnessDate))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        given(coreCaseDataService.getCase(anyLong())).willReturn(CaseDetails.builder().build());
        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then Notification should not have old entry (witness statement)
        assertThat(updatedData.getNotificationText()).isEqualTo("\nClaimant 1 - Witness summary");
    }

    private List<Element<UploadEvidenceDocumentType>> createEvidenceDocs(String name, String bundleName, String type, LocalDate issuedDate) {
        Document document = Document.builder().documentBinaryUrl(
                        TEST_URL)
                .documentFileName(TEST_FILE_NAME).build();
        List<Element<UploadEvidenceDocumentType>> evidenceDocs = new ArrayList<>();
        evidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                .builder()
                .witnessOptionName(name)
                .bundleName(bundleName)
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

    private List<Element<UploadEvidenceWitness>> getWitnessDocs(LocalDateTime uploadedDate, String uniqueUrl) {
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessEvidenceDocs.add(ElementUtils.element(UploadEvidenceWitness
            .builder()
            .witnessOptionDocument(Document.builder().documentBinaryUrl(
                    TEST_URL)
                .documentUrl(uniqueUrl)
                .documentFileName(TEST_FILE_NAME).build())
            .witnessOptionName("FirstName LastName")
            .createdDatetime(uploadedDate)
            .witnessOptionUploadDate(LocalDate.of(2023, 2, 10)).build()));
        return witnessEvidenceDocs;
    }

    private List<Element<UploadEvidenceExpert>> getExpertDocs(LocalDateTime uploadedDate, String uniqueUrl) {
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
            .builder()
            .createdDatetime(uploadedDate)
            .expertOptionUploadDate(LocalDate.now())
            .expertDocument(Document.builder().documentBinaryUrl(TEST_URL)
                .documentUrl(uniqueUrl)
                .documentFileName(TEST_FILE_NAME).build()).build()));

        return expertEvidenceDocs;
    }

    private List<Element<UploadEvidenceDocumentType>> getUploadEvidenceDocumentTypeDocs(LocalDateTime uploadedDate, String uniqueUrl) {
        List<Element<UploadEvidenceDocumentType>> uploadEvidenceDocs = new ArrayList<>();
        uploadEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
            .builder()
            .createdDatetime(uploadedDate)
            .documentIssuedDate(LocalDate.now())
            .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                .documentUrl(uniqueUrl)
                .documentFileName(TEST_FILE_NAME).build()).build()));

        return uploadEvidenceDocs;
    }

}

