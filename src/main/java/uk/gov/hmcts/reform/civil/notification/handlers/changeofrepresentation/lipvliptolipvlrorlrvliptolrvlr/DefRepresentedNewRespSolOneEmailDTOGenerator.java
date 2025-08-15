package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolipvlrorlrvliptolrvlr;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.common.NotificationHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

@Component
public class DefRepresentedNewRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE =
        "notify-lr-after-defendant-noc-approval-%s";

    private final NotificationsProperties notificationsProperties;

    public DefRepresentedNewRespSolOneEmailDTOGenerator(OrganisationService organisationService,
                                                        NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyDefendantLrAfterNoticeOfChangeTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    public Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(Map.of(
            DEFENDANT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
            LEGAL_REP_NAME, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID())
        ));
        return properties;
    }

    public String getOrganisationName(String orgId) {
        return Optional.ofNullable(orgId)
            .map(id -> organisationService.findOrganisationById(id)
                .orElseThrow(() -> new CallbackException("Invalid organisation ID: " + id)).getName())
            .orElse(NotificationHelper.LIP);
    }
}
