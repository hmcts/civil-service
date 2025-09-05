package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@Slf4j
public class SpecPaidLessAmountOrDisputesOrPartAdmissionCaseUpdater implements HandleAdmitPartOfClaimCaseUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Updating SpecPaidLessAmountOrDisputesOrPartAdmissionCase for caseId: {}", caseData.getCcdCaseReference());

        if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT == caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
                || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
                || RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()) {
            log.debug("Setting specPaidLessAmountOrDisputesOrPartAdmission to YES for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.specPaidLessAmountOrDisputesOrPartAdmission(YES);
        } else {
            log.debug("Setting specPaidLessAmountOrDisputesOrPartAdmission to NO for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.specPaidLessAmountOrDisputesOrPartAdmission(NO);
        }
    }
}
