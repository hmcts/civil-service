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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateClaimSpecAfterPaymentCallbackHandler extends CallbackHandler {

    // This should be created by the handler/service that receives payment info via our endpoint.
    private static final List<CaseEvent> EVENTS = singletonList(CREATE_CLAIM_SPEC_AFTER_PAYMENT);
    private final ObjectMapper objectMapper;
    private final DeadlinesCalculator deadlinesCalculator;
    private final FeatureToggleService toggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(V_1, ABOUT_TO_SUBMIT), this::changeStateToCaseIssued,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse changeStateToCaseIssued(CallbackParams callbackParams) {
        Long caseId = callbackParams.getCaseData().getCcdCaseReference();
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder dataBuilder = caseData.toBuilder();
        dataBuilder.businessProcess(BusinessProcess.ready(CREATE_CLAIM_SPEC_AFTER_PAYMENT));

        if (caseData.isLipvLipOneVOne() && toggleService.isLipVLipEnabled()) {
            if (Objects.isNull(caseData.getRespondent1ResponseDeadline())) {
                dataBuilder.respondent1ResponseDeadline(deadlinesCalculator.plus28DaysAt4pmDeadline(
                    LocalDateTime.now()));
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
                 .data(dataBuilder.build().toMap(objectMapper))
                 .build();
    }

}
