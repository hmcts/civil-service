package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ObligationReason;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ObligationWAFlag;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ORDER_REVIEW_OBLIGATION_CHECK;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderReviewObligationCheckCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(ORDER_REVIEW_OBLIGATION_CHECK);
    private final ObjectMapper mapper;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        Callback callback = featureToggleService.isCaseEventsEnabled()
            ? this::orderReviewObligationCheck
            : this::emptyCallbackResponse;
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), callback);
    }

    private CallbackResponse orderReviewObligationCheck(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

        caseData.setObligationWAFlag(null);

        caseData.getStoredObligationData().stream()
            .map(Element::getValue)
            .filter(data -> !data.getObligationDate().isAfter(currentDate) && YesOrNo.NO.equals(data.getObligationWATaskRaised()))
            .findFirst()
            .ifPresent(data -> {
                ObligationWAFlag.ObligationWAFlagBuilder obligationWAFlagBuilder = ObligationWAFlag.builder();
                boolean isLiftAStay = ObligationReason.LIFT_A_STAY.equals(data.getObligationReason());
                boolean isDismissCase = ObligationReason.DISMISS_CASE.equals(data.getObligationReason());
                boolean isStayACase = ObligationReason.STAY_A_CASE.equals(data.getObligationReason());
                boolean isCaseDismissed = CaseState.CASE_DISMISSED.equals(caseData.getCcdState());
                boolean isCaseStayed = CaseState.CASE_STAYED.equals(caseData.getCcdState());
                String manageStayOption = caseData.getManageStayOption();

                if ((!isLiftAStay && !isDismissCase && !isStayACase) || (isLiftAStay && isNull(manageStayOption))
                    || (isDismissCase && !isCaseDismissed) || (isStayACase && !isCaseStayed)) {
                    obligationWAFlagBuilder.currentDate(currentDate.format(formatter))
                        .obligationReason(data.getObligationReason().name())
                        .obligationReasonDisplayValue(data.getObligationReason().getDisplayedValue());
                }

                data.setObligationWATaskRaised(YesOrNo.YES);
                caseData.setObligationWAFlag(obligationWAFlagBuilder.build());
            });

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toBuilder()
                      .storedObligationData(caseData.getStoredObligationData())
                      .obligationWAFlag(caseData.getObligationWAFlag())
                      .build().toMap(mapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
