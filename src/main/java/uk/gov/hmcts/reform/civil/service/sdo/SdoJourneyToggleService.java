package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;

import java.util.List;

import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.CARM_MEDIATION_TEXT;

@Service
@RequiredArgsConstructor
@Slf4j
public class SdoJourneyToggleService {

    static final String SMALL_CLAIMS_MEDIATION_TEXT = "If you failed to attend a mediation appointment, then the judge "
        + "at the hearing may impose a sanction. This could require you to pay costs, or could result in your claim "
        + "or defence being dismissed. You should deliver to every other party, and to the court, your explanation "
        + "for non-attendance, with any supporting documents, at least 14 days before the hearing. Any other party "
        + "who wishes to comment on the failure to attend the mediation appointment should deliver their comments, "
        + "with any supporting documents, to all parties and to the court at least 14 days before the hearing.";

    private final SdoFeatureToggleService sdoFeatureToggleService;

    public void applyJourneyFlags(CaseData caseData) {
        caseData.setShowCarmFields(sdoFeatureToggleService.isCarmEnabled(caseData) ? YesOrNo.YES : YesOrNo.NO);

        if (sdoFeatureToggleService.isWelshJourneyEnabled(caseData)) {
            caseData.setBilingualHint(YesOrNo.YES);
        }
        log.debug("Applied journey flags for caseId {} (CARM={}, bilingualHint={})",
                  caseData.getCcdCaseReference(),
                  caseData.getShowCarmFields(),
                  caseData.getBilingualHint());
    }

    public void applySmallClaimsChecklistToggle(CaseData caseData,
                                                List<OrderDetailsPagesSectionsToggle> checkList) {
        if (sdoFeatureToggleService.isCarmEnabled(caseData)) {
            caseData.setSmallClaimsMediationSectionToggle(checkList);
        }
    }

    public void applyR2SmallClaimsMediation(CaseData caseData,
                                            List<IncludeInOrderToggle> includeInOrderToggle) {
        if (sdoFeatureToggleService.isCarmEnabled(caseData)) {
            caseData.setSdoR2SmallClaimsMediationSectionToggle(includeInOrderToggle);
            SdoR2SmallClaimsMediation mediation = new SdoR2SmallClaimsMediation();
            mediation.setInput(CARM_MEDIATION_TEXT);
            caseData.setSdoR2SmallClaimsMediationSectionStatement(mediation);
            log.debug("Applied R2 small claims mediation defaults for caseId {}", caseData.getCcdCaseReference());
        }
    }

    public void applySmallClaimsMediationStatement(CaseData caseData) {
        if (sdoFeatureToggleService.isCarmEnabled(caseData)) {
            caseData.setSmallClaimsMediationSectionStatement(new SmallClaimsMediation()
                                                                 .setInput(SMALL_CLAIMS_MEDIATION_TEXT));
            log.debug("Applied small claims mediation statement for caseId {}", caseData.getCcdCaseReference());
        }
    }

    /**
     * Determines whether EA court should be enabled for a SPEC claim, aligning the logic across
     * SDO and DJ submission/notification paths. Returns {@code null} when the case category is not SPEC.
     */
    public YesOrNo resolveEaCourtLocation(CaseData caseData, boolean allowLipvLrWithNoC) {
        if (!CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return null;
        }

        if (sdoFeatureToggleService.isWelshEnabledForMainCase()) {
            return YesOrNo.YES;
        }

        boolean isLipCase = caseData.isApplicantLiP() || caseData.isRespondent1LiP() || caseData.isRespondent2LiP();
        if (!isLipCase) {
            return YesOrNo.YES;
        }

        return isLipCaseWithProgressionEnabledAndCourtWhiteListed(caseData, allowLipvLrWithNoC) ? YesOrNo.YES : YesOrNo.NO;
    }

    private boolean isLipCaseWithProgressionEnabledAndCourtWhiteListed(CaseData caseData, boolean allowLipvLrWithNoC) {
        boolean eligibleLipConfiguration = caseData.isLipvLipOneVOne()
            || caseData.isLRvLipOneVOne()
            || (allowLipvLrWithNoC && caseData.isLipvLROneVOne() && sdoFeatureToggleService.isDefendantNoCOnlineForCase(caseData));

        if (!eligibleLipConfiguration) {
            return false;
        }

        // Mirror master behaviour: missing base location causes a failure rather than silently proceeding.
        String baseLocation = caseData.getCaseManagementLocation().getBaseLocation();

        return sdoFeatureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(
            baseLocation
        );
    }
}
