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
        log.info("Executing unavailable dates validation for caseId: {}", callbackParams.getCaseData().getCcdCaseReference());

        CaseData caseData = callbackParams.getCaseData();
        List<String> errors;

        if (isSmallClaim(caseData)) {
            log.debug("CaseId {}: Validating small claim hearing", caseData.getCcdCaseReference());
            SmallClaimHearing smallClaimHearing = getSmallClaimHearing(caseData);
            errors = unavailableDateValidator.validateSmallClaimsHearing(smallClaimHearing);
        } else {
            log.debug("CaseId {}: Validating fast claim hearing", caseData.getCcdCaseReference());
            Hearing hearingLRspec = caseData.getRespondent1DQ().getRespondent1DQHearingFastClaim();
            errors = unavailableDateValidator.validateFastClaimHearing(hearingLRspec);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
    }

    private boolean isSmallClaim(CaseData caseData) {
        log.debug("CaseId {}: Checking if the case is a small claim", caseData.getCcdCaseReference());
        return SpecJourneyConstantLRSpec.SMALL_CLAIM.equals(caseData.getResponseClaimTrack());
    }

    private SmallClaimHearing getSmallClaimHearing(CaseData caseData) {
        log.info("Retrieving small claim hearing for caseId: {}", caseData.getCcdCaseReference());

        if (YES.equals(caseData.getIsRespondent2())) {
            log.debug("CaseId {}: Returning Respondent 2 small claim hearing", caseData.getCcdCaseReference());
            return caseData.getRespondent2DQ().getRespondent2DQHearingSmallClaim();
        }

        return caseData.getRespondent1DQ().getRespondent1DQHearingSmallClaim();
    }
}
