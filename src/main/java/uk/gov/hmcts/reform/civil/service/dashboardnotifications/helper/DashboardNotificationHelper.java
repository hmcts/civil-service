package uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LanguagePredicate;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;

import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.isWelshApplicant;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;

@Service
public class DashboardNotificationHelper {

    private final FeatureToggleService featureToggleService;
    private final SimpleStateFlowEngine simpleStateFlowEngine;
    private final SdoCaseClassificationService sdoCaseClassificationService;

    public DashboardNotificationHelper(FeatureToggleService featureToggleService,
                                       SimpleStateFlowEngine simpleStateFlowEngine,
                                       SdoCaseClassificationService sdoCaseClassificationService) {
        this.featureToggleService = featureToggleService;
        this.simpleStateFlowEngine = simpleStateFlowEngine;
        this.sdoCaseClassificationService = sdoCaseClassificationService;
    }

    public boolean isAwaitingWelshTranslation(CaseData caseData, CaseEvent event) {
        return caseData.getBusinessProcess() != null
            && event.name().equals(caseData.getBusinessProcess().getCamundaEvent())
            && caseData.isApplicantLiP()
            && LanguagePredicate.claimantIsBilingual.test(caseData)
            && featureToggleService.isWelshEnabledForMainCase();
    }

    public boolean isAwaitingWelshTranslation(GeneralApplicationCaseData caseData) {
        return caseData.getBusinessProcess() != null
            && INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT.name().equals(caseData.getBusinessProcess().getCamundaEvent())
            && isWelshApplicant.test(caseData);
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

    public boolean isOrderMadeFastTrackTrialNotRespondedDefendant(CaseData caseData) {
        return sdoCaseClassificationService.isFastTrack(caseData) && isNull(caseData.getTrialReadyRespondent1());
    }

    public boolean isOrderMadeFastTrackTrialNotRespondedClaimant(CaseData caseData) {
        return sdoCaseClassificationService.isFastTrack(caseData) && isNull(caseData.getTrialReadyApplicant());
    }

    private AllocatedTrack getPreviousAllocatedTrack(CaseData caseData) {
        return AllocatedTrack.getAllocatedTrack(
            caseData.getTotalClaimAmount(),
            null,
            null
        );
    }
}
