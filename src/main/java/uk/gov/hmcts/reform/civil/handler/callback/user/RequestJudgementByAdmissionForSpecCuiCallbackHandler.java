package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentByAdmissionOnlineMapper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_JUDGEMENT_ADMISSION_SPEC;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;

@Service
@RequiredArgsConstructor
public class RequestJudgementByAdmissionForSpecCuiCallbackHandler extends CallbackHandler {

    private static final String NOT_VALID_DJ_BY_ADMISSION = "The Claim is not eligible for Request Judgment By Admission until %s.";
    private static final List<CaseEvent> EVENTS = Collections.singletonList(REQUEST_JUDGEMENT_ADMISSION_SPEC);
    private final ObjectMapper objectMapper;
    private final JudgementService judgementService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final FeatureToggleService featureToggleService;
    private final JudgmentByAdmissionOnlineMapper judgmentByAdmissionOnlineMapper;
    private final InterestCalculator interestCalculator;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::validateDefaultJudgementEligibility)
            .put(callbackKey(MID, "set-up-ccj-amount-summary"), this::buildJudgmentAmountSummaryDetails)
            .put(callbackKey(MID, "validate-amount-paid"), this::validateAmountPaid)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::updateBusinessProcessToReady)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateDefaultJudgementEligibility(CallbackParams callbackParams) {

        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        ArrayList<String> errors = new ArrayList<>();
        if (caseData.isJudgementDateNotPermitted()) {
            errors.add(format(NOT_VALID_DJ_BY_ADMISSION, caseData.setUpJudgementFormattedPermittedDate(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid())));
        } else {
            LocalDate whenWillThisAmountBePaid =
                Optional.ofNullable(caseData.getRespondToClaimAdmitPartLRspec()).map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid).orElse(
                    null);
            if (featureToggleService.isJudgmentOnlineLive()
                && whenWillThisAmountBePaid != null
                && caseData.isDateAfterToday(whenWillThisAmountBePaid)
                && caseData.isPartAdmitPayImmediatelyClaimSpec()) {
                errors.add(format(NOT_VALID_DJ_BY_ADMISSION, caseData.getFormattedJudgementPermittedDate(whenWillThisAmountBePaid)));
            }
        }

        if (judgementService.isLrPayImmediatelyPlan(caseData)) {
            caseDataBuilder.ccjJudgmentAmountShowInterest(YesOrNo.NO);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(errors.isEmpty() ? caseDataBuilder.build().toMap(objectMapper) : null)
            .build();
    }

    private CallbackResponse buildJudgmentAmountSummaryDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        updatedCaseData.ccjPaymentDetails(judgementService.buildJudgmentAmountSummaryDetails(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateAmountPaid(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = judgementService.validateAmountPaid(caseData);
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();
        if (judgementService.isLrPayImmediatelyPlan(caseData)
            && Objects.nonNull(caseData.getFixedCosts())
            && YesOrNo.NO.equals(caseData.getFixedCosts().getClaimFixedCosts())) {
            updatedCaseData.ccjPaymentDetails(judgementService.buildJudgmentAmountSummaryDetails(caseData));
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(errors.isEmpty() ? updatedCaseData.build().toMap(objectMapper) : null)
            .build();
    }

    private CallbackResponse updateBusinessProcessToReady(CallbackParams callbackParams) {
        String nextState;
        BusinessProcess businessProcess;
        CaseData data = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());
        CCJPaymentDetails ccjPaymentDetails = data.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()
            ? judgementService.buildJudgmentAmountSummaryDetails(data) :
            data.getCcjPaymentDetails();

        if (featureToggleService.isJudgmentOnlineLive()
            && (isOneVOne(data))
            && data.isPayImmediately()) {
            nextState = CaseState.All_FINAL_ORDERS_ISSUED.name();
            businessProcess = BusinessProcess.ready(JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC);
        } else {
            nextState = CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name();
            businessProcess = BusinessProcess.ready(REQUEST_JUDGEMENT_ADMISSION_SPEC);
        }

        CaseData.CaseDataBuilder caseDataBuilder = data.toBuilder()
            .businessProcess(businessProcess)
            .ccjPaymentDetails(ccjPaymentDetails);

        if (featureToggleService.isJudgmentOnlineLive()) {
            JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseDataBuilder.build());

            BigDecimal interest = interestCalculator.calculateInterest(data);

            String joSummaryObject = data.isLipvLipOneVOne() ? JudgmentsOnlineHelper.calculateRepaymentBreakdownSummaryWithoutClaimInterest(
                activeJudgment, true) : getJudgmentRepaymentSummaryObject(data, interest, activeJudgment);
            caseDataBuilder
                .activeJudgment(activeJudgment)
                .joIsLiveJudgmentExists(YesOrNo.YES)
                .joRepaymentSummaryObject(joSummaryObject)
                .joJudgementByAdmissionIssueDate(LocalDateTime.now());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .state(nextState)
            .build();
    }

    private String getJudgmentRepaymentSummaryObject(CaseData caseData, BigDecimal interest, JudgmentDetails activeJudgment) {
        return judgementService.isLrPayImmediatelyPlan(caseData)
            ? JudgmentsOnlineHelper.calculateRepaymentBreakdownSummaryWithoutClaimInterest(activeJudgment, false)
            : JudgmentsOnlineHelper.calculateRepaymentBreakdownSummary(activeJudgment, interest);
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(setUpHeader(caseData))
            .confirmationBody(setUpBody(caseData))
            .build();
    }

    private String setUpHeader(CaseData caseData) {
        String claimNumber = caseData.getLegacyCaseReference();
        return format(
            "# Judgment Submitted %n## A county court judgment(CCJ) has been submitted for case %s",
            claimNumber
        );
    }

    private String setUpBody(CaseData caseData) {
        if (CaseState.All_FINAL_ORDERS_ISSUED == caseData.getCcdState()) {
            return format(
                "<br />%n%n<a href=\"%s\" target=\"_blank\">Download county court judgment</a>"
                    + "<br><br>The defendant will be served the county court judgment<br><br>",
                format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
            );
        }
        return format(
            "<br /><h2 class=\"govuk-heading-m\"><u>What happens next</u></h2>"
                + "<br>This case will now proceed offline. Any updates will be sent by post.<br><br>"
        );
    }
}
