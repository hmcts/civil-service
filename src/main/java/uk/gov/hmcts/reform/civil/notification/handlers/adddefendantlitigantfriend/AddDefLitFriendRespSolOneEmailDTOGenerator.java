package uk.gov.hmcts.reform.civil.notification.handlers.adddefendantlitigantfriend;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class AddDefLitFriendRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    public AddDefLitFriendRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(notificationsProperties, organisationService);
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getSolicitorLitigationFriendAdded();
    }

    @Override
    protected String getReferenceTemplate() {
        return "litigation-friend-added-respondent-notification-%s";
    }

}
