package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
public class NotifyClaimDetailsRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private final NotifyClaimDetailsHelper notifyClaimDetailsHelper;

    public NotifyClaimDetailsRespSolTwoEmailDTOGenerator(OrganisationService organisationService,
                                                  NotifyClaimDetailsHelper notifyClaimHelper) {
        super(organisationService);
        this.notifyClaimDetailsHelper = notifyClaimHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notifyClaimDetailsHelper.getEmailTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return NotifyClaimDetailsHelper.REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
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
}
