package uk.gov.hmcts.reform.civil.service.dashboardnotifications.utils;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;

@Service
public class DashboardDecisionHelper {

    private final FeatureToggleService featureToggleService;
    private final SimpleStateFlowEngine simpleStateFlowEngine;

    public DashboardDecisionHelper(FeatureToggleService featureToggleService, SimpleStateFlowEngine simpleStateFlowEngine) {
        this.featureToggleService = featureToggleService;
        this.simpleStateFlowEngine = simpleStateFlowEngine;
    }

    public boolean hasUploadDocuments(CaseData caseData) {
        return !(isNull(caseData.getRes1MediationDocumentsReferred())
            && isNull(caseData.getRes1MediationNonAttendanceDocs())
            && isNull(caseData.getApp1MediationDocumentsReferred())
            && isNull(caseData.getApp1MediationNonAttendanceDocs()));
    }

    public boolean isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(CaseData caseData) {
        return findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_CLAIMANT_ONE));
    }

    public boolean isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(CaseData caseData) {
        return findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_DEFENDANT_ONE));
    }

    public boolean isSDODrawnPreCPRelease(CaseData caseData) {
        return !(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(
            caseData.getCaseManagementLocation().getBaseLocation())
            || featureToggleService.isWelshEnabledForMainCase());
    }

    public boolean isEligibleForReconsideration(CaseData caseData) {
        return (featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(caseData.getCaseManagementLocation().getBaseLocation())
            || featureToggleService.isWelshEnabledForMainCase())
            && caseData.isSmallClaim()
            && caseData.getTotalClaimAmount().compareTo(BigDecimal.valueOf(10000)) <= 0
            && (isNull(caseData.getDecisionOnRequestReconsiderationOptions())
            || !DecisionOnRequestReconsiderationOptions.CREATE_SDO.equals(caseData.getDecisionOnRequestReconsiderationOptions()));
    }

    public boolean hasTrackChanged(CaseData caseData) {
        return SMALL_CLAIM.equals(getPreviousAllocatedTrack(caseData))
            && !caseData.isSmallClaim();
    }

    public boolean isCarmApplicableCase(CaseData caseData) {
        return featureToggleService.isCarmEnabledForCase(caseData)
            && SMALL_CLAIM.equals(getPreviousAllocatedTrack(caseData));
    }

    public boolean isDashBoardEnabledForCase(CaseData caseData) {
        return simpleStateFlowEngine.evaluate(caseData).isFlagSet(FlowFlag.DASHBOARD_SERVICE_ENABLED);
    }

    public boolean isOrderMadeFastTrackTrialNotResponded(CaseData caseData) {
        return SdoHelper.isFastTrack(caseData) && isNull(caseData.getTrialReadyApplicant());
    }

    private AllocatedTrack getPreviousAllocatedTrack(CaseData caseData) {
        return AllocatedTrack.getAllocatedTrack(
            caseData.getTotalClaimAmount(),
            null,
            null
        );
    }
}
