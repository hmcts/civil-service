package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.MediationUnavailableDatesUtils.checkUnavailable;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateMediationUnavailableDates implements CaseTask {

    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing ValidateMediationUnavailableDates with callbackParams: {}", callbackParams);
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (caseData.getResp1MediationAvailability() != null
            && YES.equals(caseData.getResp1MediationAvailability().getIsMediationUnavailablityExists())) {
            log.info("Respondent 1 Mediation Unavailability exists. Checking unavailable dates.");
            checkUnavailable(errors, caseData.getResp1MediationAvailability().getUnavailableDatesForMediation());
        } else if (caseData.getResp2MediationAvailability() != null
            && YES.equals(caseData.getResp2MediationAvailability().getIsMediationUnavailablityExists())) {
            log.info("Respondent 2 Mediation Unavailability exists. Checking unavailable dates.");
            checkUnavailable(errors, caseData.getResp2MediationAvailability().getUnavailableDatesForMediation());
        } else {
            log.info("No Mediation Unavailability exists for Respondent 1 or Respondent 2.");
        }

        log.info("Validation errors: {}", errors);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
