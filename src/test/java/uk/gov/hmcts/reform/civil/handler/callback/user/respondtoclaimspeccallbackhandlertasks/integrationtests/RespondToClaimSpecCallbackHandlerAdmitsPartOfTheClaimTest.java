package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.integrationtests;

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
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.RespondToClaimSpecCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
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
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
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
class RespondToClaimSpecCallbackHandlerAdmitsPartOfTheClaimTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondToClaimSpecCallbackHandler handler;

    @MockBean
    private PaymentDateValidator validator;

    @MockBean
    private UnavailableDateValidator dateValidator;

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

    private CallbackParams createParams(CaseData caseData, String callback) {
        return callbackParamsOf(caseData, MID, callback, "DEFENDANT_RESPONSE_SPEC");
    }

    private AboutToStartOrSubmitCallbackResponse handleCallback(CallbackParams params) {
        return (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
    }

    private void assertResponseWithErrors(AboutToStartOrSubmitCallbackResponse response, List<String> expectedErrors) {
        assertThat(response).isNotNull();
        assertThat(response.getData()).isNull();
        assertThat(response.getErrors()).isNotNull();
        assertEquals(expectedErrors.size(), response.getErrors().size());
        assertEquals(expectedErrors, response.getErrors());
    }

    private void assertResponseNoErrors(AboutToStartOrSubmitCallbackResponse response) {
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isNull();
        assertThat(response.getData()).isNotNull();
    }

    @Test
    public void testSpecDefendantResponseAdmitPartOfClaimValidationError() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentAdmitPartOfClaimFastTrack()
                .build();
        CallbackParams params = createParams(caseData, "specHandleAdmitPartClaim");
        when(validator.validate(any())).thenReturn(List.of("Validation error"));

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);

        assertResponseWithErrors(response, List.of("Validation error"));
    }

    @Test
    public void testSpecDefendantResponseAdmitPartOfClaimFastTrack() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentAdmitPartOfClaimFastTrack()
                .build();
        CallbackParams params = createParams(caseData, "specHandleAdmitPartClaim");

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);

        assertResponseNoErrors(response);
        assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
    }

    @Test
    public void testSpecDefendantResponseAdmitPartOfClaimFastTrackStillOwes() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentAdmitPartOfClaimFastTrack()
                .build();
        BigDecimal admittedAmount = caseData.getTotalClaimAmount().multiply(BigDecimal.valueOf(50));
        caseData = caseData.toBuilder()
                .respondToAdmittedClaimOwingAmount(admittedAmount)
                .build();
        CallbackParams params = createParams(caseData, "specHandleAdmitPartClaim");

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);

        assertResponseNoErrors(response);
        assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
        BigDecimal owingAmount = new BigDecimal(response.getData().get("respondToAdmittedClaimOwingAmount").toString());
        BigDecimal owingAmountPounds = new BigDecimal(response.getData().get("respondToAdmittedClaimOwingAmountPounds").toString())
                .multiply(BigDecimal.valueOf(100));
        assertEquals(0, owingAmount.compareTo(owingAmountPounds));
    }

    @Test
    public void testSpecDefendantResponseAdmitPartOfClaimFastTrackRespondent2() {
        CaseData caseData = CaseData.builder()
                .caseAccessCategory(SPEC_CLAIM)
                .ccdCaseReference(354L)
                .totalClaimAmount(new BigDecimal(100000))
                .respondent1(PartyBuilder.builder().individual().build())
                .isRespondent1(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .isRespondent2(YES)
                .defenceAdmitPartEmploymentType2Required(YES)
                .specDefenceAdmitted2Required(YES)
                .specDefenceAdmittedRequired(YES)
                .showConditionFlags(EnumSet.of(
                        DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1,
                        DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2
                ))
                .build();
        CallbackParams params = createParams(caseData, "specHandleAdmitPartClaim");

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);

        assertResponseNoErrors(response);
        assertThat(response.getData()).containsEntry("responseClaimTrack", AllocatedTrack.MULTI_CLAIM.name());
    }

    @Test
    public void shouldSetIntermediateAllocatedTrack_whenInvoked() {
        when(toggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .totalClaimAmount(BigDecimal.valueOf(100000))
                .build();
        CallbackParams params = createParams(caseData, "track");

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);

        assertResponseNoErrors(response);
        assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.INTERMEDIATE_CLAIM.name());
    }

    @Test
    public void shouldSetMultiAllocatedTrack_whenInvoked() {
        when(toggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .totalClaimAmount(BigDecimal.valueOf(100001))
                .build();
        CallbackParams params = createParams(caseData, "track");

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);

        assertResponseNoErrors(response);
        assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.MULTI_CLAIM.name());
    }

    @Test
    public void testValidateLengthOfUnemploymentWithError() {
        CaseData caseData = CaseDataBuilder.builder().generateYearsAndMonthsIncorrectInput().build();
        CallbackParams params = createParams(caseData, "validate-length-of-unemployment");

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);

        assertResponseWithErrors(response, List.of("Length of time unemployed must be a whole number, for example, 10."));
    }

    @Test
    public void testValidateRespondentPaymentDate() {
        CaseData caseData = CaseDataBuilder.builder().generatePaymentDateForAdmitPartResponse().build();
        CallbackParams params = createParams(caseData, "validate-payment-date");
        when(validator.validate(any())).thenReturn(List.of("Validation error"));

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);

        assertResponseWithErrors(response, List.of("Validation error"));
    }

    @Test
    public void testValidateRepaymentDate() {
        CaseData caseData = CaseDataBuilder.builder().generateRepaymentDateForAdmitPartResponse().build();
        CallbackParams params = createParams(caseData, "validate-repayment-plan");
        when(dateValidator.validateFuturePaymentDate(any())).thenReturn(List.of("Validation error"));

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);

        assertResponseWithErrors(response, List.of("Validation error"));
    }

    @Test
    public void testValidateDefendant2RepaymentDate() {
        CaseData caseData = CaseDataBuilder.builder().generateDefendant2RepaymentDateForAdmitPartResponse().build();
        CallbackParams params = createParams(caseData, "validate-repayment-plan-2");
        when(dateValidator.validateFuturePaymentDate(any())).thenReturn(List.of("Validation error"));

        AboutToStartOrSubmitCallbackResponse response = handleCallback(params);

        assertResponseWithErrors(response, List.of("Validation error"));
    }
}
