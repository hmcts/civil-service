package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class NotifyClaimDetailsRespOneSolEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private final NotifyClaimDetailsHelper notifyClaimDetailsHelper;

    protected NotifyClaimDetailsRespOneSolEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService,
                                                            NotifyClaimDetailsHelper notifyClaimDetailsHelper) {
        super(notificationsProperties, organisationService);
        this.notifyClaimDetailsHelper = notifyClaimDetailsHelper;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        boolean isRespondent1 = true;
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                        isRespondent1, organisationService));
        properties.putAll(notifyClaimDetailsHelper.getCustomProperties(caseData));
        return properties;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        String respondentName = Optional.ofNullable(caseData.getRespondent1())
            .map(Party::getPartyName)
            .orElse(null);

        if (!caseData.isRespondentSolicitorRegistered()) {
            return false;
        }

        if (isOneVTwoTwoLegalRep(caseData)) {
            return notifyClaimDetailsHelper.checkDefendantToBeNotifiedWithClaimDetails(caseData, respondentName);
        }

        return true;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notifyClaimDetailsHelper.getEmailTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return NotifyClaimDetailsHelper.REFERENCE_TEMPLATE;
    }
}
