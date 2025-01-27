package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.integrationtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.RespondToClaimSpecCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        RespondToClaimSpecCallbackHandler.class,
        ExitSurveyConfiguration.class,
        ExitSurveyContentService.class,
        JacksonAutoConfiguration.class,
        ValidationAutoConfiguration.class,
        DateOfBirthValidator.class,
        UnavailableDateValidator.class,
        CaseDetailsConverter.class,
        LocationReferenceDataService.class,
        CourtLocationUtils.class,
        SimpleStateFlowEngine.class,
        SimpleStateFlowBuilder.class,
        AssignCategoryId.class,
        FrcDocumentsUtils.class,
        RespondToClaimSpecCallbackHandlerTestConfig.class
})
class RespondToClaimSpecCallbackHandlerDefendAllOfClaimTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondToClaimSpecCallbackHandler handler;

    @MockBean
    private PaymentDateValidator validator;

    @MockBean
    private PostcodeValidator postcodeValidator;

    @MockBean
    private FeatureToggleService toggleService;

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @MockBean
    private SimpleStateFlowBuilder simpleStateFlowBuilder;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private DQResponseDocumentUtils dqResponseDocumentUtils;

    @MockBean
    private CaseFlagsInitialiser caseFlagsInitialiser;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private DeadlineExtensionCalculatorService deadlineExtensionCalculatorService;

    @MockBean
    private Time time;

    @BeforeEach
    void setUp() {
        when(validator.validate(any())).thenReturn(List.of());
    }

    private void assertResponse(AboutToStartOrSubmitCallbackResponse response, boolean expectErrors, boolean expectData) {
        assertThat(response).isNotNull();
        if (expectErrors) {
            assertThat(response.getErrors()).isNotNull();
        } else {
            assertThat(response.getErrors()).isNull();
        }
        if (expectData) {
            assertThat(response.getData()).isNotNull();
        } else {
            assertThat(response.getData()).isNull();
        }
    }

    private void assertCommonData(AboutToStartOrSubmitCallbackResponse response, String track) {
        assertThat(response.getData()).containsEntry("responseClaimTrack", track);
        assertThat(response.getData()).containsEntry("specDisputesOrPartAdmission", "No");
        assertThat(response.getData()).containsEntry("specPaidLessAmountOrDisputesOrPartAdmission", "No");
    }

    @Test
    void testNotSpecDefendantResponse() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = callbackParamsOf(caseData, MID, "track");
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertResponse(response, false, true);
        assertThat(response.getData()).isNotNull();
    }

    @Test
    void testSpecDefendantResponseValidationError() {
        when(validator.validate(any())).thenReturn(List.of("Validation error"));
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceFastTrack().build();
        CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertResponse(response, true, false);
        assertEquals(1, response.getErrors().size());
        assertEquals("Validation error", response.getErrors().get(0));
    }

    @Test
    void testSpecDefendantResponseFastTrack() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceFastTrack().build();
        CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertResponse(response, false, true);
        assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
        assertThat(response.getData().get("respondent1ClaimResponsePaymentAdmissionForSpec")).isNotNull();
    }

    @Test
    void testSpecDefendantResponseFastTrackOneVTwoLegalRep() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(NO)
                .isRespondent2(YES)
                .build()
                .toBuilder()
                .showConditionFlags(EnumSet.of(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1))
                .build();
        CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertResponse(response, false, true);
        assertCommonData(response, AllocatedTrack.FAST_CLAIM.name());
    }

    @Test
    void testSpecDefendantResponseFastTrackOneVTwoSameLegalRep() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(YES)
                .isRespondent2(YES)
                .build()
                .toBuilder()
                .showConditionFlags(EnumSet.of(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2))
                .build();
        CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertResponse(response, false, true);
        assertCommonData(response, AllocatedTrack.FAST_CLAIM.name());
    }

    @Test
    void testSpecDefendantResponseFastTrackTwoVOne() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .addApplicant2()
                .applicant2(PartyBuilder.builder().individual().build())
                .build()
                .toBuilder()
                .defendantSingleResponseToBothClaimants(YES)
                .respondent1ClaimResponseTestForSpec(FULL_ADMISSION)
                .build();
        CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertResponse(response, false, true);
        assertCommonData(response, AllocatedTrack.FAST_CLAIM.name());
    }

    @Test
    void testSpecDefendantResponseFastTrackDefendantPaid() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceFastTrack().build();
        RespondToClaim respondToClaim = RespondToClaim.builder()
                .howMuchWasPaid(caseData.getTotalClaimAmount().multiply(BigDecimal.valueOf(100)))
                .build();
        caseData = caseData.toBuilder()
                .defenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED)
                .respondToClaim(respondToClaim)
                .build();
        CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertResponse(response, false, true);
        assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
        assertThat(response.getData().get("respondent1ClaimResponsePaymentAdmissionForSpec"))
                .isEqualTo(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT.name());
    }

    @Test
    void testSpecDefendantResponseFastTrackDefendantPaidLessThanClaimed() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceFastTrack().build();
        RespondToClaim respondToClaim = RespondToClaim.builder()
                .howMuchWasPaid(caseData.getTotalClaimAmount().multiply(BigDecimal.valueOf(50)))
                .build();
        caseData = caseData.toBuilder()
                .defenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED)
                .respondToClaim(respondToClaim)
                .build();
        CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertResponse(response, false, true);
        assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
        assertThat(response.getData().get("respondent1ClaimResponsePaymentAdmissionForSpec"))
                .isEqualTo(RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT.name());
    }
}
