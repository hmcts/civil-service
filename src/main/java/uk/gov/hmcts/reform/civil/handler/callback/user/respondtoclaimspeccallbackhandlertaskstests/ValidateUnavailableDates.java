package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@RequiredArgsConstructor
public class ValidateUnavailableDates implements CaseTask {

    private final UnavailableDateValidator unavailableDateValidator;

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        // UnavailableDates validation & field (model) needs to be created.
        // This will be taken care via different story,
        // because we don't have AC around this date field validation in ROC-9455
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors;
        if (SpecJourneyConstantLRSpec.SMALL_CLAIM.equals(caseData.getResponseClaimTrack())) {
            SmallClaimHearing smallClaimHearing = caseData.getRespondent1DQ().getRespondent1DQHearingSmallClaim();
            if (YES.equals(caseData.getIsRespondent2())) {
                smallClaimHearing = caseData.getRespondent2DQ().getRespondent2DQHearingSmallClaim();
            }
            errors = unavailableDateValidator.validateSmallClaimsHearing(smallClaimHearing);

        } else {
            Hearing hearingLRspec = caseData.getRespondent1DQ().getRespondent1DQHearingFastClaim();
            errors = unavailableDateValidator.validateFastClaimHearing(hearingLRspec);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
