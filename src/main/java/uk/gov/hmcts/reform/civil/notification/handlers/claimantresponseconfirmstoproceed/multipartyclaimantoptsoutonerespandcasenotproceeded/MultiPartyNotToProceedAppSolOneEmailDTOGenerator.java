package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed.multipartyclaimantoptsoutonerespandcasenotproceeded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class MultiPartyNotToProceedAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    protected MultiPartyNotToProceedAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
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
