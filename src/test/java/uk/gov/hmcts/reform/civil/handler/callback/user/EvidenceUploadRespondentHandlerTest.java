package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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

    @MockitoBean
    private FeatureToggleService featureToggleService;

    private final LocalDateTime time = LocalDateTime.now();

    @Autowired
    private RespondentSetOptionsTask respondentSetOptionsTask;

    @MockitoBean
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

    @Test
    void givenAboutToStart_assignCaseProgAllocatedTrackUnSpec() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setCaseAccessCategory(UNSPEC_CLAIM);
        caseData.setNotificationText("NULLED");
        caseData.setAllocatedTrack(FAST_CLAIM);
        caseData.setResponseClaimTrack(null);
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        caseData.setNotificationText(null);
        caseData.setAllocatedTrack(null);
        caseData.setResponseClaimTrack("SMALL_CLAIM");
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setNotificationText("existing notification");
        caseData.setClaimType(null);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(12500));
        caseData.setAddRespondent2(YES);
        caseData.setRespondent1(PartyBuilder.builder().individual().build());
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setRespondent2SameLegalRepresentative(YES);
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setRespondent2SameLegalRepresentative(YES);
        caseData.setEvidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(Integer.parseInt(selected)), false));
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setRespondent2SameLegalRepresentative(NO);
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setAddRespondent2(YES);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setRespondent2SameLegalRepresentative(NO);
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
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
        CaseData caseData1 = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData1.setWitnessSelectionEvidenceRes(witnessList);
        CaseData caseData2 = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData2.setWitnessSelectionEvidenceSmallClaimRes(witnessList);
        return Stream.of(
            arguments(caseData1),
            arguments(caseData2)
        );
    }

    static Stream<Arguments> expertOptionsSelected() {
        List<EvidenceUploadExpert> expertList = new ArrayList<>();
        expertList.add(EXPERT_REPORT);
        expertList.add(JOINT_STATEMENT);
        CaseData caseData1 = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData1.setExpertSelectionEvidenceRes(expertList);
        CaseData caseData2 = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData2.setExpertSelectionEvidenceSmallClaimRes(expertList);
        return Stream.of(
            arguments(caseData1),
            arguments(caseData2)
        );
    }

    static Stream<Arguments> trialOptionsSelected() {
        List<EvidenceUploadTrial> trialList = new ArrayList<>();
        trialList.add(AUTHORITIES);
        trialList.add(DOCUMENTARY);
        trialList.add(COSTS);
        CaseData caseData1 = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData1.setTrialSelectionEvidenceRes(trialList);
        CaseData caseData2 = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData2.setTrialSelectionEvidenceSmallClaimRes(trialList);
        return Stream.of(
            arguments(caseData1),
            arguments(caseData2)
        );
    }

    static Stream<Arguments> optionsNotSelected() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        return Stream.of(
            arguments(caseData)
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
        "setDocumentIssuedDate,setDocumentForDisclosureRes",
        "setDocumentIssuedDate,setDocumentReferredInStatementRes",
        "setDocumentIssuedDate,setDocumentEvidenceForTrialRes",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePastOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceDocumentType(), dateField, time
            .toLocalDate().minusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(NO);
        caseData.setCaseTypeFlag("do_not_show");

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(NO);
        caseDataBefore.setCaseTypeFlag("do_not_show");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setDocumentIssuedDate,setDocumentForDisclosureRes",
        "setDocumentIssuedDate,setDocumentReferredInStatementRes",
        "setDocumentIssuedDate,setDocumentEvidenceForTrialRes",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePresentOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceDocumentType(), dateField, time
            .toLocalDate())));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(NO);
        caseData.setCaseTypeFlag("do_not_show");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(NO);
        caseDataBefore.setCaseTypeFlag("do_not_show");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setDocumentIssuedDate,setDocumentForDisclosureRes, Invalid date: \"Documents for disclosure\""
            + " date entered must not be in the future (1).",
        "setDocumentIssuedDate,setDocumentForDisclosureRes, Invalid date: \"Documents for disclosure\""
            + " date entered must not be in the future (1).",
        "setDocumentIssuedDate,setDocumentReferredInStatementRes, Invalid date: \"Documents referred to in the statement\""
            + " date entered must not be in the future (5).",
    })
    void shouldReturnError_whenDocumentTypeUploadDateFutureOneRespondent(String dateField, String collectionField,
                                                                         String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceDocumentType(), dateField, time
            .toLocalDate().plusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(NO);
        caseData.setCaseTypeFlag("do_not_show");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(NO);
        caseDataBefore.setCaseTypeFlag("do_not_show");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "setExpertOptionUploadDate,setDocumentExpertReportRes",
        "setExpertOptionUploadDate,setDocumentJointStatementRes",
        "setExpertOptionUploadDate,setDocumentQuestionsRes",
        "setExpertOptionUploadDate,setDocumentAnswersRes",
    })
    void shouldNotReturnError_whenExpert2OptionUploadDatePastOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceExpert(), dateField, time
            .toLocalDate().minusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(NO);
        caseData.setCaseTypeFlag("do_not_show");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(NO);
        caseDataBefore.setCaseTypeFlag("do_not_show");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setExpertOptionUploadDate,setDocumentExpertReportRes",
        "setExpertOptionUploadDate,setDocumentJointStatementRes",
        "setExpertOptionUploadDate,setDocumentQuestionsRes",
        "setExpertOptionUploadDate,setDocumentAnswersRes",
    })
    void shouldNotReturnError_whenExpertOptionUploadDatePresentOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceExpert(), dateField, time
            .toLocalDate())));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(NO);
        caseData.setCaseTypeFlag("do_not_show");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(NO);
        caseDataBefore.setCaseTypeFlag("do_not_show");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setExpertOptionUploadDate,setDocumentExpertReportRes,Invalid date: \"Expert's report\""
            + " date entered must not be in the future (6).",
        "setExpertOptionUploadDate,setDocumentJointStatementRes,Invalid date: \"Joint statement of experts\" "
            + "date entered must not be in the future (7).",
        "setExpertOptionUploadDate,setDocumentQuestionsRes,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (8).",
        "setExpertOptionUploadDate,setDocumentAnswersRes,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (9).",
    })
    void shouldReturnError_whenExpertOptionUploadDateFutureOneRespondent(String dateField, String collectionField,
                                                                         String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceExpert(), dateField, time
            .toLocalDate().plusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(NO);
        caseData.setCaseTypeFlag("do_not_show");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(NO);
        caseDataBefore.setCaseTypeFlag("do_not_show");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "setWitnessOptionUploadDate,setDocumentWitnessStatementRes,Invalid date: \"witness statement\" "
            + "date entered must not be in the future (2).",
        "setWitnessOptionUploadDate,setDocumentWitnessSummaryRes,Invalid date: \"witness summary\" "
            + "date entered must not be in the future (3).",
        "setWitnessOptionUploadDate,setDocumentHearsayNoticeRes,Invalid date: \"Notice of the intention to rely on"
            + " hearsay evidence\" date entered must not be in the future (4).",
    })
    void shouldReturnError_whenWitnessOptionUploadDateInFutureOneRespondent(String dateField, String collectionField,
                                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceWitness(), dateField,
            time.toLocalDate().plusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(NO);
        caseData.setCaseTypeFlag("do_not_show");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(NO);
        caseDataBefore.setCaseTypeFlag("do_not_show");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "setWitnessOptionUploadDate,setDocumentWitnessStatementRes",
        "setWitnessOptionUploadDate,setDocumentHearsayNoticeRes",
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePresentOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceWitness(), dateField,
            time.toLocalDate())));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(NO);
        caseData.setCaseTypeFlag("do_not_show");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(NO);
        caseDataBefore.setCaseTypeFlag("do_not_show");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setWitnessOptionUploadDate,setDocumentWitnessStatementRes",
        "setWitnessOptionUploadDate,setDocumentHearsayNoticeRes",
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePastOneRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceWitness(), dateField,
            time.toLocalDate().minusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(NO);
        caseData.setCaseTypeFlag("do_not_show");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(NO);
        caseDataBefore.setCaseTypeFlag("do_not_show");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setWitnessOptionUploadDate,setDocumentWitnessStatementRes,Invalid date: \"witness statement\" date entered must not be in the future (2).",
        "setWitnessOptionUploadDate,setDocumentHearsayNoticeRes,Invalid date: \"Notice of the intention to rely on hearsay evidence\" " +
            "date entered must not be in the future (4).",
        "setWitnessOptionUploadDate,setDocumentWitnessStatementRes,Invalid date: \"witness statement\" date entered must not be in the future (2).",
        "setWitnessOptionUploadDate,setDocumentHearsayNoticeRes,Invalid date: \"Notice of the intention to rely on hearsay evidence\" " +
            "date entered must not be in the future (4)."
    })
    void shouldReturnError_whenOneDateIsInFutureForWitnessStatementsOneRespondent(String dateField, String collectionField, String errorMessage) {
        //documentUploadWitness1 represents a collection so can have multiple dates entered at any time,
        // these dates should all be in the past, otherwise an error will be populated

        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceWitness(), dateField, time.toLocalDate().minusWeeks(1))));
        date.add(1, element(invoke(new UploadEvidenceWitness(), dateField, time.toLocalDate().plusWeeks(1))));
        //dates above represent valid past dates, date below represents invalid future date.
        date.add(2, element(invoke(new UploadEvidenceWitness(), dateField, time.toLocalDate().minusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(NO);
        caseData.setCaseTypeFlag("do_not_show");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(NO);
        caseDataBefore.setCaseTypeFlag("do_not_show");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(errorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "setDocumentIssuedDate,setDocumentForDisclosureRes2",
        "setDocumentIssuedDate,setDocumentReferredInStatementRes2",
        "setDocumentIssuedDate,setDocumentEvidenceForTrialRes2",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePastTwoRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceDocumentType(), dateField, time
            .toLocalDate().minusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setRespondent2SameLegalRepresentative(NO);
        caseData.setCaseTypeFlag("RespondentTwoFields");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(YES);
        caseDataBefore.setRespondent2(PartyBuilder.builder().individual().build());
        caseDataBefore.setRespondent2SameLegalRepresentative(NO);
        caseDataBefore.setCaseTypeFlag("RespondentTwoFields");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setDocumentIssuedDate,setDocumentForDisclosureRes2",
        "setDocumentIssuedDate,setDocumentReferredInStatementRes2",
        "setDocumentIssuedDate,setDocumentEvidenceForTrialRes2",
    })
    void shouldNotReturnError_whenDocumentTypeUploadDatePresentTwoRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceDocumentType(), dateField, time
            .toLocalDate())));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setRespondent2SameLegalRepresentative(NO);
        caseData.setCaseTypeFlag("RespondentTwoFields");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(YES);
        caseDataBefore.setRespondent2(PartyBuilder.builder().individual().build());
        caseDataBefore.setRespondent2SameLegalRepresentative(NO);
        caseDataBefore.setCaseTypeFlag("RespondentTwoFields");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setDocumentIssuedDate,setDocumentForDisclosureRes2, Invalid date: \"Documents for disclosure\""
            + " date entered must not be in the future (1).",
        "setDocumentIssuedDate,setDocumentReferredInStatementRes2, Invalid date: \"Documents referred to in the statement\""
            + " date entered must not be in the future (5).",
        "setDocumentIssuedDate,setDocumentEvidenceForTrialRes2, Invalid date: \"Documentary evidence for trial\""
            + " date entered must not be in the future (10).",
    })
    void shouldReturnError_whenDocumentTypeUploadDateFutureTwoRespondent(String dateField, String collectionField,
                                                                         String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceDocumentType>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceDocumentType(), dateField, time
            .toLocalDate().plusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setRespondent2SameLegalRepresentative(NO);
        caseData.setCaseTypeFlag("RespondentTwoFields");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(YES);
        caseDataBefore.setRespondent2(PartyBuilder.builder().individual().build());
        caseDataBefore.setRespondent2SameLegalRepresentative(NO);
        caseDataBefore.setCaseTypeFlag("RespondentTwoFields");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "setExpertOptionUploadDate,setDocumentExpertReportRes2",
        "setExpertOptionUploadDate,setDocumentJointStatementRes2",
        "setExpertOptionUploadDate,setDocumentQuestionsRes2",
        "setExpertOptionUploadDate,setDocumentAnswersRes2"
    })
    void shouldNotReturnError_whenExpert2OptionUploadDatePastTwoRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceExpert(), dateField, time
            .toLocalDate().minusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setRespondent2SameLegalRepresentative(NO);
        caseData.setCaseTypeFlag("RespondentTwoFields");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(YES);
        caseDataBefore.setRespondent2(PartyBuilder.builder().individual().build());
        caseDataBefore.setRespondent2SameLegalRepresentative(NO);
        caseDataBefore.setCaseTypeFlag("RespondentTwoFields");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setExpertOptionUploadDate,setDocumentExpertReportRes2",
        "setExpertOptionUploadDate,setDocumentJointStatementRes2",
        "setExpertOptionUploadDate,setDocumentQuestionsRes2",
        "setExpertOptionUploadDate,setDocumentAnswersRes2"
    })
    void shouldNotReturnError_whenExpertOptionUploadDatePresentTwoRespondent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceExpert(), dateField, time
            .toLocalDate())));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setRespondent2SameLegalRepresentative(NO);
        caseData.setCaseTypeFlag("RespondentTwoFields");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(YES);
        caseDataBefore.setRespondent2(PartyBuilder.builder().individual().build());
        caseDataBefore.setRespondent2SameLegalRepresentative(NO);
        caseDataBefore.setCaseTypeFlag("RespondentTwoFields");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setExpertOptionUploadDate,setDocumentExpertReportRes2,Invalid date: \"Expert's report\""
            + " date entered must not be in the future (6).",
        "setExpertOptionUploadDate,setDocumentJointStatementRes2,Invalid date: \"Joint statement of experts\" "
            + "date entered must not be in the future (7).",
        "setExpertOptionUploadDate,setDocumentQuestionsRes2,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (8).",
        "setExpertOptionUploadDate,setDocumentAnswersRes2,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (9)."
    })
    void shouldReturnError_whenExpertOptionUploadDateFuture(String dateField, String collectionField,
                                                            String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceExpert(), dateField, time
            .toLocalDate().plusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setRespondent2SameLegalRepresentative(NO);
        caseData.setCaseTypeFlag("RespondentTwoFields");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(YES);
        caseDataBefore.setRespondent2(PartyBuilder.builder().individual().build());
        caseDataBefore.setRespondent2SameLegalRepresentative(NO);
        caseDataBefore.setCaseTypeFlag("RespondentTwoFields");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "setWitnessOptionUploadDate,setDocumentWitnessStatementRes2,Invalid date: \"witness statement\" "
            + "date entered must not be in the future (2).",
        "setWitnessOptionUploadDate,setDocumentHearsayNoticeRes2,Invalid date: \"Notice of the intention to rely on"
            + " hearsay evidence\" date entered must not be in the future (4).",
        "setWitnessOptionUploadDate,setDocumentWitnessStatementRes2,Invalid date: \"witness statement\" "
            + "date entered must not be in the future (2).",
        "setWitnessOptionUploadDate,setDocumentHearsayNoticeRes2,Invalid date: \"Notice of the intention to rely on"
            + " hearsay evidence\" date entered must not be in the future (4)."
    })
    void shouldReturnError_whenWitnessOptionUploadDateInFuture(String dateField, String collectionField,
                                                               String expectedErrorMessage) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceWitness(), dateField,
            time.toLocalDate().plusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setRespondent2SameLegalRepresentative(NO);
        caseData.setCaseTypeFlag("RespondentTwoFields");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(YES);
        caseDataBefore.setRespondent2(PartyBuilder.builder().individual().build());
        caseDataBefore.setRespondent2SameLegalRepresentative(NO);
        caseDataBefore.setCaseTypeFlag("RespondentTwoFields");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "setWitnessOptionUploadDate,setDocumentWitnessStatementRes2",
        "setWitnessOptionUploadDate,setDocumentHearsayNoticeRes2"
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePresent(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceWitness(), dateField,
            time.toLocalDate())));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setRespondent2SameLegalRepresentative(NO);
        caseData.setCaseTypeFlag("RespondentTwoFields");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(YES);
        caseDataBefore.setRespondent2(PartyBuilder.builder().individual().build());
        caseDataBefore.setRespondent2SameLegalRepresentative(NO);
        caseDataBefore.setCaseTypeFlag("RespondentTwoFields");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setWitnessOptionUploadDate,setDocumentWitnessStatementRes2",
        "setWitnessOptionUploadDate,setDocumentHearsayNoticeRes2"
    })
    void shouldNotReturnError_whenWitnessOptionUploadDatePast(String dateField, String collectionField) {
        // Given
        List<Element<UploadEvidenceWitness>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceWitness(), dateField,
            time.toLocalDate().minusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setRespondent2SameLegalRepresentative(NO);
        caseData.setCaseTypeFlag("RespondentTwoFields");

        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(YES);
        caseDataBefore.setRespondent2(PartyBuilder.builder().individual().build());
        caseDataBefore.setRespondent2SameLegalRepresentative(NO);
        caseDataBefore.setCaseTypeFlag("RespondentTwoFields");
        CallbackParams params = callbackParamsOf(caseData, caseDataBefore, MID, null,
                                                 PAGE_ID, Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "setExpertOptionUploadDate,setDocumentExpertReportRes2,Invalid date: \"Expert's report\""
            + " date entered must not be in the future (6).",
        "setExpertOptionUploadDate,setDocumentAnswersRes2,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (9).",
        "setExpertOptionUploadDate,setDocumentQuestionsRes2,Invalid date: \"Questions for other party's expert "
            + "or joint experts\" expert statement date entered must not be in the future (8).",
        "setExpertOptionUploadDate,setDocumentAnswersRes2,Invalid date: \"Answers to questions asked by the other party\" "
            + "date entered must not be in the future (9)."
    })
    void shouldReturnError_whenOneDateIsInFutureForExpertStatementsTwoRespondents(String dateField, String collectionField, String errorMessage) {
        //documentUploadWitness1 represents a collection so can have multiple dates entered at any time,
        // these dates should all be in the past, otherwise an error will be populated

        // Given
        List<Element<UploadEvidenceExpert>> date = new ArrayList<>();
        date.add(0, element(invoke(new UploadEvidenceExpert(), dateField, time.toLocalDate().minusWeeks(1))));
        date.add(1, element(invoke(new UploadEvidenceExpert(), dateField, time.toLocalDate().plusWeeks(1))));
        //dates above represent valid past dates, date below represents invalid future date.
        date.add(2, element(invoke(new UploadEvidenceExpert(), dateField, time.toLocalDate().minusWeeks(1))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseData, collectionField, date);
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setRespondent2SameLegalRepresentative(NO);
        caseData.setCaseTypeFlag("RespondentTwoFields");
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        invoke(caseDataBefore, collectionField, List.of());
        caseDataBefore.setAddRespondent2(YES);
        caseDataBefore.setRespondent2(PartyBuilder.builder().individual().build());
        caseDataBefore.setRespondent2SameLegalRepresentative(NO);
        caseDataBefore.setCaseTypeFlag("RespondentTwoFields");
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
        UploadEvidenceDocumentType documentUpload = new UploadEvidenceDocumentType();
        documentUpload.setDocumentIssuedDate(LocalDate.of(2022, 2, 10));
        documentUpload.setCreatedDatetime(LocalDateTime.of(2022, 05, 10, 12, 13, 12));
        documentUpload.setDocumentUpload(testDocument);
        List<Element<UploadEvidenceDocumentType>> documentList = new ArrayList<>();
        Element<UploadEvidenceDocumentType> element = new Element<>();
        element.setValue(documentUpload);
        documentList.add(element);
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddRespondent2(YES);
        caseData.setDocumentForDisclosureRes2(documentList);
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
        UploadEvidenceDocumentType documentUpload = new UploadEvidenceDocumentType();
        documentUpload.setDocumentIssuedDate(LocalDate.of(2022, 2, 10));
        documentUpload.setCreatedDatetime(LocalDateTime.of(2022, 05, 10, 12, 13, 12));
        documentUpload.setDocumentUpload(testDocument);
        List<Element<UploadEvidenceDocumentType>> documentList = new ArrayList<>();
        Element<UploadEvidenceDocumentType> element = new Element<>();
        element.setValue(documentUpload);
        documentList.add(element);
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddRespondent2(YES);
        caseData.setDocumentForDisclosureRes2(documentList);
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
        UploadEvidenceExpert documentUpload = new UploadEvidenceExpert();
        documentUpload.setExpertDocument(testDocument);
        documentUpload.setCreatedDatetime(LocalDateTime.of(2022, 05, 10, 12, 13, 12));
        List<Element<UploadEvidenceExpert>> documentList = new ArrayList<>();
        Element<UploadEvidenceExpert> element = new Element<>();
        element.setValue(documentUpload);
        documentList.add(element);
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddRespondent2(NO);
        caseData.setDocumentAnswersRes(documentList);
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
        UploadEvidenceExpert documentUpload = new UploadEvidenceExpert();
        documentUpload.setExpertDocument(testDocument);
        documentUpload.setCreatedDatetime(LocalDateTime.of(2022, 05, 10, 12, 13, 12));
        List<Element<UploadEvidenceExpert>> documentList = new ArrayList<>();
        Element<UploadEvidenceExpert> element = new Element<>();
        element.setValue(documentUpload);
        documentList.add(element);
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddRespondent2(NO);
        caseData.setDocumentQuestionsRes(documentList);
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddRespondent2(YES);
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddRespondent2(NO);
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddRespondent2(YES);
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder().uid("uid").build());
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
        assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
            .confirmationHeader(header)
            .confirmationBody(body)
            .build());
    }

    private <T, A> T invoke(T target, String method, A argument) {
        Class<?> parameterType = argument instanceof List ? List.class : argument.getClass();
        ReflectionUtils.invokeMethod(ReflectionUtils.getRequiredMethod(target.getClass(),
            method, parameterType), target, argument);
        return target;
    }

    @Test
    void shouldAddRespondentEvidenceDocWhenBundleCreatedDateIsBeforeEvidenceUploaded_onNewCreatedBundle() {
        List<Element<UploadEvidenceDocumentType>> respondentDocsUploadedAfterBundle = new ArrayList<>();
        UploadEvidenceDocumentType document = new UploadEvidenceDocumentType();
        document.setCreatedDatetime(LocalDateTime.now(ZoneId.of("Europe/London")));
        respondentDocsUploadedAfterBundle.add(ElementUtils.element(document));
        // Given caseBundles with bundle created date is before witness and expert doc created date
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        // populate respondentDocsUploadedAfterBundle with a default built element after a new bundle, it only
        // contains createdDatetime and no document, so will be removed from final list
        caseData.setRespondentDocsUploadedAfterBundle(respondentDocsUploadedAfterBundle);
        // added after trial bundle, so will  be added
        caseData.setDocumentWitnessStatementRes(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url22"));
        caseData.setDocumentExpertReportRes(getExpertDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url44"));
        caseData.setCaseBundles(prepareCaseBundles(LocalDateTime.of(2022, 04, 10, 12, 12, 12)));
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        // populate respondentDocsUploadedAfterBundle with an existing upload
        caseData.setRespondentDocsUploadedAfterBundle(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url66"));
        // added before trial bundle, so will not be added
        caseData.setDocumentQuestionsRes(getExpertDocs(LocalDateTime.of(2022, 03, 10, 12, 13, 12), "url11"));
        caseData.setDocumentWitnessSummaryRes(getWitnessDocs(LocalDateTime.of(2022, 03, 10, 12, 13, 12), "url22"));
        caseData.setDocumentQuestionsRes2(getExpertDocs(LocalDateTime.of(2022, 03, 10, 12, 13, 12), "url33"));
        // added after trial bundle, so will  be added
        caseData.setDocumentExpertReportRes(getExpertDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url44"));
        caseData.setDocumentWitnessSummaryRes2(getWitnessDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url55"));
        caseData.setDocumentWitnessStatementRes(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url22"));
        caseData.setDocumentDisclosureListRes2(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url77"));
        // this should not be added, as it has duplicate URL, indicating it already is in list, and should be skipped.
        caseData.setDocumentDisclosureListRes(getUploadEvidenceDocumentTypeDocs(LocalDateTime.of(2022, 06, 10, 12, 13, 12), "url66"));
        caseData.setCaseBundles(prepareCaseBundles(LocalDateTime.of(2022, 04, 10, 12, 12, 12)));
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setDocumentDisclosureListRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"));
        caseData.setDocumentDisclosureListRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url2"));
        caseData.setDocumentForDisclosureRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url3"));
        caseData.setDocumentForDisclosureRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url4"));
        caseData.setDocumentReferredInStatementRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url5"));
        caseData.setDocumentReferredInStatementRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url6"));
        caseData.setDocumentCaseSummaryRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url7"));
        caseData.setDocumentCaseSummaryRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url8"));
        caseData.setDocumentSkeletonArgumentRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url9"));
        caseData.setDocumentSkeletonArgumentRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url10"));
        caseData.setDocumentAuthoritiesRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url11"));
        caseData.setDocumentAuthoritiesRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url12"));
        caseData.setDocumentCostsRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url13"));
        caseData.setDocumentCostsRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url14"));
        caseData.setDocumentHearsayNoticeRes(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url15"));
        caseData.setDocumentHearsayNoticeRes2(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url16"));
        caseData.setDocumentWitnessSummaryRes(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url17"));
        caseData.setDocumentWitnessSummaryRes2(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url18"));
        caseData.setDocumentQuestionsRes(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url19"));
        caseData.setDocumentQuestionsRes2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url20"));
        caseData.setDocumentEvidenceForTrialRes(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url21"));
        caseData.setDocumentEvidenceForTrialRes2(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url22"));
        caseData.setDocumentAnswersRes(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url23"));
        caseData.setDocumentAnswersRes2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url24"));
        caseData.setDocumentJointStatementRes(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url25"));
        caseData.setDocumentJointStatementRes2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url26"));
        caseData.setDocumentExpertReportRes(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url27"));
        caseData.setDocumentExpertReportRes2(getExpertDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url28"));
        caseData.setDocumentWitnessStatementRes(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url29"));
        caseData.setDocumentWitnessStatementRes2(getWitnessDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url30"));
        caseData.setCaseBundles(prepareCaseBundles(LocalDateTime.of(2022, 05, 15, 12, 12, 12)));

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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setRespondentDocsUploadedAfterBundle(getUploadEvidenceDocumentTypeDocs(
                LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"));
        caseData.setDocumentWitnessSummaryRes(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url22"));
        caseData.setCaseBundles(prepareCaseBundles(LocalDateTime.of(2022, 05, 10, 12, 12, 12)));

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
        caseBundles.add(new IdValue<>("1", new Bundle().setId("1")
            .setTitle("Trial Bundle")
            .setStitchStatus(Optional.of("NEW")).setDescription("Trial Bundle")
            .setCreatedOn(Optional.of(LocalDateTime.of(2022, 05, 15, 12, 12, 12)))));
        caseBundles.add(new IdValue<>("1", new Bundle().setId("1")
            .setTitle("Trial Bundle")));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setDocumentQuestionsRes(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"));
        caseData.setDocumentWitnessSummaryRes(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"));
        caseData.setCaseBundles(caseBundles);

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
        caseBundles.add(new IdValue<>("1", new Bundle().setId("1")
            .setTitle("Trial Bundle")
            .setStitchStatus(Optional.of("NEW")).setDescription("Trial Bundle")
            .setCreatedOn(Optional.ofNullable(null))));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setDocumentQuestionsRes(getExpertDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"));
        caseData.setDocumentWitnessSummaryRes(getWitnessDocs(LocalDateTime.of(2022, 05, 10, 12, 13, 12), "url1"));
        caseData.setCaseBundles(caseBundles);

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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddRespondent2(YES);
        caseData.setRespondent1(PartyBuilder.builder().individual().build());
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setEvidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(Integer.parseInt(selected)), false));
        caseData.setDocumentWitnessSummaryRes(
                createWitnessDocs(witnessName, createdDate, witnessDate));
        caseData.setDocumentWitnessStatementRes(
                createWitnessDocs(witnessName, createdDate, witnessDate));
        caseData.setDocumentHearsayNoticeRes(createWitnessDocs(witnessName, createdDate, witnessDate));
        caseData.setDocumentExpertReportRes(createExpertDocs("expertName", witnessDate, "expertise", null, null, null, null));
        caseData.setDocumentJointStatementRes(createExpertDocs("expertsName", witnessDate, null, "expertises", null, null, null));
        caseData.setDocumentQuestionsRes(createExpertDocs("expertName", witnessDate, null, null, "other", "question", null));
        caseData.setDocumentAnswersRes(createExpertDocs("expertName", witnessDate, null, null, "other", null, "answer"));
        caseData.setDocumentForDisclosureRes(createEvidenceDocs(null, "typeDisclosure", witnessDate));
        caseData.setDocumentReferredInStatementRes(createEvidenceDocs("witness", "typeReferred", witnessDate));
        caseData.setDocumentEvidenceForTrialRes(createEvidenceDocs(null, "typeForTrial", witnessDate));
        caseData.setDocumentDisclosureListRes(createEvidenceDocs(null, null, null));
        caseData.setDocumentCaseSummaryRes(createEvidenceDocs(null, null, null));
        caseData.setDocumentSkeletonArgumentRes(createEvidenceDocs(null, null, null));
        caseData.setDocumentAuthoritiesRes(createEvidenceDocs(null, null, null));
        caseData.setDocumentCostsRes(createEvidenceDocs(null, null, null));
        CaseData caseDataBefore = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseDataBefore.setAddRespondent2(YES);
        caseDataBefore.setRespondent1(PartyBuilder.builder().individual().build());
        caseDataBefore.setRespondent2(PartyBuilder.builder().individual().build());
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setDocumentWitnessSummaryRes2(createWitnessDocs(witnessName, createdDate, witnessDate));
        caseData.setDocumentWitnessStatementRes2(createWitnessDocs(witnessName, createdDate, witnessDate));
        caseData.setDocumentHearsayNoticeRes2(createWitnessDocs(witnessName, createdDate, witnessDate));
        caseData.setDocumentExpertReportRes2(createExpertDocs("expertName", witnessDate, "expertise", null, null, null, null));
        caseData.setDocumentJointStatementRes2(createExpertDocs("expertsName", witnessDate, null, "expertises", null, null, null));
        caseData.setDocumentQuestionsRes2(createExpertDocs("expertName", witnessDate, null, null, "other", "question", null));
        caseData.setDocumentAnswersRes2(createExpertDocs("expertName", witnessDate, null, null, "other", null, "answer"));
        caseData.setDocumentForDisclosureRes2(createEvidenceDocs(null, "typeDisclosure", witnessDate));
        caseData.setDocumentReferredInStatementRes2(createEvidenceDocs("witness", "typeReferred", witnessDate));
        caseData.setDocumentEvidenceForTrialRes2(createEvidenceDocs(null, "typeForTrial", witnessDate));
        caseData.setDocumentDisclosureListRes2(createEvidenceDocs(null, null, null));
        caseData.setDocumentCaseSummaryRes2(createEvidenceDocs(null, null, null));
        caseData.setDocumentSkeletonArgumentRes2(createEvidenceDocs(null, null, null));
        caseData.setDocumentAuthoritiesRes2(createEvidenceDocs(null, null, null));
        caseData.setDocumentCostsRes2(createEvidenceDocs(null, null, null));
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setNotificationText("Documentation that has been uploaded: \n\n Defendant 1 - Joint Statement of Experts / Single Joint Expert Report \n");
        caseData.setApplicant1(PartyBuilder.builder().individual().build());
        caseData.setEvidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(0), false));
        caseData.setDocumentJointStatementRes(createExpertDocs("expertsName", witnessDate, null, "expertises", null, null, null));
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setNotificationText(null);
        caseData.setEvidenceUploadOptions(DynamicList.fromList(options, Object::toString, options.get(0), false));
        caseData.setDocumentWitnessStatementRes(createWitnessDocs(witnessName, LocalDateTime.now().minusDays(2), witnessDate));
        caseData.setDocumentWitnessSummaryRes(createWitnessDocs(witnessName, LocalDateTime.now(), witnessDate));
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

    private Document createDocument(String url, String fileName) {
        return new Document(null, url, fileName, null, null, null);
    }

    private Document createDocumentWithUrl(String url, String uniqueUrl, String fileName) {
        return new Document(uniqueUrl, url, fileName, null, null, null);
    }

    @Test
    void should_compareAndCopy() {
        List<Element<Object>> before = null;
        List<Element<Object>> after = null;
        List<Element<Object>> target = null;
        assertThat(handler.compareAndCopy(before, after, target)).isNull();
        Element<Object> e1 = new Element<>();
        e1.setId(UUID.randomUUID());
        e1.setValue("1");
        after = List.of(e1);
        assertThat(handler.compareAndCopy(before, after, target)).hasSize(1);
        before = List.of(e1);
        assertThat(handler.compareAndCopy(before, after, target)).isEmpty();
        Element<Object> e2 = new Element<>();
        e2.setId(UUID.randomUUID());
        e2.setValue("2");
        after = List.of(e1, e2);
        assertThat(handler.compareAndCopy(before, after, target)).hasSize(1);
    }

    private List<Element<UploadEvidenceDocumentType>> createEvidenceDocs(String name, String type, LocalDate issuedDate) {
        Document document = createDocument(TEST_URL, TEST_FILE_NAME);
        UploadEvidenceDocumentType uploadEvidence = new UploadEvidenceDocumentType();
        uploadEvidence.setWitnessOptionName(name);
        uploadEvidence.setTypeOfDocument(type);
        uploadEvidence.setDocumentIssuedDate(issuedDate);
        uploadEvidence.setDocumentUpload(document);
        List<Element<UploadEvidenceDocumentType>> evidenceDocs = new ArrayList<>();
        evidenceDocs.add(ElementUtils.element(uploadEvidence));
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
        UploadEvidenceExpert uploadEvidence = new UploadEvidenceExpert();
        uploadEvidence.setExpertDocument(document);
        uploadEvidence.setExpertOptionName(expertName);
        uploadEvidence.setExpertOptionExpertise(expertise);
        uploadEvidence.setExpertOptionExpertises(expertises);
        uploadEvidence.setExpertOptionOtherParty(otherParty);
        uploadEvidence.setExpertDocumentQuestion(question);
        uploadEvidence.setExpertDocumentAnswer(answer);
        uploadEvidence.setExpertOptionUploadDate(dateUpload);
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(uploadEvidence));
        return expertEvidenceDocs;
    }

    private List<Element<UploadEvidenceWitness>> createWitnessDocs(String witnessName,
                                                                   LocalDateTime createdDate,
                                                                   LocalDate dateMade) {
        Document document = createDocument(TEST_URL, TEST_FILE_NAME);
        UploadEvidenceWitness uploadEvidence = new UploadEvidenceWitness();
        uploadEvidence.setWitnessOptionDocument(document);
        uploadEvidence.setWitnessOptionName(witnessName);
        uploadEvidence.setCreatedDatetime(createdDate);
        uploadEvidence.setWitnessOptionUploadDate(dateMade);
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessEvidenceDocs.add(ElementUtils.element(uploadEvidence));
        return witnessEvidenceDocs;
    }

    private List<IdValue<Bundle>> prepareCaseBundles(LocalDateTime bundleCreatedDate) {
        List<IdValue<Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", new Bundle().setId("1")
            .setTitle("Trial Bundle")
            .setStitchStatus(Optional.of("NEW")).setDescription("Trial Bundle")
            .setCreatedOn(Optional.of(bundleCreatedDate))));
        return caseBundles;
    }

    private List<Element<UploadEvidenceWitness>> getWitnessDocs(LocalDateTime uploadedDate, String uniqueUrl) {
        Document document = createDocumentWithUrl(TEST_URL, uniqueUrl, TEST_FILE_NAME);
        UploadEvidenceWitness uploadEvidence = new UploadEvidenceWitness();
        uploadEvidence.setWitnessOptionDocument(document);
        uploadEvidence.setWitnessOptionName("FirstName LastName");
        uploadEvidence.setCreatedDatetime(uploadedDate);
        uploadEvidence.setWitnessOptionUploadDate(LocalDate.of(2023, 2, 10));
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessEvidenceDocs.add(ElementUtils.element(uploadEvidence));
        return witnessEvidenceDocs;
    }

    private List<Element<UploadEvidenceExpert>> getExpertDocs(LocalDateTime uploadedDate, String uniqueUrl) {
        Document document = createDocumentWithUrl(TEST_URL, uniqueUrl, TEST_FILE_NAME);
        UploadEvidenceExpert uploadEvidence = new UploadEvidenceExpert();
        uploadEvidence.setExpertOptionUploadDate(LocalDate.now());
        uploadEvidence.setCreatedDatetime(uploadedDate);
        uploadEvidence.setExpertDocument(document);
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(uploadEvidence));
        return expertEvidenceDocs;
    }

    private List<Element<UploadEvidenceDocumentType>> getUploadEvidenceDocumentTypeDocs(LocalDateTime uploadedDate, String uniqueUrl) {
        Document document = createDocumentWithUrl(TEST_URL, uniqueUrl, TEST_FILE_NAME);
        UploadEvidenceDocumentType uploadEvidence = new UploadEvidenceDocumentType();
        uploadEvidence.setDocumentIssuedDate(LocalDate.now());
        uploadEvidence.setCreatedDatetime(uploadedDate);
        uploadEvidence.setDocumentUpload(document);
        List<Element<UploadEvidenceDocumentType>> uploadEvidenceDocs = new ArrayList<>();
        uploadEvidenceDocs.add(ElementUtils.element(uploadEvidence));
        return uploadEvidenceDocs;
    }
}

