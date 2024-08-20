package uk.gov.hmcts.reform.civil.handler.callback.user.task.createClaimSpecCallbackHanderTask;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SpecValidateClaimInterestDateTask {

    public CallbackResponse specValidateClaimInterestDate(CaseData caseData, String eventId) {
        if (eventId.equals("CREATE_CLAIM_SPEC")) {
            List<String> errors = new ArrayList<>();
            if (caseData.getInterestFromSpecificDate() != null && caseData.getInterestFromSpecificDate().isAfter(
                LocalDate.now())) {
                errors.add("Correct the date. You can’t use a future date.");
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }
}
