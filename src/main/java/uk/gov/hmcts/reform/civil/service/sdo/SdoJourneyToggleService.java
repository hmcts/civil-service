package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
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

/**
 * Concentrates the various CARM / bilingual toggle mutations so the pre-population services
 * can focus on core business logic.
 */
@Service
@RequiredArgsConstructor
public class SdoJourneyToggleService {

    static final String SMALL_CLAIMS_MEDIATION_TEXT = "If you failed to attend a mediation appointment, then the judge "
        + "at the hearing may impose a sanction. This could require you to pay costs, or could result in your claim "
        + "or defence being dismissed. You should deliver to every other party, and to the court, your explanation "
        + "for non-attendance, with any supporting documents, at least 14 days before the hearing. Any other party "
        + "who wishes to comment on the failure to attend the mediation appointment should deliver their comments, "
        + "with any supporting documents, to all parties and to the court at least 14 days before the hearing.";

    private final SdoFeatureToggleService sdoFeatureToggleService;

    public void applyJourneyFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.showCarmFields(sdoFeatureToggleService.isCarmEnabled(caseData) ? YesOrNo.YES : YesOrNo.NO);

        if (sdoFeatureToggleService.isWelshJourneyEnabled(caseData)) {
            updatedData.bilingualHint(YesOrNo.YES);
        }
    }

    public void applySmallClaimsChecklistToggle(CaseData caseData,
                                                CaseData.CaseDataBuilder<?, ?> updatedData,
                                                List<OrderDetailsPagesSectionsToggle> checkList) {
        if (sdoFeatureToggleService.isCarmEnabled(caseData)) {
            updatedData.smallClaimsMediationSectionToggle(checkList);
        }
    }

    public void applyR2SmallClaimsMediation(CaseData caseData,
                                            CaseData.CaseDataBuilder<?, ?> updatedData,
                                            List<IncludeInOrderToggle> includeInOrderToggle) {
        if (sdoFeatureToggleService.isCarmEnabled(caseData)) {
            updatedData.sdoR2SmallClaimsMediationSectionToggle(includeInOrderToggle);
            updatedData.sdoR2SmallClaimsMediationSectionStatement(SdoR2SmallClaimsMediation.builder()
                                                                      .input(CARM_MEDIATION_TEXT)
                                                                      .build());
        }
    }

    public void applySmallClaimsMediationStatement(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (sdoFeatureToggleService.isCarmEnabled(caseData)) {
            updatedData.smallClaimsMediationSectionStatement(SmallClaimsMediation.builder()
                                                              .input(SMALL_CLAIMS_MEDIATION_TEXT)
                                                              .build());
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
