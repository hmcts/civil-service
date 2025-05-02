package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
public class ClaimantResponseConfirmsToProceedEmailHelper {

    private final NotificationsProperties notificationsProperties;
    private final FeatureToggleService featureToggleService;

    public ClaimantResponseConfirmsToProceedEmailHelper(NotificationsProperties notificationsProperties, FeatureToggleService featureToggleService) {
        this.notificationsProperties = notificationsProperties;
        this.featureToggleService = featureToggleService;
    }

    public String getTemplate(CaseData caseData, boolean isApplicant) {
        if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            boolean proceedsWithAction = rejectedAll(caseData) && mediationRejected(caseData);
            if (isApplicant) {
                return  proceedsWithAction ? notificationsProperties.getClaimantSolicitorConfirmsToProceedSpecWithAction()
                    : notificationsProperties.getClaimantSolicitorConfirmsToProceedSpec();
            }

            return proceedsWithAction ? notificationsProperties.getRespondentSolicitorNotifyToProceedSpecWithAction()
                : notificationsProperties.getRespondentSolicitorNotifyToProceedSpec();

        } else if (caseData.getAllocatedTrack().equals(MULTI_CLAIM) && !featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            return notificationsProperties.getSolicitorCaseTakenOffline();
        }

        return notificationsProperties.getClaimantSolicitorConfirmsToProceed();
    }

    private boolean rejectedAll(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE;
    }

    private boolean mediationRejected(CaseData caseData) {
        return Stream.of(
            caseData.getResponseClaimMediationSpecRequired(),
            caseData.getResponseClaimMediationSpec2Required(),
            Optional.ofNullable(caseData.getApplicant1ClaimMediationSpecRequired())
                .map(SmallClaimMedicalLRspec::getHasAgreedFreeMediation).orElse(null)
        ).filter(Objects::nonNull).anyMatch(YesOrNo.NO::equals);
    }

    public boolean isMultiPartyNotProceed(CaseData caseData, boolean isRespondent2) {
        return (NO.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()) && isRespondent2)
            || (NO.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()) && !isRespondent2);
    }
}
