package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseagreedsettledpartadmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class ClaimantResponseAgreedSettledPartAdmitRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    protected ClaimantResponseAgreedSettledPartAdmitRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(notificationsProperties, organisationService);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getRespondentLrPartAdmitSettleClaimTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-part-admit-settle-respondent-notification-%s";
    }
}
