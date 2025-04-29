package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class ClaimantResponseConfirmsToProceedRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private final ClaimantResponseConfirmsToProceedEmailHelper claimantResponseConfirmsToProceedEmailHelper;

    protected ClaimantResponseConfirmsToProceedRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService,
                                                                           ClaimantResponseConfirmsToProceedEmailHelper claimantResponseConfirmsToProceedEmailHelper) {
        super(notificationsProperties, organisationService);
        this.claimantResponseConfirmsToProceedEmailHelper = claimantResponseConfirmsToProceedEmailHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        boolean isMultiPartyNotProceed = claimantResponseConfirmsToProceedEmailHelper.isMultiPartyNotProceed(caseData, false);
        return claimantResponseConfirmsToProceedEmailHelper.getTemplate(caseData, false, isMultiPartyNotProceed);
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-confirms-to-proceed-respondent-notification-%s";
    }
}
