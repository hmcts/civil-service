package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.DiscontinueClaimHelper;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISCONTINUE_CLAIM_CLAIMANT;

@Service
@RequiredArgsConstructor
public class DiscontinueClaimClaimantCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(DISCONTINUE_CLAIM_CLAIMANT);

    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::validateState
        );
    }

    private CallbackResponse validateState(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        final var caseDataBuilder = caseData.toBuilder();
        List<String> errors = new ArrayList<>();

        DiscontinueClaimHelper.checkState(caseData, errors);
        if (errors.isEmpty()) {
            List<String> claimantNames = new ArrayList<>();
            claimantNames.add(caseData.getApplicant1().getPartyName());
            claimantNames.add(caseData.getApplicant2().getPartyName());
            caseDataBuilder.claimantWhoIsDiscontinuing(DynamicList.fromList(claimantNames));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
