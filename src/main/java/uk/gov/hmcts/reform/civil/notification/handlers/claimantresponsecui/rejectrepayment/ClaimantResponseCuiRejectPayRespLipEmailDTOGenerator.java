package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.rejectrepayment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClaimantResponseCuiRejectPayRespLipEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "claimant-reject-repayment-respondent-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final FeatureToggleService featureToggleService;

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (isBilingualForLipApplicant(caseData)) {
            return notificationsProperties.getNotifyDefendantLipWelshTemplate();
        }
        return notificationsProperties.getNotifyDefendantLipTemplate();
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
