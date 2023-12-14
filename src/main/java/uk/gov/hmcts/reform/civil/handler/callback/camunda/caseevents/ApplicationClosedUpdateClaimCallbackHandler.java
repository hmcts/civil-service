package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_CLOSED_UPDATE_CLAIM;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationClosedUpdateClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(APPLICATION_CLOSED_UPDATE_CLAIM);

    private static final String APPLICATION_CLOSED_DESCRIPTION = "Application Closed";

    private final GenAppStateHelperService helper;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::triggerGeneralApplicationClosure
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse triggerGeneralApplicationClosure(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        if (!Collections.isEmpty(caseData.getGeneralApplications())) {
            try {
                caseData = helper.updateApplicationDetailsInClaim(caseData,
                        APPLICATION_CLOSED_DESCRIPTION,
                        GenAppStateHelperService.RequiredState.APPLICATION_CLOSED);
            } catch (Exception e) {
                String errorMessage = "Error occurred while updating claim with application status: " + e.getMessage();
                log.error(errorMessage);
                errors.add(errorMessage);
                return AboutToStartOrSubmitCallbackResponse.builder()
                        .errors(errors)
                        .build();
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
    }
}
