package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler;

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
public class ValidateUnavailableDates implements CaseTask {

    private final UnavailableDateValidator unavailableDateValidator;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Long caseId = caseData.getCcdCaseReference();
        log.info("Executing ValidateUnavailableDates for caseId: {}", caseId);
        List<String> errors;

        if (SpecJourneyConstantLRSpec.SMALL_CLAIM.equals(caseData.getResponseClaimTrack())) {
            log.info("Processing small claim track for caseId: {}", caseId);
            SmallClaimHearing smallClaimHearing = caseData.getRespondent1DQ().getRespondent1DQHearingSmallClaim();
            if (YES.equals(caseData.getIsRespondent2())) {
                log.info("Respondent 2 is involved, using Respondent 2 DQ hearing small claim for caseId: {}", caseId);
                smallClaimHearing = caseData.getRespondent2DQ().getRespondent2DQHearingSmallClaim();
            }
            errors = unavailableDateValidator.validateSmallClaimsHearing(smallClaimHearing);

        } else {
            log.info("Processing fast claim track for caseId: {}", caseId);
            Hearing hearingLRspec = caseData.getRespondent1DQ().getRespondent1DQHearingFastClaim();
            errors = unavailableDateValidator.validateFastClaimHearing(hearingLRspec);
        }

        log.info("Validation errors for caseId {}: {}", caseId, errors);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
