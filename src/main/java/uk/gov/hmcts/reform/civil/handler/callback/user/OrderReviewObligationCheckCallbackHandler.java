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
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
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
        CaseData caseData = callbackParams.getCaseData().toBuilder()
            .build();

        LocalDate currentDate = LocalDate.now();
        caseData.getStoredObligationData().stream()
            .map(Element::getValue)
            .filter(data -> !data.getObligationDate().isAfter(currentDate) && YesOrNo.NO.equals(data.getObligationWATaskRaised()))
            .forEach(data -> {
                data.setObligationWATaskRaised(YesOrNo.YES);
                // TODO: add logic here to trigger WA tasks for obligations that are due
            });

        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        List<Element<ObligationData>> storedObligationDataList = caseData.getStoredObligationData();

        updatedCaseData.storedObligationData(storedObligationDataList);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(mapper))
            .build();

    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
