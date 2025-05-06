package uk.gov.hmcts.reform.civil.notification.handlers.adddefendantlitigantfriend;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class AddDefLitFriendRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    public AddDefLitFriendRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService,
                                                      NotificationsSignatureConfiguration configuration, FeatureToggleService featureToggleService) {
        super(configuration, featureToggleService, organisationService);
        this.notificationsProperties = notificationsProperties;
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
