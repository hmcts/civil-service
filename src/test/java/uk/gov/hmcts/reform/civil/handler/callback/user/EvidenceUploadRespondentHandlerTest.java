package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.civil.config.RespondentEvidenceHandlerTestConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadExpert;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.RespondentDocumentUploadTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.RespondentSetOptionsTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory;
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
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOADED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_RESPONDENT;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadExpert.EXPERT_REPORT;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadExpert.JOINT_STATEMENT;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial.AUTHORITIES;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial.COSTS;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial.DOCUMENTARY;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness.DOCUMENTS_REFERRED;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness.WITNESS_STATEMENT;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness.WITNESS_SUMMARY;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    EvidenceUploadRespondentHandler.class,
    JacksonAutoConfiguration.class,
    RespondentSetOptionsTask.class,
    RespondentDocumentUploadTask.class,
    RespondentEvidenceHandlerTestConfiguration.class
})
class EvidenceUploadRespondentHandlerTest extends BaseCallbackHandlerTest {

    private static final String TEST_URL = "url";
    private static final String TEST_FILE_NAME = "testFileName.pdf";
    private static final String UPLOAD_TIMESTAMP = "14 Apr 2024 00:00:00";
    @Autowired
    private EvidenceUploadRespondentHandler handler;

    @MockBean
    private FeatureToggleService featureToggleService;

    private final LocalDateTime time = LocalDateTime.now();

    @Autowired
    private RespondentSetOptionsTask respondentSetOptionsTask;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    private final UploadEvidenceExpert uploadEvidenceDate = new UploadEvidenceExpert();
    private final UploadEvidenceWitness uploadEvidenceDate2 = new UploadEvidenceWitness();
    private final UploadEvidenceDocumentType uploadEvidenceDate3 = new UploadEvidenceDocumentType();
    private static final String NotificationWhenBothDefendant = "\n"
        + "Both defendants - Disclosure list\n"
        + "Both defendants - Documents for disclosure\n"
        + "Both defendants - Documents referred to in the statement\n"
        + "Both defendants - Expert's report\n"
        + "Both defendants - Joint Statement of Experts / Single Joint Expert Report\n"
        + "Both defendants - Questions for other party's expert or joint experts\n"
        + "Both defendants - Answer to questions asked\n"
        + "Both defendants - Case Summary\n"
        + "Both defendants - Skeleton argument\n"
        + "Both defendants - Authorities\n"
        + "Both defendants - Costs\n"
        + "Both defendants - Documentary evidence for trial";
    private static final String NotificationWhenDefendantTwo = "\n"
        + "Defendant 2 - Disclosure list\n"
        + "Defendant 2 - Documents for disclosure\n"
        + "Defendant 2 - Documents referred to in the statement\n"
        + "Defendant 2 - Expert's report\n"
        + "Defendant 2 - Joint Statement of Experts / Single Joint Expert Report\n"
        + "Defendant 2 - Questions for other party's expert or joint experts\n"
        + "Defendant 2 - Answer to questions asked\n"
        + "Defendant 2 - Case Summary\n"
        + "Defendant 2 - Skeleton argument\n"
        + "Defendant 2 - Authorities\n"
        + "Defendant 2 - Costs\n"
        + "Defendant 2 - Documentary evidence for trial";
    private static final String PAGE_ID = "validateValuesRespondent";

    @BeforeEach
    void setup() {
        given(featureToggleService.isCaseProgressionEnabled()).willReturn(false);
    }

