package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@Slf4j
public class PartAdmittedByEitherRespondentsCaseUpdater implements HandleAdmitPartOfClaimCaseUpdater {

    @Override
    public void update(CaseData caseData) {
        log.info("Updating Part Admitted By Either Respondents for caseId: {}", caseData.getCcdCaseReference());
        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceAdmitted2Required())) {
            caseData.setPartAdmittedByEitherRespondents(YES);
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceAdmittedRequired())) {
            caseData.setPartAdmittedByEitherRespondents(YES);
        } else {
            caseData.setPartAdmittedByEitherRespondents(NO);
        }
    }
}
