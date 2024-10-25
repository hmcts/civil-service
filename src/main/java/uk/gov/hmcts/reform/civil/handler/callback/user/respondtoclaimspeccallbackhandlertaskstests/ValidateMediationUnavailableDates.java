package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

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
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        if ((caseData.getResp1MediationAvailability() != null
            && YES.equals(caseData.getResp1MediationAvailability().getIsMediationUnavailablityExists()))) {
            checkUnavailable(errors, caseData.getResp1MediationAvailability().getUnavailableDatesForMediation());
        } else if (caseData.getResp2MediationAvailability() != null
            && YES.equals(caseData.getResp2MediationAvailability().getIsMediationUnavailablityExists())) {
            checkUnavailable(errors, caseData.getResp2MediationAvailability().getUnavailableDatesForMediation());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}