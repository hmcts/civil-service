package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.util.Optional;

@Component
public class OwingAmountCaseUpdater implements HandleAdmitPartOfClaimCaseUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmount())
                .map(MonetaryConversions::penniesToPounds)
                .ifPresent(updatedCaseData::respondToAdmittedClaimOwingAmountPounds);
        Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmount2())
                .map(MonetaryConversions::penniesToPounds)
                .ifPresent(updatedCaseData::respondToAdmittedClaimOwingAmountPounds2);
    }
}
