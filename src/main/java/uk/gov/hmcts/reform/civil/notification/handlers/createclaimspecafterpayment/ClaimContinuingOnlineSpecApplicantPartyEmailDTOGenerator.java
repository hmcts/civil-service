package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimContinuingOnlineSpecApplicantPartyEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final FeatureToggleService featureToggleService;

    public ClaimContinuingOnlineSpecApplicantPartyEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            FeatureToggleService featureToggleService
    ) {
        this.notificationsProperties = notificationsProperties;
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.isLipvLipOneVOne()
                && featureToggleService.isLipVLipEnabled()
                && caseData.isClaimantBilingual()) {
            return notificationsProperties.getBilingualClaimantClaimContinuingOnlineForSpec();
        }
        return notificationsProperties.getClaimantClaimContinuingOnlineForSpec();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return true;
    }

    @Override
    protected Map<String, String> addCustomProperties(
            Map<String, String> properties,
            CaseData caseData
    ) {
        properties.put(RESPONDENT_NAME,
                getPartyNameBasedOnType(caseData.getRespondent1()));
        properties.put(CLAIMANT_NAME,
                getPartyNameBasedOnType(caseData.getApplicant1()));
        properties.put(ISSUED_ON,
                formatLocalDate(caseData.getIssueDate(), DATE));
        properties.put(CLAIM_REFERENCE_NUMBER,
                caseData.getLegacyCaseReference());
        properties.put(RESPONSE_DEADLINE,
                formatLocalDate(
                        caseData.getRespondent1ResponseDeadline().toLocalDate(),
                        DATE
                ));
        return properties;
    }
}
