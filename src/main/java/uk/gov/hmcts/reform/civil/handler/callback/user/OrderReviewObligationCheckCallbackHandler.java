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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ObligationData;
import uk.gov.hmcts.reform.civil.model.ObligationWAFlag;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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
                updateObligationWAFlag(obligationWAFlagBuilder, data);
                obligationWAFlagBuilder.currentDate(currentDate.format(formatter));
                obligationWAFlagBuilder.obligationReason(data.getObligationReason().name());
                obligationWAFlagBuilder.obligationReasonDisplayValue(data.getObligationReason().getDisplayedValue());
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

    private void updateObligationWAFlag(ObligationWAFlag.ObligationWAFlagBuilder obligationWAFlagBuilder, ObligationData data) {
        switch (data.getObligationReason()) {
            case UNLESS_ORDER -> obligationWAFlagBuilder.unlessOrder(YesOrNo.YES);
            case STAY_A_CASE -> obligationWAFlagBuilder.stayACase(YesOrNo.YES);
            case LIFT_A_STAY -> obligationWAFlagBuilder.liftAStay(YesOrNo.YES);
            case DISMISS_CASE -> obligationWAFlagBuilder.dismissCase(YesOrNo.YES);
            case PRE_TRIAL_CHECKLIST -> obligationWAFlagBuilder.preTrialChecklist(YesOrNo.YES);
            case GENERAL_ORDER -> obligationWAFlagBuilder.generalOrder(YesOrNo.YES);
            case RESERVE_JUDGMENT -> obligationWAFlagBuilder.reserveJudgment(YesOrNo.YES);
            case OTHER -> obligationWAFlagBuilder.other(YesOrNo.YES);
            default -> {
                log.info("Obligation reason {} not found", data.getObligationReason());
            }
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
