package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
public class NotifyClaimRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private final NotifyClaimHelper notifyClaimHelper;

    public NotifyClaimRespSolTwoEmailDTOGenerator(OrganisationService organisationService,
                                                  NotifyClaimHelper notifyClaimHelper) {
        super(organisationService);
        this.notifyClaimHelper = notifyClaimHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notifyClaimHelper.getNotifyClaimEmailTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return NotifyClaimHelper.NOTIFY_REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(notifyClaimHelper.retrieveCustomProperties(caseData));
        return properties;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        if (isOneVTwoTwoLegalRep(caseData) && caseData.isRespondentTwoSolicitorRegistered()) {
            String respondentName = Optional.ofNullable(caseData.getRespondent2())
                .map(Party::getPartyName)
                .orElse(null);

            return notifyClaimHelper.checkIfThisDefendantToBeNotified(caseData, respondentName);
        }
        return false;
    }
}
