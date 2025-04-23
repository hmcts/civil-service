package uk.gov.hmcts.reform.civil.notification.handlers.adddefendantlitigantfriend;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class AddDefLitFriendAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    public AddDefLitFriendAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(notificationsProperties, organisationService);
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getSolicitorLitigationFriendAdded();
    }

    @Override
    protected String getReferenceTemplate() {
        return "litigation-friend-added-applicant-notification-%s";
    }

}
