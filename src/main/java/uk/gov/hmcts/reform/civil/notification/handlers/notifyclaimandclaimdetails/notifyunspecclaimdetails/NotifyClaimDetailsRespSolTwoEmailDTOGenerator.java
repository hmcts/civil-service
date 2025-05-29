package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class NotifyClaimDetailsRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private final NotifyClaimDetailsHelper notifyClaimDetailsHelper;

    protected NotifyClaimDetailsRespSolTwoEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService,
                                                            NotifyClaimDetailsHelper notifyClaimDetailsHelper) {
        super(notificationsProperties, organisationService);
        this.notifyClaimDetailsHelper = notifyClaimDetailsHelper;
    }

    @Override
    protected String getReferenceTemplate() {
        return NotifyClaimDetailsHelper.REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        boolean isRespondent1 = false;
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                isRespondent1, organisationService));
        properties.putAll(notifyClaimDetailsHelper.getCustomProperties(caseData));
        return properties;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        if (isOneVTwoTwoLegalRep(caseData) && caseData.isRespondentTwoSolicitorRegistered()) {
            String respondentName = Optional.ofNullable(caseData.getRespondent2())
                .map(Party::getPartyName)
                .orElse(null);

            return notifyClaimDetailsHelper.checkDefendantToBeNotifiedWithClaimDetails(caseData, respondentName);
        }
        return false;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notifyClaimDetailsHelper.getEmailTemplate();
    }
}
