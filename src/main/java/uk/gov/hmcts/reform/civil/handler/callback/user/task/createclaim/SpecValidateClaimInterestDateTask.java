package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class SpecValidateClaimInterestDateTask {

    private static final String CREATE_CLAIM_SPEC_EVENT = "CREATE_CLAIM_SPEC";

    public CallbackResponse specValidateClaimInterestDate(CaseData caseData, String eventId) {
        if (isCreateClaimSpecEvent(eventId)) {
            List<String> errors = validateInterestDate(caseData);
            return buildCallbackResponse(errors);
        }
        return buildCallbackResponse(new ArrayList<>());
    }

    private boolean isCreateClaimSpecEvent(String eventId) {
        return CREATE_CLAIM_SPEC_EVENT.equals(eventId);
    }

    private List<String> validateInterestDate(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (isFutureInterestDate(caseData.getInterestFromSpecificDate())) {
            errors.add("Correct the date. You can’t use a future date.");
        }
        return errors;
    }

    private boolean isFutureInterestDate(LocalDate interestDate) {
        return interestDate != null && interestDate.isAfter(LocalDate.now());
    }

    private CallbackResponse buildCallbackResponse(List<String> errors) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
