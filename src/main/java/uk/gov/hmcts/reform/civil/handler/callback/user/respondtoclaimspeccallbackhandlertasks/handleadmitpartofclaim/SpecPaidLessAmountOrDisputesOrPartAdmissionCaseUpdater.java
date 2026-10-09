package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@Slf4j
public class SpecPaidLessAmountOrDisputesOrPartAdmissionCaseUpdater implements HandleAdmitPartOfClaimCaseUpdater {

    @Override
    public void update(CaseData caseData) {
        log.info("Updating SpecPaidLessAmountOrDisputesOrPartAdmissionCase for caseId: {}", caseData.getCcdCaseReference());

        if (matchesCurrentDefendant(caseData)) {
            log.debug("Setting specPaidLessAmountOrDisputesOrPartAdmission to YES for caseId: {}", caseData.getCcdCaseReference());
            caseData.setSpecPaidLessAmountOrDisputesOrPartAdmission(YES);
        } else {
            log.debug("Setting specPaidLessAmountOrDisputesOrPartAdmission to NO for caseId: {}", caseData.getCcdCaseReference());
            caseData.setSpecPaidLessAmountOrDisputesOrPartAdmission(NO);
        }
    }

    private boolean matchesCurrentDefendant(CaseData caseData) {
        if (caseData.isCurrentDefendantRespondent2()) {
            return RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent2ClaimResponseTypeForSpec()
                || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
                || isPaidLess(caseData.getDefenceRouteRequired2(), caseData.getRespondToClaim2(), caseData.getTotalClaimAmount());
        }

        return RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
            == caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec();
    }

    private boolean isPaidLess(String defenceRoute, RespondToClaim respondToClaim, BigDecimal totalClaimAmount) {
        if (!HAS_PAID_THE_AMOUNT_CLAIMED.equals(defenceRoute) || totalClaimAmount == null) {
            return false;
        }
        return Optional.ofNullable(respondToClaim)
            .map(RespondToClaim::getHowMuchWasPaid)
            .map(MonetaryConversions::penniesToPounds)
            .map(paid -> paid.compareTo(totalClaimAmount) < 0)
            .orElse(false);
    }
}
