package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.Optional;

@AllArgsConstructor
public abstract class DefendantEmailDTOGenerator extends EmailDTOGenerator {

    @Override
    public String getEmailAddress(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1())
            .map(Party::getPartyEmail)
            .orElse("");
    }
}
