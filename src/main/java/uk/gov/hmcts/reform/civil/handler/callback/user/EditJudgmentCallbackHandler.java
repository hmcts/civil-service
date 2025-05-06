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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.EditJudgmentOnlineMapper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EDIT_JUDGMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_JUDGMENT_VARIED_DETERMINATION_OF_MEANS;

@Service
@RequiredArgsConstructor
public class EditJudgmentCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(EDIT_JUDGMENT);
    protected final ObjectMapper objectMapper;
    private final EditJudgmentOnlineMapper editJudgmentOnlineMapper;
    private final InterestCalculator interestCalculator;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::populateFromActiveJudgment)
            .put(callbackKey(MID, "validateDates"), this::validateDates)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveJudgmentDetails)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse populateFromActiveJudgment(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        JudgmentDetails activeJudgment = caseData.getActiveJudgment();
        if (JudgmentType.DEFAULT_JUDGMENT.equals(caseData.getActiveJudgment().getType())
            || JudgmentType.JUDGMENT_BY_ADMISSION.equals(caseData.getActiveJudgment().getType())) {
            // populate data from Default Judgment Or JBA
            caseData.setJoOrderMadeDate(activeJudgment.getIssueDate());
            caseData.setJoPaymentPlan(activeJudgment.getPaymentPlan());
            caseData.setJoInstalmentDetails(activeJudgment.getInstalmentDetails());
            caseData.setJoJudgmentRecordReason(null);
            caseData.setJoAmountOrdered(activeJudgment.getOrderedAmount());
            caseData.setJoAmountCostOrdered(activeJudgment.getCosts());
            caseData.setJoIssuedDate(activeJudgment.getIssueDate());
            caseData.setJoShowRegisteredWithRTLOption(YesOrNo.NO);
        } else {
            if (activeJudgment.getIsRegisterWithRTL() == YesOrNo.NO) {
                caseData.setJoShowRegisteredWithRTLOption(YesOrNo.YES);
            } else {
                caseData.setJoShowRegisteredWithRTLOption(YesOrNo.NO);
            }
        }

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        List<String> errors = JudgmentsOnlineHelper.validateMidCallbackData(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Judgment edited")
            .confirmationBody("The judgment has been edited")
            .build();
    }

    private CallbackResponse saveJudgmentDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        List<String> errors = new ArrayList<>();

        if (caseData.getJoIsRegisteredWithRTL() == YesOrNo.YES) {
            caseDataBuilder.joIssuedDate(caseData.getJoOrderMadeDate());
        }
        if (caseData.getActiveJudgment() != null) {
            JudgmentDetails activeJudgment = editJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);
            caseDataBuilder.activeJudgment(activeJudgment);
            BigDecimal interest = interestCalculator.calculateInterest(caseData);
            caseDataBuilder.joRepaymentSummaryObject(JudgmentsOnlineHelper.calculateRepaymentBreakdownSummary(activeJudgment, interest));
        } else {
            errors.add("There is no active judgment to edit");
        }

        if (caseData.getJoJudgmentRecordReason() == JudgmentRecordedReason.DETERMINATION_OF_MEANS) {
            caseDataBuilder.businessProcess(BusinessProcess.ready(NOTIFY_JUDGMENT_VARIED_DETERMINATION_OF_MEANS));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
