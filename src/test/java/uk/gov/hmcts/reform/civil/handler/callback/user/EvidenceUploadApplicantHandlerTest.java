package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.commons.util.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @MockitoBean
    private CoreCaseUserService coreCaseUserService;
    @MockitoBean
    private CaseDetailsConverter caseDetailsConverter;
    @MockitoBean
    private CoreCaseDataService coreCaseDataService;
    @MockitoBean
    CaseData.CaseDataBuilder caseDataBuilder;
    @MockitoBean
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

    @Test
    void givenAboutToStart_assignCaseProgAllocatedTrackUnSpec() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setNotificationText(null);
        caseData.setAllocatedTrack(SMALL_CLAIM);
        caseData.setResponseClaimTrack(null);
        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        caseData.setNotificationText(null);
        caseData.setAllocatedTrack(null);
        caseData.setResponseClaimTrack("FAST_CLAIM");
        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setClaimType(null);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(12500));
        caseData.setAddApplicant2(YES);
        caseData.setApplicant1(PartyBuilder.builder().individual().build());
        caseData.setApplicant2(PartyBuilder.builder().individual().build());
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setAddApplicant2(YES);
        caseData.setApplicant1(PartyBuilder.builder().individual().build());
        caseData.setApplicant2(PartyBuilder.builder().individual().build());
        caseData.setEvidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(Integer.parseInt(selected)), false));
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
        "setDocumentIssuedDate,setDocumentForDisclosure",
        "setDocumentIssuedDate,setDocumentReferredInStatement",
        "setDocumentIssuedDate,setDocumentEvidenceForTrial",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePast(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceDocumentType(), dateField, time
            .toLocalDate().minusWeeks(1))));

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setDocumentIssuedDate,setDocumentForDisclosure",
        "setDocumentIssuedDate,setDocumentReferredInStatement",
        "setDocumentIssuedDate,setDocumentEvidenceForTrial",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePresent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceDocumentType(), dateField, time
            .toLocalDate())));

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setDocumentIssuedDate,setDocumentForDisclosure, Invalid date: \"Documents for disclosure\""
            + " date entered must not be in the future (1).",
        "setDocumentIssuedDate,setDocumentReferredInStatement, Invalid date: \"Documents referred to in the statement\""
            + " date entered must not be in the future (5).",
        "setDocumentIssuedDate,setDocumentEvidenceForTrial, Invalid date: \"Documentary evidence for trial\""
            + " date entered must not be in the future (10).",
    })
    void shouldReturnError_whenDocumentTypeUploadDateFuture(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceDocumentType(), dateField, time
            .toLocalDate().plusWeeks(1))));

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @Test
    void shouldReturnError_whenBundleUploadDatePast() {
        Document emptyDocument = Document.builder().build();
        var documentUpload = createUploadEvidenceDocumentType(
            LocalDate.of(2022, 2, 10),
            "test",
            emptyDocument);
        List<Element<UploadEvidenceDocumentType>> documentList = new ArrayList<>();
        documentList.add(createElement(documentUpload));

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setBundleEvidence(documentList);

        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getErrors()).contains("Invalid date: \"Bundle Hearing date\" date entered must not be in the past (11).");
    }

    @Test
    void shouldReturnError_whenBundleUploadDatePastAndNewBundleUploadedHasPastDate() {
        Document emptyDocument = Document.builder().build();
        var documentUpload = createUploadEvidenceDocumentType(
            LocalDate.of(2022, 2, 10),
            "test",
            emptyDocument);
        List<Element<UploadEvidenceDocumentType>> documentList = new ArrayList<>();
        documentList.add(createElement(documentUpload));

        Document emptyDocument2 = Document.builder().build();
        var documentUpload2 = createUploadEvidenceDocumentType(
            LocalDate.of(2022, 2, 10),
            "test",
            emptyDocument2);
        List<Element<UploadEvidenceDocumentType>> documentList2 = new ArrayList<>();
        documentList2.add(createElement(documentUpload));
        documentList2.add(createElement(documentUpload2));

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseDataBefore.setBundleEvidence(documentList);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setBundleEvidence(documentList2);

        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getErrors()).contains("Invalid date: \"Bundle Hearing date\" date entered must not be in the past (11).");
    }

    @Test
    void shouldNotReturnError_whenBundleUploadDatePastAndNewBundleUploadedHasNotPastDate() {
        Document emptyDocument = Document.builder().build();
        var documentUpload = createUploadEvidenceDocumentType(
            LocalDate.of(2022, 2, 10),
            "test",
            emptyDocument);
        List<Element<UploadEvidenceDocumentType>> documentList = new ArrayList<>();
        documentList.add(createElement(documentUpload));

        Document emptyDocument2 = Document.builder().build();
        var documentUpload2 = createUploadEvidenceDocumentType(
            LocalDate.now().plusMonths(2L),
            "test",
            emptyDocument2);
        List<Element<UploadEvidenceDocumentType>> documentList2 = new ArrayList<>();
        documentList2.add(createElement(documentUpload));
        documentList2.add(createElement(documentUpload2));

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseDataBefore.setBundleEvidence(documentList);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setBundleEvidence(documentList2);

        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setDocumentIssuedDate,setDocumentForDisclosureApp2, Invalid date: \"Documents for disclosure\""
            + " date entered must not be in the future (1).",
        "setDocumentIssuedDate,setDocumentReferredInStatementApp2, Invalid date: \"Documents referred to in the statement\""
            + " date entered must not be in the future (5).",
        "setDocumentIssuedDate,setDocumentEvidenceForTrialApp2, Invalid date: \"Documentary evidence for trial\""
            + " date entered must not be in the future (10)."
    })
    void shouldReturnError_whenDocumentTypeUploadDateFutureApplicant2(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceDocumentType(), dateField, time.now()
            .toLocalDate().plusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddApplicant2(YES);
        caseData.setApplicant1(PartyBuilder.builder().individual().build());
        caseData.setApplicant2(PartyBuilder.builder().individual().build());
        caseData.setCaseTypeFlag("ApplicantTwoFields");
        invoke(caseData, collectionField, date);
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseDataBefore.setAddApplicant2(YES);
        caseDataBefore.setApplicant1(PartyBuilder.builder().individual().build());
        caseDataBefore.setApplicant2(PartyBuilder.builder().individual().build());
        caseDataBefore.setCaseTypeFlag("ApplicantTwoFields");
        invoke(caseDataBefore, collectionField, List.of());
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "setExpertOptionUploadDate,setDocumentExpertReport",
        "setExpertOptionUploadDate,setDocumentJointStatement",
        "setExpertOptionUploadDate,setDocumentQuestions",
        "setExpertOptionUploadDate,setDocumentAnswers"
    })
    void shouldNotReturnError_whenExpertOptionUploadDatePast(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceExpert(), dateField, time
            .toLocalDate().minusWeeks(1))));

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setExpertOptionUploadDate,setDocumentExpertReport",
        "setExpertOptionUploadDate,setDocumentJointStatement",
        "setExpertOptionUploadDate,setDocumentQuestions",
        "setExpertOptionUploadDate,setDocumentAnswers"
    })
    void shouldNotReturnError_whenExpertOptionUploadDatePresent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceExpert(), dateField, time
            .toLocalDate())));

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setExpertOptionUploadDate,setDocumentExpertReport,Invalid date: \"Expert's report\""
            + " date entered must not be in the future (6).",
        "setExpertOptionUploadDate,setDocumentJointStatement,Invalid date: \"Joint statement of experts\" "
            + "date entered must not be in the future (7).",
        "setExpertOptionUploadDate,setDocumentQuestions,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (8).",
        "setExpertOptionUploadDate,setDocumentAnswers,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (9)."
    })
    void shouldReturnError_whenExpertOptionUploadDateFuture(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceExpert(), dateField, time
            .toLocalDate().plusWeeks(1))));

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "setExpertOptionUploadDate,setDocumentExpertReportApp2,Invalid date: \"Expert's report\""
            + " date entered must not be in the future (6).",
        "setExpertOptionUploadDate,setDocumentJointStatementApp2,Invalid date: \"Joint statement of experts\" "
            + "date entered must not be in the future (7).",
        "setExpertOptionUploadDate,setDocumentQuestionsApp2,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (8).",
        "setExpertOptionUploadDate,setDocumentAnswersApp2,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (9)."
    })
    void shouldReturnError_whenExpertOptionUploadDateFutureApplicant2(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceExpert(), dateField, time.now()
            .toLocalDate().plusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddApplicant2(YES);
        caseData.setApplicant1(PartyBuilder.builder().individual().build());
        caseData.setApplicant2(PartyBuilder.builder().individual().build());
        caseData.setCaseTypeFlag("ApplicantTwoFields");
        invoke(caseData, collectionField, date);

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseDataBefore.setAddApplicant2(YES);
        caseDataBefore.setApplicant1(PartyBuilder.builder().individual().build());
        caseDataBefore.setApplicant2(PartyBuilder.builder().individual().build());
        caseDataBefore.setCaseTypeFlag("ApplicantTwoFields");
        invoke(caseDataBefore, collectionField, List.of());
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "setWitnessOptionUploadDate,setDocumentWitnessStatement,Invalid date: \"witness statement\" "
            + "date entered must not be in the future (2).",
        "setWitnessOptionUploadDate,setDocumentWitnessSummary,Invalid date: \"witness summary\" "
            + "date entered must not be in the future (3).",
        "setWitnessOptionUploadDate,setDocumentHearsayNotice,Invalid date: \"Notice of the intention to rely on"
            + " hearsay evidence\" date entered must not be in the future (4)."
    })
    void shouldReturnError_whenWitnessOptionUploadDateInFuture(String dateField, String collectionField,
                                                               String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceWitness(), dateField,
            time.toLocalDate().plusWeeks(1))));

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "setWitnessOptionUploadDate,setDocumentWitnessStatement",
        "setWitnessOptionUploadDate,setDocumentHearsayNotice"
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePresent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceWitness(), dateField,
            time.toLocalDate())));

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setWitnessOptionUploadDate,setDocumentWitnessStatement",
        "setWitnessOptionUploadDate,setDocumentHearsayNotice"
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePast(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceWitness(), dateField,
            time.toLocalDate().minusWeeks(1))));

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setWitnessOptionUploadDate,setDocumentWitnessStatement,Invalid date: \"witness statement\" date entered must not be in the future (2).",
        "setWitnessOptionUploadDate,setDocumentHearsayNotice,Invalid date: \"Notice of the intention to rely on hearsay evidence\" " +
            "date entered must not be in the future (4)."
    })
    void shouldReturnError_whenOneDateIsInFutureForWitnessStatements(String dateField, String collectionField, String errorMessage) {
        //documentUploadWitness1 represents a collection so can have multiple dates entered at any time,
        // these dates should all be in the past, otherwise an error will be populated

        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceWitness(), dateField, time.toLocalDate().minusWeeks(1))));
        date.add(1, element(invoke(new UploadEvidenceWitness(), dateField, time.toLocalDate().plusWeeks(1))));
        //dates above represent valid past dates, date below represents invalid future date.
        date.add(2, element(invoke(new UploadEvidenceWitness(), dateField, time.toLocalDate().minusWeeks(1))));

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(errorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "setExpertOptionUploadDate,setDocumentExpertReport,Invalid date: \"Expert's report\""
            + " date entered must not be in the future (6).",
        "setExpertOptionUploadDate,setDocumentJointStatement,Invalid date: \"Joint statement of experts\" "
            + "date entered must not be in the future (7).",
        "setExpertOptionUploadDate,setDocumentQuestions,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (8).",
        "setExpertOptionUploadDate,setDocumentAnswers,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (9)."
    })
    void shouldReturnError_whenOneDateIsInFutureForExpertStatements(String dateField, String collectionField, String errorMessage) {
        //documentUploadWitness1 represents a collection so can have multiple dates entered at any time,
        // these dates should all be in the past, otherwise an error will be populated

        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceExpert(), dateField, time.toLocalDate().minusWeeks(1))));
        date.add(1, element(invoke(new UploadEvidenceExpert(), dateField, time.toLocalDate().plusWeeks(1))));
        //dates above represent valid past dates, date below represents invalid future date.
        date.add(2, element(invoke(new UploadEvidenceExpert(), dateField, time.toLocalDate().minusWeeks(1))));

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(errorMessage);
    }

    @Test
    void shouldCallExternalTask_whenAboutToSubmit() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
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
        var documentUpload = createUploadEvidenceWitness(
            LocalDate.of(2023, 2, 10),
            LocalDateTime.of(2022, 05, 10, 12, 13, 12),
            testDocument);
        List<Element<UploadEvidenceWitness>> documentList = new ArrayList<>();
        documentList.add(createElement(documentUpload));
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setDocumentHearsayNotice(documentList);
        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddRespondent2(YES);
        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddRespondent2(YES);
        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

        // When
        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

        // Then
        SubmittedCallbackResponse expectedResponse = SubmittedCallbackResponse.builder()
            .confirmationHeader(header)
            .confirmationBody(body)
            .build();
        assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    private <T, A> T invoke(T target, String method, A argument) {
        Class<?> parameterType = argument instanceof List ? List.class : argument.getClass();
        ReflectionUtils.invokeMethod(ReflectionUtils.getRequiredMethod(target.getClass(),
            method, parameterType), target, argument);
        return target;
    }

    private UserInfo createUserInfo(String uid) {
        UserInfo userInfo = UserInfo.builder().uid(uid).build();
        return userInfo;
    }

    private Document createDocument(String binaryUrl, String fileName) {
        Document document = Document.builder()
            .documentBinaryUrl(binaryUrl)
            .documentFileName(fileName)
            .build();
        return document;
    }

    private Document createDocumentWithUrl(String binaryUrl, String fileName, String url) {
        Document document = Document.builder()
            .documentBinaryUrl(binaryUrl)
            .documentFileName(fileName)
            .documentUrl(url)
            .build();
        return document;
    }

    private UploadEvidenceDocumentType createUploadEvidenceDocumentType(LocalDate issuedDate, String bundleName, Document document) {
        UploadEvidenceDocumentType evidenceDoc = new UploadEvidenceDocumentType();
        evidenceDoc.setDocumentIssuedDate(issuedDate);
        evidenceDoc.setBundleName(bundleName);
        evidenceDoc.setDocumentUpload(document);
        return evidenceDoc;
    }

    private UploadEvidenceDocumentType createUploadEvidenceDocumentTypeWithDateTime(LocalDateTime createdDate, LocalDate issuedDate, Document document) {
        UploadEvidenceDocumentType evidenceDoc = new UploadEvidenceDocumentType();
        evidenceDoc.setCreatedDatetime(createdDate);
        evidenceDoc.setDocumentIssuedDate(issuedDate);
        evidenceDoc.setDocumentUpload(document);
        return evidenceDoc;
    }

    private Bundle createBundle(String id, String title, String description, Optional<String> stitchStatus, Optional<LocalDateTime> createdOn) {
        Bundle bundle = new Bundle()
            .setId(id)
            .setTitle(title)
            .setDescription(description)
            .setStitchStatus(stitchStatus)
            .setCreatedOn(createdOn);
        return bundle;
    }

    private CaseDetails createCaseDetails() {
        CaseDetails caseDetails = CaseDetails.builder().build();
        return caseDetails;
    }

    private UploadEvidenceWitness createUploadEvidenceWitness(LocalDate uploadDate, LocalDateTime createdDate, Document document) {
        UploadEvidenceWitness witness = new UploadEvidenceWitness();
        witness.setWitnessOptionUploadDate(uploadDate);
        witness.setCreatedDatetime(createdDate);
        witness.setWitnessOptionDocument(document);
        return witness;
    }

    private UploadEvidenceExpert createUploadEvidenceExpert(LocalDate uploadDate, LocalDateTime createdDate, Document document) {
        UploadEvidenceExpert expert = new UploadEvidenceExpert();
        expert.setExpertOptionUploadDate(uploadDate);
        expert.setCreatedDatetime(createdDate);
        expert.setExpertDocument(document);
        return expert;
    }

    private <T> Element<T> createElement(T value) {
        Element<T> element = new Element<>();
        element.setValue(value);
        return element;
    }

    @Test
    void shouldAddApplicantEvidenceDocWhenBundleCreatedDateIsBeforeEvidenceUploaded_onNewCreatedBundle() {
        List<Element<UploadEvidenceDocumentType>> applicantDocsUploadedAfterBundle = new ArrayList<>();
        UploadEvidenceDocumentType document = new UploadEvidenceDocumentType();
        document.setCreatedDatetime(LocalDateTime.now(ZoneId.of("Europe/London")));
        applicantDocsUploadedAfterBundle.add(ElementUtils.element(document));
        // Given caseBundles with bundle created date is before witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        // populate applicantDocsUploadedAfterBundle with a default built element after a new bundle, it only
        // contains createdDatetime and no document, so will be removed from final list
        caseData.setApplicantDocsUploadedAfterBundle(applicantDocsUploadedAfterBundle);
        // added after trial bundle, so will  be added
        caseData.setDocumentWitnessStatement(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url22"));
        caseData.setDocumentExpertReport(getExpertDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url44"));
        caseData.setCaseBundles(prepareCaseBundles(LocalDateTime.of(2022, 04, 10, 12, 12, 12)));
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        // populate applicantDocsUploadedAfterBundle with an existing upload
        caseData.setApplicantDocsUploadedAfterBundle(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url77"));
        // added before trial bundle, so will not be added
        caseData.setDocumentAnswers(getExpertDocs(LocalDateTime.of(2022, 03, 10, 12, 13, 12), "url11"));
        caseData.setDocumentWitnessSummary(getWitnessDocs(LocalDateTime.of(2022, 03, 10, 12, 13, 12), "url22"));
        caseData.setDocumentQuestions(getExpertDocs(LocalDateTime.of(2022, 03, 10, 12, 13, 12), "url33"));
        // added after trial bundle, so will  be added
        caseData.setDocumentAuthorities(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url44"));
        caseData.setDocumentCosts(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url55"));
        caseData.setDocumentWitnessStatement(getWitnessDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url99"));
        caseData.setDocumentDisclosureList(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url88"));
        // this should not be added, as it has duplicate URL, indicating it already is in list, and should be skipped.
        caseData.setDocumentExpertReport(getExpertDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url77"));
        caseData.setCaseBundles(prepareCaseBundles(LocalDateTime.of(2022, 04, 10, 12, 12, 12)));
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setApplicantDocsUploadedAfterBundle(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"));
        caseData.setDocumentWitnessSummary(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url22"));
        caseData.setCaseBundles(prepareCaseBundles(LocalDateTime.of(2022, 05, 10, 12, 12, 12)));

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setDocumentDisclosureList(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"));
        caseData.setDocumentDisclosureListApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url2"));
        caseData.setDocumentForDisclosure(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url3"));
        caseData.setDocumentForDisclosureApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url4"));
        caseData.setDocumentReferredInStatement(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url5"));
        caseData.setDocumentReferredInStatementApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url6"));
        caseData.setDocumentCaseSummary(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url7"));
        caseData.setDocumentCaseSummaryApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url8"));
        caseData.setDocumentSkeletonArgument(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url9"));
        caseData.setDocumentSkeletonArgumentApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url10"));
        caseData.setDocumentAuthorities(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url11"));
        caseData.setDocumentAuthoritiesApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url12"));
        caseData.setDocumentCosts(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url13"));
        caseData.setDocumentCostsApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url14"));
        caseData.setDocumentHearsayNotice(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url15"));
        caseData.setDocumentHearsayNoticeApp2(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url16"));
        caseData.setDocumentWitnessSummary(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url17"));
        caseData.setDocumentWitnessSummaryApp2(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url18"));
        caseData.setDocumentQuestions(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url19"));
        caseData.setDocumentQuestionsApp2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url20"));
        caseData.setDocumentEvidenceForTrial(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url21"));
        caseData.setDocumentEvidenceForTrialApp2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url22"));
        caseData.setDocumentAnswers(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url23"));
        caseData.setDocumentAnswersApp2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url24"));
        caseData.setDocumentJointStatement(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url25"));
        caseData.setDocumentJointStatementApp2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url26"));
        caseData.setDocumentExpertReport(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url27"));
        caseData.setDocumentExpertReportApp2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url28"));
        caseData.setDocumentWitnessStatement(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url29"));
        caseData.setDocumentWitnessStatement(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url30"));
        caseData.setCaseBundles(prepareCaseBundles(LocalDateTime.of(2022, 05, 15, 12, 12, 12)));

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
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
        caseBundles.add(new IdValue<>("1", createBundle("1",
            "Trial Bundle",
            "Trial Bundle",
            Optional.of("NEW"),
            Optional.of(LocalDateTime.of(2022, 05, 15, 12, 12, 12)))));
        Bundle bundle2 = new Bundle()
            .setId("1")
            .setTitle("Trial Bundle");
        caseBundles.add(new IdValue<>("1", bundle2));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setDocumentQuestions(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url3"));
        caseData.setDocumentWitnessSummary(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url3"));
        caseData.setCaseBundles(caseBundles);

        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
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
        caseBundles.add(new IdValue<>("1", createBundle("1",
            "Trial Bundle",
            "Trial Bundle",
            Optional.of("NEW"),
            Optional.ofNullable(null))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setDocumentQuestions(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url3"));
        caseData.setDocumentWitnessSummary(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url3"));
        caseData.setCaseBundles(caseBundles);

        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddApplicant2(YES);
        caseData.setApplicant1(PartyBuilder.builder().individual().build());
        caseData.setApplicant2(PartyBuilder.builder().individual().build());
        caseData.setEvidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(Integer.parseInt(selected)), false));
        caseData.setDocumentWitnessSummary(
                        createWitnessDocs(witnessName, createdDate, witnessDate));
        caseData.setDocumentWitnessStatement(
                        createWitnessDocs(witnessName, createdDate, witnessDate));
        caseData.setDocumentHearsayNotice(createWitnessDocs(witnessName, createdDate, witnessDate));
        caseData.setDocumentExpertReport(createExpertDocs("expertName", witnessDate, "expertise", null, null, null, null));
        caseData.setDocumentJointStatement(createExpertDocs("expertsName", witnessDate, null, "expertises", null, null, null));
        caseData.setDocumentQuestions(createExpertDocs("expertName", witnessDate, null, null, "other", "question", null));
        caseData.setDocumentAnswers(createExpertDocs("expertName", witnessDate, null, null, "other", null, "answer"));
        caseData.setDocumentForDisclosure(createEvidenceDocs(null, null, "typeDisclosure", witnessDate));
        caseData.setDocumentReferredInStatement(createEvidenceDocs("witness", null, "typeReferred", witnessDate));
        caseData.setDocumentEvidenceForTrial(createEvidenceDocs(null, null, "typeForTrial", witnessDate));
        caseData.setDocumentDisclosureList(createEvidenceDocs(null, null, null, null));
        caseData.setDocumentCaseSummary(createEvidenceDocs(null, null, null, null));
        caseData.setDocumentSkeletonArgument(createEvidenceDocs(null, null, null, null));
        caseData.setDocumentAuthorities(createEvidenceDocs(null, null, null, null));
        caseData.setDocumentCosts(createEvidenceDocs(null, null, null, null));
        caseData.setBundleEvidence(createEvidenceDocs(null, "A bundle", null, bundleDate));
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseDataBefore.setAddApplicant2(YES);
        caseDataBefore.setApplicant1(PartyBuilder.builder().individual().build());
        caseDataBefore.setApplicant2(PartyBuilder.builder().individual().build());
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        given(coreCaseDataService.getCase(anyLong())).willReturn(createCaseDetails());
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddApplicant2(YES);
        caseData.setApplicant1(PartyBuilder.builder().individual().build());
        caseData.setApplicant2(PartyBuilder.builder().individual().build());
        caseData.setEvidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(1), false));
        caseData.setDocumentWitnessSummaryApp2(createWitnessDocs(witnessName, createdDate, witnessDate));
        caseData.setDocumentWitnessStatementApp2(createWitnessDocs(witnessName, createdDate, witnessDate));
        caseData.setDocumentHearsayNoticeApp2(createWitnessDocs(witnessName, createdDate, witnessDate));
        caseData.setDocumentExpertReportApp2(createExpertDocs("expertName", witnessDate, "expertise", null, null, null, null));
        caseData.setDocumentJointStatementApp2(createExpertDocs("expertsName", witnessDate, null, "expertises", null, null, null));
        caseData.setDocumentQuestionsApp2(createExpertDocs("expertName", witnessDate, null, null, "other", "question", null));
        caseData.setDocumentAnswersApp2(createExpertDocs("expertName", witnessDate, null, null, "other", null, "answer"));
        caseData.setDocumentForDisclosureApp2(createEvidenceDocs(null, null, "typeDisclosure", witnessDate));
        caseData.setDocumentReferredInStatementApp2(createEvidenceDocs("witness", null, "typeReferred", witnessDate));
        caseData.setDocumentEvidenceForTrialApp2(createEvidenceDocs(null, null, "typeForTrial", witnessDate));
        caseData.setDocumentDisclosureListApp2(createEvidenceDocs(null, null, null, null));
        caseData.setDocumentCaseSummaryApp2(createEvidenceDocs(null, null, null, null));
        caseData.setDocumentSkeletonArgumentApp2(createEvidenceDocs(null, null, null, null));
        caseData.setDocumentAuthoritiesApp2(createEvidenceDocs(null, null, null, null));
        caseData.setDocumentCostsApp2(createEvidenceDocs(null, null, null, null));
        caseData.setBundleEvidence(createEvidenceDocs(null, "A bundle", null, bundleDate));
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));

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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setNotificationText("Documentation that has been uploaded: \n\n Claimant 1 - Joint Statement of Experts / Single Joint Expert Report \n");
        caseData.setApplicant1(PartyBuilder.builder().individual().build());
        caseData.setEvidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(0), false));
        caseData.setDocumentJointStatement(createExpertDocs("expertsName", witnessDate, null, "expertises", null, null, null));
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        given(coreCaseDataService.getCase(anyLong())).willReturn(createCaseDetails());
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setNotificationText(null);
        caseData.setEvidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(0), false));
        caseData.setDocumentWitnessStatement(createWitnessDocs(witnessName, time.minusDays(2), witnessDate));
        caseData.setDocumentWitnessSummary(createWitnessDocs(witnessName, time, witnessDate));
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(createUserInfo("uid"));
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        given(coreCaseDataService.getCase(anyLong())).willReturn(createCaseDetails());
        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then Notification should not have old entry (witness statement)
        assertThat(updatedData.getNotificationText()).isEqualTo("\nClaimant 1 - Witness summary");
    }

    private List<Element<UploadEvidenceDocumentType>> createEvidenceDocs(String name, String bundleName, String type, LocalDate issuedDate) {
        Document document = createDocument(TEST_URL, TEST_FILE_NAME);
        UploadEvidenceDocumentType evidenceDoc = new UploadEvidenceDocumentType();
        evidenceDoc.setWitnessOptionName(name);
        evidenceDoc.setBundleName(bundleName);
        evidenceDoc.setTypeOfDocument(type);
        evidenceDoc.setDocumentIssuedDate(issuedDate);
        evidenceDoc.setDocumentUpload(document);
        List<Element<UploadEvidenceDocumentType>> evidenceDocs = new ArrayList<>();
        evidenceDocs.add(ElementUtils.element(evidenceDoc));
        return evidenceDocs;
    }

    private List<Element<UploadEvidenceExpert>> createExpertDocs(String expertName,
                                                                 LocalDate dateUpload,
                                                                 String expertise,
                                                                 String expertises,
                                                                 String otherParty,
                                                                 String question,
                                                                 String answer) {
        Document document = createDocument(TEST_URL, TEST_FILE_NAME);
        UploadEvidenceExpert expertDoc = new UploadEvidenceExpert();
        expertDoc.setExpertDocument(document);
        expertDoc.setExpertOptionName(expertName);
        expertDoc.setExpertOptionExpertise(expertise);
        expertDoc.setExpertOptionExpertises(expertises);
        expertDoc.setExpertOptionOtherParty(otherParty);
        expertDoc.setExpertDocumentQuestion(question);
        expertDoc.setExpertDocumentAnswer(answer);
        expertDoc.setExpertOptionUploadDate(dateUpload);
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(expertDoc));
        return expertEvidenceDocs;
    }

    private List<Element<UploadEvidenceWitness>> createWitnessDocs(String witnessName,
                                                                   LocalDateTime createdDate,
                                                                   LocalDate dateMade) {
        Document document = createDocument(TEST_URL, TEST_FILE_NAME);
        UploadEvidenceWitness witnessDoc = new UploadEvidenceWitness();
        witnessDoc.setWitnessOptionDocument(document);
        witnessDoc.setWitnessOptionName(witnessName);
        witnessDoc.setCreatedDatetime(createdDate);
        witnessDoc.setWitnessOptionUploadDate(dateMade);
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessEvidenceDocs.add(ElementUtils.element(witnessDoc));
        return witnessEvidenceDocs;
    }

    private List<IdValue<Bundle>> prepareCaseBundles(LocalDateTime bundleCreatedDate) {
        List<IdValue<Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", createBundle("1",
            "Trial Bundle",
            "Trial Bundle",
            Optional.of("NEW"),
            Optional.of(bundleCreatedDate))));
        return caseBundles;
    }

    private List<Element<UploadEvidenceWitness>> getWitnessDocs(LocalDateTime uploadedDate, String uniqueUrl) {
        Document document = createDocumentWithUrl(TEST_URL, TEST_FILE_NAME, uniqueUrl);
        UploadEvidenceWitness witnessDoc = new UploadEvidenceWitness();
        witnessDoc.setWitnessOptionDocument(document);
        witnessDoc.setWitnessOptionName("FirstName LastName");
        witnessDoc.setCreatedDatetime(uploadedDate);
        witnessDoc.setWitnessOptionUploadDate(LocalDate.of(2023, 2, 10));
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessEvidenceDocs.add(ElementUtils.element(witnessDoc));
        return witnessEvidenceDocs;
    }

    private List<Element<UploadEvidenceExpert>> getExpertDocs(LocalDateTime uploadedDate, String uniqueUrl) {
        Document document = createDocumentWithUrl(TEST_URL, TEST_FILE_NAME, uniqueUrl);
        UploadEvidenceExpert expertDoc = new UploadEvidenceExpert();
        expertDoc.setCreatedDatetime(uploadedDate);
        expertDoc.setExpertOptionUploadDate(LocalDate.now());
        expertDoc.setExpertDocument(document);
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(expertDoc));
        return expertEvidenceDocs;
    }

    private List<Element<UploadEvidenceDocumentType>> getUploadEvidenceDocumentTypeDocs(LocalDateTime uploadedDate, String uniqueUrl) {
        Document document = createDocumentWithUrl(TEST_URL, TEST_FILE_NAME, uniqueUrl);
        UploadEvidenceDocumentType evidenceDoc = new UploadEvidenceDocumentType();
        evidenceDoc.setCreatedDatetime(uploadedDate);
        evidenceDoc.setDocumentIssuedDate(LocalDate.now());
        evidenceDoc.setDocumentUpload(document);
        List<Element<UploadEvidenceDocumentType>> uploadEvidenceDocs = new ArrayList<>();
        uploadEvidenceDocs.add(ElementUtils.element(evidenceDoc));
        return uploadEvidenceDocs;
    }

}

