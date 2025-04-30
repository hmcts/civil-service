package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed.multiPartyClaimantOptsOutOneRespAndCaseNotProceeded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class MultiPartyNotToProceedRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    protected MultiPartyNotToProceedRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(notificationsProperties, organisationService);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getClaimantSolicitorConfirmsNotToProceed();
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-confirms-not-to-proceed-respondent-notification-%s";
    }
}
