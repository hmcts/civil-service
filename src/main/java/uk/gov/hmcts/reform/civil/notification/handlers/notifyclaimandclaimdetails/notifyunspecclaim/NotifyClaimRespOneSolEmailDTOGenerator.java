package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
public class NotifyClaimRespOneSolEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private final NotifyClaimHelper notifyClaimHelper;

    public NotifyClaimRespOneSolEmailDTOGenerator(OrganisationService organisationService,
                                              NotifyClaimHelper notifyClaimHelper) {
        super(organisationService);
        this.notifyClaimHelper = notifyClaimHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notifyClaimHelper.getEmailTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return NotifyClaimHelper.REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(notifyClaimHelper.getCustomProperties(caseData));
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
            return notifyClaimHelper.checkIfThisDefendantToBeNotified(caseData, respondentName);
        }

        return true;
    }
}
