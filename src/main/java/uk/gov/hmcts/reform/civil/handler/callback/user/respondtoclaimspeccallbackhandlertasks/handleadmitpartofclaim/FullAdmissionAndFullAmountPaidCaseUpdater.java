package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Slf4j
@Component
public class FullAdmissionAndFullAmountPaidCaseUpdater implements HandleAdmitPartOfClaimCaseUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Updating Full Admission and Full Amount Paid for caseId: {}", caseData.getCcdCaseReference());

        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceFullAdmitted2Required())) {
            log.debug("Condition met for Respondent 2 for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.fullAdmissionAndFullAmountPaid(YES);
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceFullAdmittedRequired())) {
            log.debug("Condition met for Respondent 1 for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.fullAdmissionAndFullAmountPaid(YES);
        } else {
            log.debug("No conditions met for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.fullAdmissionAndFullAmountPaid(NO);
        }
    }
}