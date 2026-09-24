package uk.gov.hmcts.reform.civil.notification.handlers.unspecclaimsettled;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class UnspecClaimSettledRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "notify-defendant-lr-unspec-claim-settled-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected UnspecClaimSettledRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                            OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return isOneVOne(caseData)
            && !caseData.isRespondent1LiP()
            && StringUtils.isNotEmpty(caseData.getRespondentSolicitor1EmailAddress());
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyDefendantLRClaimantSettleTheClaimUnspecTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        super.addCustomProperties(properties, caseData);
        properties.put(CLAIMANT_NAME, caseData.getApplicant1().getPartyName());
        properties.put(CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString());
        properties.put(DEFENDANT_REFERENCE_NUMBER, getDefRefNumber(caseData));
        properties.put(LEGAL_REP_NAME, getLegalOrganizationNameForRespondent(caseData, true, organisationService));
        return properties;
    }

    private String getDefRefNumber(CaseData caseData) {
        if (nonNull(caseData.getSolicitorReferences())
            && nonNull(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())) {
            return caseData.getSolicitorReferences().getRespondentSolicitor1Reference();
        }
        return "Not provided";
    }
}
