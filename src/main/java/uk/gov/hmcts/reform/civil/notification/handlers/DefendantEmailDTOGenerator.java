package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;

@AllArgsConstructor
public abstract class DefendantEmailDTOGenerator extends EmailDTOGenerator {

    @Override
    public String getEmailAddress(CaseData caseData) {
        return caseData.getRespondent1PartyEmail();
    }
}
