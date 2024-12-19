package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ObligationData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CONFIRM_ORDER_REVIEW;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CONFIRM_ORDER_REVIEW_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.enums.CourtStaffNextSteps.STILL_TASKS;

@Service
@RequiredArgsConstructor
public class ConfirmOrderReviewCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CONFIRM_ORDER_REVIEW);

    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;
    private static final String HEADER_CONFIRMATION = "# The order review has been completed";
    private static final String BODY_CONFIRMATION_NO_OBLIGATION = "&nbsp;";
    private static final String BODY_CONFIRMATION_OBLIGATION = "### What happens next \n\n" +
        "A new task will be generated on the review date.";

    private static final String TASKS_LEFT_ERROR_1 = "Order review not completed.";
    private static final String TASKS_LEFT_ERROR_2 = "You must complete the tasks in the order before you can submit your order review.";
    private static final String TASKS_LEFT_ERROR_3 = "Once you have completed the task you can submit your order review by clicking on the link on your task list.";
    private static final String OBLIGATION_DATE_ERROR = "The obligation date must be in the future";

    @Override
    protected Map<String, Callback> callbacks() {

        if (featureToggleService.isCaseEventsEnabled()) {
            return Map.of(
                callbackKey(ABOUT_TO_START), this::cleanObligationData,
                callbackKey(MID, "validate-obligation-date"), this:: validateObligationDate,
                callbackKey(MID, "validate-tasks-left"), this:: validateTasksLeft,
                callbackKey(ABOUT_TO_SUBMIT), this::confirmOrderReview,
                callbackKey(SUBMITTED), this::fillConfirmationScreen
            );
        } else {
            return Map.of(
                callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
                callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse,
                callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
            );
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse cleanObligationData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        caseDataBuilder
            .obligationDatePresent(null)
            .courtStaffNextSteps(null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateObligationDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        var obligationList = caseData.getObligationData();

        List<String> errors =
            obligationList.stream()
                .filter(element -> element != null && element.getValue() != null)
                .map(Element::getValue)
                .filter(data -> data.getObligationDate() == null || !data.getObligationDate().isAfter(
                    LocalDate.now()))
                .map(data -> OBLIGATION_DATE_ERROR).collect(Collectors.toList());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateTasksLeft(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        ArrayList<String> errors = new ArrayList<>();

        if (STILL_TASKS.equals(caseData.getCourtStaffNextSteps())) {
            errors.add(format(TASKS_LEFT_ERROR_1));
            errors.add(format(TASKS_LEFT_ERROR_2));
            errors.add(format(TASKS_LEFT_ERROR_3));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse confirmOrderReview(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        if (YesOrNo.YES.equals(caseData.getObligationDatePresent())) {
            updatedCaseData.businessProcess(BusinessProcess.ready(CONFIRM_ORDER_REVIEW));
        } else if (YesOrNo.YES.equals(caseData.getIsFinalOrder())) {
            updatedCaseData.businessProcess(BusinessProcess.ready(CONFIRM_ORDER_REVIEW_FINAL_ORDER));
        }

        if (nonNull(caseData.getObligationData())) {

            List<Element<ObligationData>> storedObligationData = Optional.ofNullable(caseData.getStoredObligationData())
                .orElse(Collections.emptyList());

            List<Element<ObligationData>> combinedData = new ArrayList<>();
            combinedData.addAll(storedObligationData);
            combinedData.addAll(caseData.getObligationData());

            updatedCaseData.obligationData(null)
                .storedObligationData(combinedData);
        }

        if (YesOrNo.YES.equals(caseData.getIsFinalOrder())) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedCaseData.build().toMap(objectMapper))
                .state(CaseState.All_FINAL_ORDERS_ISSUED.toString())
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse fillConfirmationScreen(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(HEADER_CONFIRMATION)
            .confirmationBody(YesOrNo.YES.equals(caseData.getObligationDatePresent())
                                  ? BODY_CONFIRMATION_OBLIGATION
                                  : BODY_CONFIRMATION_NO_OBLIGATION)
            .build();
    }
}
