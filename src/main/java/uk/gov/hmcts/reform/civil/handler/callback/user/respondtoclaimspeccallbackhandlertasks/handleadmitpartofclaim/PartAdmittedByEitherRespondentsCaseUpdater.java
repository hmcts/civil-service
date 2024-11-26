package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
public class PartAdmittedByEitherRespondentsCaseUpdater implements HandleAdmitPartOfClaimCaseUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceAdmitted2Required())) {
            updatedCaseData.partAdmittedByEitherRespondents(YES);
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceAdmittedRequired())) {
            updatedCaseData.partAdmittedByEitherRespondents(YES);
        } else {
            updatedCaseData.partAdmittedByEitherRespondents(NO);
        }
    }
}
