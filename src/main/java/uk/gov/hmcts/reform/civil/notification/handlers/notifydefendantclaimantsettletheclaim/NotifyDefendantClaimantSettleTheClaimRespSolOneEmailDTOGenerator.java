package uk.gov.hmcts.reform.civil.notification.handlers.notifydefendantclaimantsettletheclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static java.util.Objects.nonNull;

@Component
public class NotifyDefendantClaimantSettleTheClaimRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    protected NotifyDefendantClaimantSettleTheClaimRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                                                OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyDefendantLRClaimantSettleTheClaimTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "notify-defendant-lr-claimant-settle-the-claim-notification-%s";
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        super.addCustomProperties(properties, caseData);
        properties.put(CLAIMANT_NAME, caseData.getApplicant1().getPartyName());
        properties.put(CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString());
        properties.put(DEFENDANT_REFERENCE_NUMBER, getDefRefNumber(caseData));
        return properties;
    }

    private String getDefRefNumber(CaseData caseData) {
        if (nonNull(caseData.getSolicitorReferences())
            && nonNull(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())) {
            return caseData.getSolicitorReferences().getRespondentSolicitor1Reference();
        } else {
            return "Not provided";
        }
    }
}
