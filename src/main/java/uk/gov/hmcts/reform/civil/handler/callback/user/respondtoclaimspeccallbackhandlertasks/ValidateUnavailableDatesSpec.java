package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ValidateUnavailableDatesSpec implements CaseTask {

    private final UnavailableDateValidator unavailableDateValidator;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors;

        if (isSmallClaim(caseData)) {
            SmallClaimHearing smallClaimHearing = getSmallClaimHearing(caseData);
            errors = unavailableDateValidator.validateSmallClaimsHearing(smallClaimHearing);
        } else {
            Hearing hearingLRspec = caseData.getRespondent1DQ().getRespondent1DQHearingFastClaim();
            errors = unavailableDateValidator.validateFastClaimHearing(hearingLRspec);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private boolean isSmallClaim(CaseData caseData) {
        return SpecJourneyConstantLRSpec.SMALL_CLAIM.equals(caseData.getResponseClaimTrack());
    }

    private SmallClaimHearing getSmallClaimHearing(CaseData caseData) {
        if (YES.equals(caseData.getIsRespondent2())) {
            return caseData.getRespondent2DQ().getRespondent2DQHearingSmallClaim();
        }
        return caseData.getRespondent1DQ().getRespondent1DQHearingSmallClaim();
    }
}
