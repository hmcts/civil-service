package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
@Slf4j
public class SpecFullAdmitPaidCaseUpdater implements HandleAdmitPartOfClaimCaseUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Updating SpecFullAdmitPaidCase for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION
                && caseData.getSpecDefenceFullAdmittedRequired() == NO) {
            log.debug("Setting specFullAdmitPaid to NO for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.specFullAdmitPaid(NO);
        }
    }
}
