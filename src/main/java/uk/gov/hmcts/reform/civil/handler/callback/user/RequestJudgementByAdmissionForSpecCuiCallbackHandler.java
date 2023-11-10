package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_JUDGEMENT_ADMISSION_SPEC;

@Service
@RequiredArgsConstructor
public class RequestJudgementByAdmissionForSpecCuiCallbackHandler extends CallbackHandler {

    private static final String NOT_VALID_DJ_BY_ADMISSION = "The Claim is not eligible for Request Judgment By Admission until %s.";
    private static final List<CaseEvent> EVENTS = Collections.singletonList(REQUEST_JUDGEMENT_ADMISSION_SPEC);
    private final ObjectMapper objectMapper;
    private final JudgementService judgementService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final FeatureToggleService featureToggleService;
    private final DeadlineExtensionCalculatorService deadlineCalculatorService;

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

        final WorkingDayIndicator workingDayIndicator = null;
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        ArrayList<String> errors = new ArrayList<>();
        if (caseData.isJudgementDateNotPermitted()) {
            LocalDate extendedRespondent1ResponseDate = deadlineCalculatorService.calculateExtendedDeadline(
                caseData.getRespondent1ResponseDate().toLocalDate(),
                RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY);
            errors.add(format(NOT_VALID_DJ_BY_ADMISSION, caseData.setUpJudgementFormattedPermittedDate(extendedRespondent1ResponseDate)));
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
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(errors.isEmpty() ? caseData.toMap(objectMapper) : null)
            .build();
    }

    private CallbackResponse updateBusinessProcessToReady(CallbackParams callbackParams) {
        CaseData data = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());
        CCJPaymentDetails
            ccjPaymentDetails = data.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()
            ? judgementService.buildJudgmentAmountSummaryDetails(data) :
            data.getCcjPaymentDetails();
        CaseData.CaseDataBuilder caseDataBuilder = data.toBuilder()
            .businessProcess(BusinessProcess.ready(REQUEST_JUDGEMENT_ADMISSION_SPEC))
            .ccjPaymentDetails(ccjPaymentDetails);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name())
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(setUpHeader(caseData))
            .confirmationBody(setUpBody())
            .build();
    }

    private String setUpHeader(CaseData caseData) {
        String claimNumber = caseData.getLegacyCaseReference();
        return format(
            "# Judgment Submitted %n## A county court judgment(ccj) has been submitted for case %s",
            claimNumber
        );
    }

    private String setUpBody() {
        return format(
            "<br /><h2 class=\"govuk-heading-m\"><u>What happens next</u></h2>"
                + "<br>This case will now proceed offline. Any updates will be sent by post.<br><br>"
        );
    }
}
