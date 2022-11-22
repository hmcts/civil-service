package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.civil.repositories.SpecReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_LIP_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateClaimLipCallBackHandler extends CallbackHandler {

    private final DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    private final SpecReferenceNumberRepository specReferenceNumberRepository;
    private final Time time;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::lipClaimInitialState)
            .put(callbackKey(V_1, ABOUT_TO_START), this::lipClaimInitialState)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitClaim)
            .put(callbackKey(V_1, ABOUT_TO_SUBMIT), this::submitClaim)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(
            CaseEvent.CREATE_LIP_CLAIM
        );
    }

    private CallbackResponse lipClaimInitialState(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.superClaimType(SPEC_CLAIM);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder caseDataBuilder = callbackParams.getCaseData().toBuilder();
        caseDataBuilder.respondent1PinToPostLRspec(defendantPinToPostLRspecService.buildDefendantPinToPost());
        caseDataBuilder.submittedDate(time.now());
        if (null != callbackParams.getRequest().getEventId()) {
            caseDataBuilder.legacyCaseReference(specReferenceNumberRepository.getSpecReferenceNumber());
            caseDataBuilder.businessProcess(BusinessProcess.ready(CREATE_LIP_CLAIM));
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

}
