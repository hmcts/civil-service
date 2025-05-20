package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.confirmproceed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@RequiredArgsConstructor
@Slf4j
@Component
public class ClaimantRespConfirmProceedClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "claimant-confirms-to-proceed-applicant-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final FeatureToggleService featureToggleService;
    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        System.out.println("w");
        if (isBilingualForLipApplicant(caseData)) {
            return notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate();
        }
        return notificationsProperties.getClaimantLipClaimUpdatedTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    private boolean isBilingualForLipApplicant(CaseData caseData) {
        return caseData.isApplicantNotRepresented() && featureToggleService.isLipVLipEnabled()
            && caseData.isClaimantBilingual();
    }
}
