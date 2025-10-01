package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.util.Optional;

@Component
@Slf4j
public class OwingAmountCaseUpdater implements HandleAdmitPartOfClaimCaseUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Updating Owing Amount for caseId: {}", caseData.getCcdCaseReference());
        Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmount())
                .map(MonetaryConversions::penniesToPounds)
                .ifPresent(updatedCaseData::respondToAdmittedClaimOwingAmountPounds);
        Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmount2())
                .map(MonetaryConversions::penniesToPounds)
                .ifPresent(updatedCaseData::respondToAdmittedClaimOwingAmountPounds2);
    }
}