    @Test
    void givenAboutToStart_assignCaseProgAllocatedTrackUnSpec() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .notificationText("NULLED")
            .allocatedTrack(FAST_CLAIM)
            .responseClaimTrack(null)
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
    void givenAboutToStart_assignCaseProgAllocatedTrackSpec() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .notificationText(null)
            .allocatedTrack(null)
            .responseClaimTrack("SMALL_CLAIM")
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
    void givenAboutToStart_1v2SameSolicitor_shouldShowOptions() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .notificationText("existing notification")
            .claimType(null)
            .totalClaimAmount(BigDecimal.valueOf(12500))
            .addRespondent2(YES)
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(YES)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        assertThat(response.getData()).extracting("evidenceUploadOptions").isNotNull();
        assertThat(response.getData()).extracting("notificationText").isEqualTo("existing notification");
    }

    @ParameterizedTest
    @CsvSource({"0", "1", "2"})
    void givenCreateShow_1v2SameSolicitor_RespondentTwoFlag(String selected) {
        // Given
        List<String> options = List.of(EvidenceUploadHandlerBase.OPTION_DEF1,
            EvidenceUploadHandlerBase.OPTION_DEF2,
            EvidenceUploadHandlerBase.OPTION_DEF_BOTH);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(YES)
            .evidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(Integer.parseInt(selected)), false))
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        if (!selected.equals("1")) {
            assertThat(response.getData()).extracting("caseTypeFlag").isNotEqualTo("RespondentTwoFields");
        } else {
            assertThat(response.getData()).extracting("caseTypeFlag").isEqualTo("RespondentTwoFields");
        }
    }

    @Test
    void givenCreateShow_1v2DifferentSolicitorsWillChangeToRespondentTwoFlag() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        assertThat(response.getData()).extracting("caseTypeFlag").isEqualTo("RespondentTwoFields");
    }

    @Test
    void givenCreateShow_1v2DifferentSolicitorsWillChangeToRespondentTwoFlagSpec() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YES)
            .caseAccessCategory(SPEC_CLAIM)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        assertThat(response.getData()).extracting("caseTypeFlag").isEqualTo("RespondentTwoFields");
    }

    @Test
    void givenCreateShow_1v1WillNotChangeToRespondentTwoFlag() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        // Then
        assertThat(response.getData()).extracting("caseTypeFlag").isNotEqualTo("RespondentTwoFields");
    }

    static Stream<Arguments> witnessOptionsSelected() {
        List<EvidenceUploadWitness> witnessList = new ArrayList<>();
        witnessList.add(WITNESS_STATEMENT);
        witnessList.add(WITNESS_SUMMARY);
        witnessList.add(DOCUMENTS_REFERRED);
        return Stream.of(
            arguments(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .witnessSelectionEvidenceRes(witnessList).build()),
            arguments(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .witnessSelectionEvidenceSmallClaimRes(witnessList).build())
        );
    }

    static Stream<Arguments> expertOptionsSelected() {
        List<EvidenceUploadExpert> expertList = new ArrayList<>();
        expertList.add(EXPERT_REPORT);
        expertList.add(JOINT_STATEMENT);
        return Stream.of(
            arguments(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .expertSelectionEvidenceRes(expertList).build()),
            arguments(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .expertSelectionEvidenceSmallClaimRes(expertList).build())
        );
    }

    static Stream<Arguments> trialOptionsSelected() {
        List<EvidenceUploadTrial> trialList = new ArrayList<>();
        trialList.add(AUTHORITIES);
        trialList.add(DOCUMENTARY);
        trialList.add(COSTS);
        return Stream.of(
            arguments(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .trialSelectionEvidenceRes(trialList).build()),
            arguments(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .trialSelectionEvidenceSmallClaimRes(trialList).build())
        );
    }

    static Stream<Arguments> optionsNotSelected() {

        return Stream.of(
            arguments(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .build())
        );
    }

    @ParameterizedTest
    @MethodSource("witnessOptionsSelected")
    void shouldSetWitnessFlag_whenWitnessOptionsAreSelected(CaseData caseData) {
        // Given
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getData()).extracting("witnessStatementFlag").isEqualTo("show_witness_statement");
        assertThat(response.getData()).extracting("witnessSummaryFlag").isEqualTo("show_witness_summary");
        assertThat(response.getData()).extracting("witnessReferredStatementFlag").isEqualTo("show_witness_referred");
    }

    @ParameterizedTest
    @MethodSource("optionsNotSelected")
    void shouldNotSetWitnessFlag_whenWitnessOptionsAreNotSelected(CaseData caseData) {
        // Given
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getData()).extracting("witnessStatementFlag").isEqualTo("do_not_show");
        assertThat(response.getData()).extracting("witnessSummaryFlag").isEqualTo("do_not_show");
        assertThat(response.getData()).extracting("witnessReferredStatementFlag").isEqualTo("do_not_show");
    }

    @ParameterizedTest
    @MethodSource("expertOptionsSelected")
    void shouldSetExpertFlag_whenExpertOptionsAreSelected(CaseData caseData) {
        // Given
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getData()).extracting("expertReportFlag").isEqualTo("show_expert_report");
        assertThat(response.getData()).extracting("expertJointFlag").isEqualTo("show_joint_expert");
    }

    @ParameterizedTest
    @MethodSource("optionsNotSelected")
    void shouldNotSetExpertFlag_whenExpertOptionsAreNotSelected(CaseData caseData) {
        // Given
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getData()).extracting("expertReportFlag").isEqualTo("do_not_show");
        assertThat(response.getData()).extracting("expertJointFlag").isEqualTo("do_not_show");
    }

    @ParameterizedTest
    @MethodSource("trialOptionsSelected")
    void shouldSetTrialFlag_whenTrialOptionsAreSelected(CaseData caseData) {
        // Given
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getData()).extracting("trialAuthorityFlag").isEqualTo("show_trial_authority");
        assertThat(response.getData()).extracting("trialDocumentaryFlag").isEqualTo("show_trial_documentary");
        assertThat(response.getData()).extracting("trialCostsFlag").isEqualTo("show_trial_costs");
    }

    @ParameterizedTest
    @MethodSource("optionsNotSelected")
    void shouldNotSetTrialFlag_whenTrialOptionsAreNotSelected(CaseData caseData) {
        // Given
        CallbackParams params = callbackParamsOf(caseData, MID, "createShowCondition");
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getData()).extracting("trialAuthorityFlag").isEqualTo("do_not_show");
        assertThat(response.getData()).extracting("trialDocumentaryFlag").isEqualTo("do_not_show");
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosureRes",
        "documentIssuedDate,documentReferredInStatementRes",
        "documentIssuedDate,documentEvidenceForTrialRes",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePastOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time
            .toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build().toBuilder(), collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosureRes",
        "documentIssuedDate,documentReferredInStatementRes",
        "documentIssuedDate,documentEvidenceForTrialRes",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePresentOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time
            .toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build().toBuilder(), collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosureRes, Invalid date: \"Documents for disclosure\""
            + " date entered must not be in the future (1).",
        "documentIssuedDate,documentForDisclosureRes, Invalid date: \"Documents for disclosure\""
            + " date entered must not be in the future (1).",
        "documentIssuedDate,documentReferredInStatementRes, Invalid date: \"Documents referred to in the statement\""
            + " date entered must not be in the future (5).",
    })
    void shouldReturnError_whenDocumentTypeUploadDateFutureOneRespondent(String dateField, String collectionField,
                                                                         String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time
            .toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build().toBuilder(), collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReportRes",
        "expertOptionUploadDate,documentJointStatementRes",
        "expertOptionUploadDate,documentQuestionsRes",
        "expertOptionUploadDate,documentAnswersRes",
    })
    void shouldNotReturnError_whenExpert2OptionUploadDatePastOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time
            .toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build().toBuilder(), collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReportRes",
        "expertOptionUploadDate,documentJointStatementRes",
        "expertOptionUploadDate,documentQuestionsRes",
        "expertOptionUploadDate,documentAnswersRes",
    })
    void shouldNotReturnError_whenExpertOptionUploadDatePresentOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time
            .toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build().toBuilder(), collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReportRes,Invalid date: \"Expert's report\""
            + " date entered must not be in the future (6).",
        "expertOptionUploadDate,documentJointStatementRes,Invalid date: \"Joint statement of experts\" "
            + "date entered must not be in the future (7).",
        "expertOptionUploadDate,documentQuestionsRes,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (8).",
        "expertOptionUploadDate,documentAnswersRes,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (9).",
    })
    void shouldReturnError_whenExpertOptionUploadDateFutureOneRespondent(String dateField, String collectionField,
                                                                         String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time
            .toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
            collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatementRes,Invalid date: \"witness statement\" "
            + "date entered must not be in the future (2).",
        "witnessOptionUploadDate,documentWitnessSummaryRes,Invalid date: \"witness summary\" "
            + "date entered must not be in the future (3).",
        "witnessOptionUploadDate,documentHearsayNoticeRes,Invalid date: \"Notice of the intention to rely on"
            + " hearsay evidence\" date entered must not be in the future (4).",
    })
    void shouldReturnError_whenWitnessOptionUploadDateInFutureOneRespondent(String dateField, String collectionField,
                                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
            time.toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
            collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatementRes",
        "witnessOptionUploadDate,documentHearsayNoticeRes",
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePresentOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
            time.toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
            collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatementRes",
        "witnessOptionUploadDate,documentHearsayNoticeRes",
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePastOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
            time.toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
            collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatementRes,Invalid date: \"witness statement\" date entered must not be in the future (2).",
        "witnessOptionUploadDate,documentHearsayNoticeRes,Invalid date: \"Notice of the intention to rely on hearsay evidence\" " +
            "date entered must not be in the future (4).",
        "witnessOptionUploadDate,documentWitnessStatementRes,Invalid date: \"witness statement\" date entered must not be in the future (2).",
        "witnessOptionUploadDate,documentHearsayNoticeRes,Invalid date: \"Notice of the intention to rely on hearsay evidence\" " +
            "date entered must not be in the future (4)."
    })
    void shouldReturnError_whenOneDateIsInFutureForWitnessStatementsOneRespondent(String dateField, String collectionField, String errorMessage) {
        //documentUploadWitness1 represents a collection so can have multiple dates entered at any time,
        // these dates should all be in the past, otherwise an error will be populated

        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField, time.toLocalDate().minusWeeks(1)).build()));
        date.add(1, element(invoke(uploadEvidenceDate2.toBuilder(), dateField, time.toLocalDate().plusWeeks(1)).build()));
        //dates above represent valid past dates, date below represents invalid future date.
        date.add(2, element(invoke(uploadEvidenceDate2.toBuilder(), dateField, time.toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(), collectionField, date)
            .addRespondent2(NO)
            .caseTypeFlag("do_not_show")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(errorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosureRes2",
        "documentIssuedDate,documentReferredInStatementRes2",
        "documentIssuedDate,documentEvidenceForTrialRes2",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePastTwoRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time
            .toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build().toBuilder(), collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosureRes2",
        "documentIssuedDate,documentReferredInStatementRes2",
        "documentIssuedDate,documentEvidenceForTrialRes2",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePresentTwoRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time
            .toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build().toBuilder(), collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "documentIssuedDate,documentForDisclosureRes2, Invalid date: \"Documents for disclosure\""
            + " date entered must not be in the future (1).",
        "documentIssuedDate,documentReferredInStatementRes2, Invalid date: \"Documents referred to in the statement\""
            + " date entered must not be in the future (5).",
        "documentIssuedDate,documentEvidenceForTrialRes2, Invalid date: \"Documentary evidence for trial\""
            + " date entered must not be in the future (10).",
    })
    void shouldReturnError_whenDocumentTypeUploadDateFutureTwoRespondent(String dateField, String collectionField,
                                                                         String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate3.toBuilder(), dateField, time
            .toLocalDate().plusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build().toBuilder(), collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReportRes2",
        "expertOptionUploadDate,documentJointStatementRes2",
        "expertOptionUploadDate,documentQuestionsRes2",
        "expertOptionUploadDate,documentAnswersRes2"
    })
    void shouldNotReturnError_whenExpert2OptionUploadDatePastTwoRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time
            .toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build().toBuilder(), collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReportRes2",
        "expertOptionUploadDate,documentJointStatementRes2",
        "expertOptionUploadDate,documentQuestionsRes2",
        "expertOptionUploadDate,documentAnswersRes2"
    })
    void shouldNotReturnError_whenExpertOptionUploadDatePresentTwoRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time
            .toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build().toBuilder(), collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReportRes2,Invalid date: \"Expert's report\""
            + " date entered must not be in the future (6).",
        "expertOptionUploadDate,documentJointStatementRes2,Invalid date: \"Joint statement of experts\" "
            + "date entered must not be in the future (7).",
        "expertOptionUploadDate,documentQuestionsRes2,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (8).",
        "expertOptionUploadDate,documentAnswersRes2,Invalid date: \"Answers to questions asked by the other party\" "
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
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatementRes2,Invalid date: \"witness statement\" "
            + "date entered must not be in the future (2).",
        "witnessOptionUploadDate,documentHearsayNoticeRes2,Invalid date: \"Notice of the intention to rely on"
            + " hearsay evidence\" date entered must not be in the future (4).",
        "witnessOptionUploadDate,documentWitnessStatementRes2,Invalid date: \"witness statement\" "
            + "date entered must not be in the future (2).",
        "witnessOptionUploadDate,documentHearsayNoticeRes2,Invalid date: \"Notice of the intention to rely on"
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
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatementRes2",
        "witnessOptionUploadDate,documentHearsayNoticeRes2",
        "witnessOptionUploadDate,documentWitnessStatementRes2",
        "witnessOptionUploadDate,documentHearsayNoticeRes2"
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePresent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
            time.toLocalDate()).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
            collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "witnessOptionUploadDate,documentWitnessStatementRes2",
        "witnessOptionUploadDate,documentHearsayNoticeRes2",
        "witnessOptionUploadDate,documentWitnessStatementRes2",
        "witnessOptionUploadDate,documentHearsayNoticeRes2"
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePast(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate2.toBuilder(), dateField,
            time.toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(),
            collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "expertOptionUploadDate,documentExpertReportRes2,Invalid date: \"Expert's report\""
            + " date entered must not be in the future (6).",
        "expertOptionUploadDate,documentAnswersRes2,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (9).",
        "expertOptionUploadDate,documentQuestionsRes2,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (8).",
        "expertOptionUploadDate,documentAnswersRes2,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (9)."
    })
    void shouldReturnError_whenOneDateIsInFutureForExpertStatementsTwoRespondents(String dateField, String collectionField, String errorMessage) {
        //documentUploadWitness1 represents a collection so can have multiple dates entered at any time,
        // these dates should all be in the past, otherwise an error will be populated

        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.toLocalDate().minusWeeks(1)).build()));
        date.add(1, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.toLocalDate().plusWeeks(1)).build()));
        //dates above represent valid past dates, date below represents invalid future date.
        date.add(2, element(invoke(uploadEvidenceDate.toBuilder(), dateField, time.toLocalDate().minusWeeks(1)).build()));

        CaseData caseData = invoke(CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder(), collectionField, date)
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2SameLegalRepresentative(NO)
            .caseTypeFlag("RespondentTwoFields")
            .build();
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
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then
        assertThat(updatedData.getCaseDocumentUploadDateRes()).isCloseTo(time, within(30, ChronoUnit.SECONDS));
    }

    @Test
    void shouldAssignCategoryID_whenDocumentExistsTwoRespondentSpec() {
        Document testDocument = new Document("testurl",
            "testBinUrl", "A Fancy Name",
            "hash", null, UPLOAD_TIMESTAMP);
        var documentUpload = UploadEvidenceDocumentType.builder()
            .documentIssuedDate(LocalDate.of(2022, 2, 10))
            .createdDatetime(LocalDateTime.of(2022, 05, 10, 12, 13, 12))
            .documentUpload(testDocument).build();
        List<Element<UploadEvidenceDocumentType>> documentList = new ArrayList<>();
        documentList.add(Element.<UploadEvidenceDocumentType>builder().value(documentUpload).build());
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(YES)
            .documentForDisclosureRes2(documentList)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getDocumentForDisclosureRes2().get(0).getValue().getDocumentUpload()
            .getCategoryID()).isEqualTo("RespondentTwoDisclosure");
    }

    @Test
    void shouldAssignCategoryID_whenDocumentExistsTwoRespondentUnSpec() {
        Document testDocument = new Document("testurl",
            "testBinUrl", "A Fancy Name",
            "hash", null, UPLOAD_TIMESTAMP);
        var documentUpload = UploadEvidenceDocumentType.builder()
            .documentIssuedDate(LocalDate.of(2022, 2, 10))
            .createdDatetime(LocalDateTime.of(2022, 05, 10, 12, 13, 12))
            .documentUpload(testDocument).build();
        List<Element<UploadEvidenceDocumentType>> documentList = new ArrayList<>();
        documentList.add(Element.<UploadEvidenceDocumentType>builder().value(documentUpload).build());
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(YES)
            .documentForDisclosureRes2(documentList)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getDocumentForDisclosureRes2().get(0).getValue().getDocumentUpload()
            .getCategoryID()).isEqualTo("RespondentTwoDisclosure");
    }

    @Test
    void shouldAssignCategoryID_whenDocumentExistsOneRespondentSpec() {
        Document testDocument = new Document("testurl",
            "testBinUrl", "A Fancy Name",
            "hash", null, UPLOAD_TIMESTAMP);
        var documentUpload = UploadEvidenceExpert.builder()
            .expertDocument(testDocument)
            .createdDatetime(LocalDateTime.of(2022, 05, 10, 12, 13, 12))
            .build();
        List<Element<UploadEvidenceExpert>> documentList = new ArrayList<>();
        documentList.add(Element.<UploadEvidenceExpert>builder().value(documentUpload).build());
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .documentAnswersRes(documentList)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getDocumentAnswersRes().get(0).getValue().getExpertDocument()
            .getCategoryID()).isEqualTo("RespondentOneExpertAnswers");
    }

    @Test
    void shouldAssignCategoryID_whenDocumentExistsOneRespondentUnSpec() {
        Document testDocument = new Document("testurl",
            "testBinUrl", "A Fancy Name",
            "hash", null, UPLOAD_TIMESTAMP);
        var documentUpload = UploadEvidenceExpert.builder()
            .expertDocument(testDocument)
            .createdDatetime(LocalDateTime.of(2022, 05, 10, 12, 13, 12))
            .build();
        List<Element<UploadEvidenceExpert>> documentList = new ArrayList<>();
        documentList.add(Element.<UploadEvidenceExpert>builder().value(documentUpload).build());
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .documentQuestionsRes(documentList)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getDocumentQuestionsRes().get(0).getValue().getExpertDocument()
            .getCategoryID()).isEqualTo("RespondentOneExpertQuestions");
    }

    @Test
    void shouldNotAssignCategoryID_whenDocumentNotExistsTwoRespondent() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(YES)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getDocumentForDisclosureRes2()).isNull();
    }

    @Test
    void shouldNotAssignCategoryID_whenDocumentNotExistsOneRespondent() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .build();
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // When
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getDocumentAnswersRes()).isNull();
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
        assertThat(registerTarget).containsExactly(entry(EVIDENCE_UPLOAD_RESPONDENT.name(), handler));
    }

    private <T, A> T invoke(T target, String method, A argument) {
        ReflectionUtils.invokeMethod(ReflectionUtils.getRequiredMethod(target.getClass(),
            method, argument.getClass()), target, argument);
        return target;
    }

    @Test
    void shouldAddRespondentEvidenceDocWhenBundleCreatedDateIsBeforeEvidenceUploaded_onNewCreatedBundle() {
        List<Element<UploadEvidenceDocumentType>> respondentDocsUploadedAfterBundle = new ArrayList<>();
        UploadEvidenceDocumentType document = new UploadEvidenceDocumentType();
        document.setCreatedDatetime(LocalDateTime.now(ZoneId.of("Europe/London")));
        respondentDocsUploadedAfterBundle.add(ElementUtils.element(document));
        // Given caseBundles with bundle created date is before witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            // populate respondentDocsUploadedAfterBundle with a default built element after a new bundle, it only
            // contains createdDatetime and no document, so will be removed from final list
            .respondentDocsUploadedAfterBundle(respondentDocsUploadedAfterBundle)
            // added after trial bundle, so will  be added
            .documentWitnessStatementRes(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url22"))
            .documentExpertReportRes(getExpertDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url44"))
            .caseBundles(prepareCaseBundles(LocalDateTime.of(2022, 04, 10, 12, 12, 12))).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then respondent docs uploaded after bundle should return size 2, 2 new docs and 1 being removed.
        assertThat(updatedData.getRespondentDocsUploadedAfterBundle()).hasSize(2);
    }

    @Test
    void shouldAddRespondentEvidenceDocWhenBundleCreatedDateIsBeforeEvidenceUploaded() {
        // Given caseBundles with bundle created date is before witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            // populate respondentDocsUploadedAfterBundle with an existing upload
            .respondentDocsUploadedAfterBundle(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url66"))
            // added before trial bundle, so will not be added
            .documentQuestionsRes(getExpertDocs(LocalDateTime.of(2022, 03, 10, 12, 13, 12), "url11"))
            .documentWitnessSummaryRes(getWitnessDocs(LocalDateTime.of(2022, 03, 10, 12, 13, 12), "url22"))
            .documentQuestionsRes2(getExpertDocs(LocalDateTime.of(2022, 03, 10, 12, 13, 12), "url33"))
            // added after trial bundle, so will  be added
            .documentExpertReportRes(getExpertDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url44"))
            .documentWitnessSummaryRes2(getWitnessDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url55"))
            .documentWitnessStatementRes(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url22"))
            .documentDisclosureListRes2(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url77"))
            // this should not be added, as it has duplicate URL, indicating it already is in list, and should be skipped.
            .documentDisclosureListRes(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url66"))
            .caseBundles(prepareCaseBundles(LocalDateTime.of(2022, 04, 10, 12, 12, 12))).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then respondent docs uploaded after bundle should return size 5, 4 new docs and 1 existing.
        assertThat(updatedData.getRespondentDocsUploadedAfterBundle()).hasSize(5);
    }

    @Test
    void shouldNotAddRespondentEvidenceDocWhenBundleCreatedDateIsAfterEvidenceUploaded() {
        // Given caseBundles with bundle created date is after witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .documentDisclosureListRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"))
            .documentDisclosureListRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url2"))
            .documentForDisclosureRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url3"))
            .documentForDisclosureRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url4"))
            .documentReferredInStatementRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url5"))
            .documentReferredInStatementRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url6"))
            .documentCaseSummaryRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url7"))
            .documentCaseSummaryRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url8"))
            .documentSkeletonArgumentRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url9"))
            .documentSkeletonArgumentRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url10"))
            .documentAuthoritiesRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url11"))
            .documentAuthoritiesRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url12"))
            .documentCostsRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url13"))
            .documentCostsRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url14"))
            .documentHearsayNoticeRes(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url15"))
            .documentHearsayNoticeRes2(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url16"))
            .documentWitnessSummaryRes(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url17"))
            .documentWitnessSummaryRes2(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url18"))
            .documentQuestionsRes(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url19"))
            .documentQuestionsRes2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url20"))
            .documentEvidenceForTrialRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url21"))
            .documentEvidenceForTrialRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url22"))
            .documentAnswersRes(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url23"))
            .documentAnswersRes2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url24"))
            .documentJointStatementRes(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url25"))
            .documentJointStatementRes2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url26"))
            .documentExpertReportRes(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url27"))
            .documentExpertReportRes2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url28"))
            .documentWitnessStatementRes(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url29"))
            .documentWitnessStatementRes2(getWitnessDocs(
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
        assertThat(updatedData.getRespondentDocsUploadedAfterBundle()).isNull();
    }

    @Test
    void shouldAddAdditionalBundleDocuments_RespondentEvidenceDocWhenBundleCreatedDateIsBeforeEvidenceUploaded() {
        // Given caseBundles with bundle created date is before witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondentDocsUploadedAfterBundle(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"))
            .documentWitnessSummaryRes(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url22"))
            .caseBundles(prepareCaseBundles(LocalDateTime.of(2022, 05, 10, 12, 12, 12))).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then applicant docs uploaded after bundle should return size 2
        assertThat(updatedData.getRespondentDocsUploadedAfterBundle()).hasSize(2);
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
            .documentQuestionsRes(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"))
            .documentWitnessSummaryRes(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"))
            .caseBundles(caseBundles)
            .build();

        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
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
            .documentQuestionsRes(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"))
            .documentWitnessSummaryRes(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"))
            .caseBundles(caseBundles)
            .build();

        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
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
    void should_do_naming_convention_resp1(String selected) {
        LocalDateTime createdDate = LocalDateTime.of(2022, 05, 10, 12, 13, 12);
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        String witnessName = "ResOneWitness";
        List<String> options = List.of(EvidenceUploadHandlerBase.OPTION_DEF1,
            EvidenceUploadHandlerBase.OPTION_DEF2,
            EvidenceUploadHandlerBase.OPTION_DEF_BOTH);
        LocalDate witnessDate = LocalDate.of(2023, 2, 10);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(YES)
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent2(PartyBuilder.builder().individual().build())
            .evidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(Integer.parseInt(selected)), false))
            .documentWitnessSummaryRes(
                createWitnessDocs(witnessName, createdDate, witnessDate))
            .documentWitnessStatementRes(
                createWitnessDocs(witnessName, createdDate, witnessDate))
            .documentHearsayNoticeRes(createWitnessDocs(witnessName, createdDate, witnessDate))
            .documentExpertReportRes(createExpertDocs("expertName", witnessDate, "expertise", null, null, null, null))
            .documentJointStatementRes(createExpertDocs("expertsName", witnessDate, null, "expertises", null, null, null))
            .documentQuestionsRes(createExpertDocs("expertName", witnessDate, null, null, "other", "question", null))
            .documentAnswersRes(createExpertDocs("expertName", witnessDate, null, null, "other", null, "answer"))
            .documentForDisclosureRes(createEvidenceDocs(null, "typeDisclosure", witnessDate))
            .documentReferredInStatementRes(createEvidenceDocs("witness", "typeReferred", witnessDate))
            .documentEvidenceForTrialRes(createEvidenceDocs(null, "typeForTrial", witnessDate))
            .documentDisclosureListRes(createEvidenceDocs(null, null, null))
            .documentCaseSummaryRes(createEvidenceDocs(null, null, null))
            .documentSkeletonArgumentRes(createEvidenceDocs(null, null, null))
            .documentAuthoritiesRes(createEvidenceDocs(null, null, null))
            .documentCostsRes(createEvidenceDocs(null, null, null))
            .build();
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(YES)
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent2(PartyBuilder.builder().individual().build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());

        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);

        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then applicant docs should have name changed
        assertThat(updatedData.getDocumentWitnessSummaryRes().get(0).getValue()
            .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Summary of ResOneWitness 10-02-2023.pdf");
        assertThat(updatedData.getDocumentWitnessStatementRes().get(0).getValue()
            .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Statement of ResOneWitness 10-02-2023.pdf");
        assertThat(updatedData.getDocumentHearsayNoticeRes().get(0).getValue()
            .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Hearsay evidence ResOneWitness 10-02-2023.pdf");
        assertThat(updatedData.getDocumentExpertReportRes().get(0).getValue()
            .getExpertDocument().getDocumentFileName()).isEqualTo("Experts report expertName expertise 10-02-2023.pdf");
        assertThat(updatedData.getDocumentJointStatementRes().get(0).getValue()
            .getExpertDocument().getDocumentFileName()).isEqualTo("Joint report expertsName expertises 10-02-2023.pdf");
        assertThat(updatedData.getDocumentQuestionsRes().get(0).getValue()
            .getExpertDocument().getDocumentFileName()).isEqualTo("expertName other question.pdf");
        assertThat(updatedData.getDocumentAnswersRes().get(0).getValue()
            .getExpertDocument().getDocumentFileName()).isEqualTo("expertName other answer.pdf");
        assertThat(updatedData.getDocumentForDisclosureRes().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo("Document for disclosure typeDisclosure 10-02-2023.pdf");
        assertThat(updatedData.getDocumentReferredInStatementRes().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo("typeReferred referred to in the statement of witness 10-02-2023.pdf");
        assertThat(updatedData.getDocumentEvidenceForTrialRes().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo("Documentary Evidence typeForTrial 10-02-2023.pdf");
        assertThat(updatedData.getDocumentDisclosureListRes().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentCaseSummaryRes().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentSkeletonArgumentRes().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentAuthoritiesRes().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentCostsRes().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);

        String both = "2";
        if (selected.equals(both)) {
            assertThat(updatedData.getDocumentWitnessSummaryRes2().get(0).getValue()
                .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Summary of ResOneWitness 10-02-2023.pdf");
            assertThat(updatedData.getDocumentWitnessSummaryRes2().get(0).getValue()
                .getWitnessOptionDocument().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_WITNESS_SUMMARY.getCategoryId());
            assertThat(updatedData.getDocumentWitnessStatementRes2().get(0).getValue()
                .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Statement of ResOneWitness 10-02-2023.pdf");
            assertThat(updatedData.getDocumentWitnessStatementRes2().get(0).getValue()
                .getWitnessOptionDocument().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_WITNESS_STATEMENT.getCategoryId());
            assertThat(updatedData.getDocumentHearsayNoticeRes2().get(0).getValue()
                .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Hearsay evidence ResOneWitness 10-02-2023.pdf");
            assertThat(updatedData.getDocumentHearsayNoticeRes2().get(0).getValue()
                .getWitnessOptionDocument().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_WITNESS_HEARSAY.getCategoryId());
            assertThat(updatedData.getDocumentExpertReportRes2().get(0).getValue()
                .getExpertDocument().getDocumentFileName()).isEqualTo("Experts report expertName expertise 10-02-2023.pdf");
            assertThat(updatedData.getDocumentExpertReportRes2().get(0).getValue()
                .getExpertDocument().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_EXPERT_REPORT.getCategoryId());
            assertThat(updatedData.getDocumentJointStatementRes2().get(0).getValue()
                .getExpertDocument().getDocumentFileName()).isEqualTo("Joint report expertsName expertises 10-02-2023.pdf");
            assertThat(updatedData.getDocumentJointStatementRes2().get(0).getValue()
                .getExpertDocument().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_EXPERT_JOINT_STATEMENT.getCategoryId());
            assertThat(updatedData.getDocumentQuestionsRes2().get(0).getValue()
                .getExpertDocument().getDocumentFileName()).isEqualTo("expertName other question.pdf");
            assertThat(updatedData.getDocumentQuestionsRes2().get(0).getValue()
                .getExpertDocument().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_EXPERT_QUESTIONS.getCategoryId());
            assertThat(updatedData.getDocumentAnswersRes2().get(0).getValue()
                .getExpertDocument().getDocumentFileName()).isEqualTo("expertName other answer.pdf");
            assertThat(updatedData.getDocumentAnswersRes2().get(0).getValue()
                .getExpertDocument().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_EXPERT_ANSWERS.getCategoryId());
            assertThat(updatedData.getDocumentForDisclosureRes2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo("Document for disclosure typeDisclosure 10-02-2023.pdf");
            assertThat(updatedData.getDocumentForDisclosureRes2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_DISCLOSURE.getCategoryId());
            assertThat(updatedData.getDocumentReferredInStatementRes2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo("typeReferred referred to in the statement of witness 10-02-2023.pdf");
            assertThat(updatedData.getDocumentReferredInStatementRes2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_WITNESS_REFERRED.getCategoryId());
            assertThat(updatedData.getDocumentEvidenceForTrialRes2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo("Documentary Evidence typeForTrial 10-02-2023.pdf");
            assertThat(updatedData.getDocumentEvidenceForTrialRes2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE.getCategoryId());
            assertThat(updatedData.getDocumentDisclosureListRes2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentDisclosureListRes2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_DISCLOSURE_LIST.getCategoryId());
            assertThat(updatedData.getDocumentCaseSummaryRes2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentCaseSummaryRes2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_PRE_TRIAL_SUMMARY.getCategoryId());
            assertThat(updatedData.getDocumentSkeletonArgumentRes2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentSkeletonArgumentRes2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_TRIAL_SKELETON.getCategoryId());
            assertThat(updatedData.getDocumentAuthoritiesRes2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentAuthoritiesRes2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_TRIAL_SKELETON.getCategoryId());
            assertThat(updatedData.getDocumentCostsRes2().get(0).getValue()
                .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(updatedData.getDocumentCostsRes2().get(0).getValue()
                .getDocumentUpload().getCategoryID()).isEqualTo(DocumentCategory.RESPONDENT_TWO_SCHEDULE_OF_COSTS.getCategoryId());
            assertThat(updatedData.getNotificationText()).contains(NotificationWhenBothDefendant);
        }
    }

    @Test
    void should_do_naming_convention_resp2() {
        LocalDateTime createdDate = LocalDateTime.of(2022, 05, 10, 12, 13, 12);

        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        String witnessName = "ResTwoWitness";
        LocalDate witnessDate = LocalDate.of(2023, 2, 10);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .documentWitnessSummaryRes2(
                createWitnessDocs(witnessName, createdDate, witnessDate))
            .documentWitnessStatementRes2(
                createWitnessDocs(witnessName, createdDate, witnessDate))
            .documentHearsayNoticeRes2(createWitnessDocs(witnessName, createdDate, witnessDate))
            .documentExpertReportRes2(createExpertDocs("expertName", witnessDate, "expertise", null, null, null, null))
            .documentJointStatementRes2(createExpertDocs("expertsName", witnessDate, null, "expertises", null, null, null))
            .documentQuestionsRes2(createExpertDocs("expertName", witnessDate, null, null, "other", "question", null))
            .documentAnswersRes2(createExpertDocs("expertName", witnessDate, null, null, "other", null, "answer"))
            .documentForDisclosureRes2(createEvidenceDocs(null, "typeDisclosure", witnessDate))
            .documentReferredInStatementRes2(createEvidenceDocs("witness", "typeReferred", witnessDate))
            .documentEvidenceForTrialRes2(createEvidenceDocs(null, "typeForTrial", witnessDate))
            .documentDisclosureListRes2(createEvidenceDocs(null, null, null))
            .documentCaseSummaryRes2(createEvidenceDocs(null, null, null))
            .documentSkeletonArgumentRes2(createEvidenceDocs(null, null, null))
            .documentAuthoritiesRes2(createEvidenceDocs(null, null, null))
            .documentCostsRes2(createEvidenceDocs(null, null, null))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());

        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(false);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(true);

        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then applicant docs should have name changed
        assertThat(updatedData.getDocumentWitnessSummaryRes2().get(0).getValue()
            .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Summary of ResTwoWitness 10-02-2023.pdf");
        assertThat(updatedData.getDocumentWitnessStatementRes2().get(0).getValue()
            .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Witness Statement of ResTwoWitness 10-02-2023.pdf");
        assertThat(updatedData.getDocumentHearsayNoticeRes2().get(0).getValue()
            .getWitnessOptionDocument().getDocumentFileName()).isEqualTo("Hearsay evidence ResTwoWitness 10-02-2023.pdf");
        assertThat(updatedData.getDocumentExpertReportRes2().get(0).getValue()
            .getExpertDocument().getDocumentFileName()).isEqualTo("Experts report expertName expertise 10-02-2023.pdf");
        assertThat(updatedData.getDocumentJointStatementRes2().get(0).getValue()
            .getExpertDocument().getDocumentFileName()).isEqualTo("Joint report expertsName expertises 10-02-2023.pdf");
        assertThat(updatedData.getDocumentQuestionsRes2().get(0).getValue()
            .getExpertDocument().getDocumentFileName()).isEqualTo("expertName other question.pdf");
        assertThat(updatedData.getDocumentAnswersRes2().get(0).getValue()
            .getExpertDocument().getDocumentFileName()).isEqualTo("expertName other answer.pdf");
        assertThat(updatedData.getDocumentForDisclosureRes2().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo("Document for disclosure typeDisclosure 10-02-2023.pdf");
        assertThat(updatedData.getDocumentReferredInStatementRes2().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo("typeReferred referred to in the statement of witness 10-02-2023.pdf");
        assertThat(updatedData.getDocumentEvidenceForTrialRes2().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo("Documentary Evidence typeForTrial 10-02-2023.pdf");
        assertThat(updatedData.getDocumentDisclosureListRes2().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentCaseSummaryRes2().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentSkeletonArgumentRes2().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentAuthoritiesRes2().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getDocumentCostsRes2().get(0).getValue()
            .getDocumentUpload().getDocumentFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(updatedData.getNotificationText()).isEqualTo(NotificationWhenDefendantTwo);
    }

    @Test
    void shouldNotAddSameNotificationIfAlreadyAdded_notificationText() {
        // If we populate notification string with an entry, we do not want to duplicate that on further uploads of same type.
        List<String> options = List.of(EvidenceUploadHandlerBase.OPTION_DEF1,
            EvidenceUploadHandlerBase.OPTION_DEF2,
            EvidenceUploadHandlerBase.OPTION_DEF_BOTH);
        LocalDate witnessDate = LocalDate.of(2023, 2, 10);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .notificationText("Documentation that has been uploaded: \n\n Defendant 1 - Joint Statement of Experts / Single Joint Expert Report \n")
            .applicant1(PartyBuilder.builder().individual().build())
            .evidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(0), false))
            .documentJointStatementRes(createExpertDocs("expertsName", witnessDate, null, "expertises", null, null, null))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);
        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then Notificcation should not have a duplicate entry
        assertThat(updatedData.getNotificationText())
            .isEqualTo("Documentation that has been uploaded: \n\n Defendant 1 - Joint Statement of Experts / Single Joint Expert Report \n");
    }

    @Test
    void shouldNotPopulateNotificationWithOldDocument_whenNewDocumentUploadAdded() {
        // When evidence upload is retriggered we do not send a notification for old content i.e uploaded before midnight of current day
        List<String> options = List.of(EvidenceUploadHandlerBase.OPTION_DEF1,
            EvidenceUploadHandlerBase.OPTION_DEF2,
            EvidenceUploadHandlerBase.OPTION_DEF_BOTH);
        LocalDate witnessDate = LocalDate.of(2023, 2, 10);
        String witnessName = "Witness";
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .notificationText(null)
            .evidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(0), false))
            .documentWitnessStatementRes(createWitnessDocs(witnessName, LocalDateTime.now().minusDays(2), witnessDate))
            .documentWitnessSummaryRes(createWitnessDocs(witnessName, LocalDateTime.now(), witnessDate))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).willReturn(true);
        given(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).willReturn(false);

        // When handle is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then Notification should not have old entry (witness statement)
        assertThat(updatedData.getNotificationText()).isEqualTo("\nDefendant 1 - Witness summary");
    }

    @Test
    void should_compareAndCopy() {
        List<Element<Object>> before = null;
        List<Element<Object>> after = null;
        List<Element<Object>> target = null;
        assertThat(handler.compareAndCopy(before, after, target)).isNull();
        Element<Object> e1 = Element.builder().id(UUID.randomUUID()).value("1").build();
        after = List.of(e1);
        assertThat(handler.compareAndCopy(before, after, target)).hasSize(1);
        before = List.of(e1);
        assertThat(handler.compareAndCopy(before, after, target)).isEmpty();
        Element<Object> e2 = Element.builder().id(UUID.randomUUID()).value("2").build();
        after = List.of(e1, e2);
        assertThat(handler.compareAndCopy(before, after, target)).hasSize(1);
    }

    private List<Element<UploadEvidenceDocumentType>> createEvidenceDocs(String name, String type, LocalDate issuedDate) {
        Document document = Document.builder().documentBinaryUrl(
                TEST_URL)
            .documentFileName(TEST_FILE_NAME).build();
        List<Element<UploadEvidenceDocumentType>> evidenceDocs = new ArrayList<>();
        evidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
            .builder()
            .witnessOptionName(name)
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
            .expertOptionUploadDate(LocalDate.now())
            .createdDatetime(uploadedDate)
            .expertDocument(Document.builder().documentBinaryUrl(TEST_URL)
                .documentUrl(uniqueUrl)
                .documentFileName(TEST_FILE_NAME).build()).build()));

        return expertEvidenceDocs;
    }

    private List<Element<UploadEvidenceDocumentType>> getUploadEvidenceDocumentTypeDocs(LocalDateTime uploadedDate, String uniqueUrl) {
        List<Element<UploadEvidenceDocumentType>> uploadEvidenceDocs = new ArrayList<>();
        uploadEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
            .builder()
            .documentIssuedDate(LocalDate.now())
            .createdDatetime(uploadedDate)
            .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                .documentUrl(uniqueUrl)
                .documentFileName(TEST_FILE_NAME).build()).build()));

        return uploadEvidenceDocs;
    }
}

