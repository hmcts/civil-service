package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@Slf4j
public class DefenceAdmitPartPaymentTimeRouteCaseUpdater implements HandleAdmitPartOfClaimCaseUpdater {

    @Override
    public void update(CaseData caseData) {
        log.info("Updating Defence Admit Part Payment Time Route for caseId: {}", caseData.getCcdCaseReference());

        if (YES.equals(caseData.getIsRespondent1())) {
            caseData.setDefenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            log.debug("Respondent 1 payment route generic updated for caseId: {}", caseData.getCcdCaseReference());
        } else if (caseData.isCurrentDefendantRespondent2()) {
            caseData.setDefenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired2());
            log.debug("Respondent 2 payment route generic updated for caseId: {}", caseData.getCcdCaseReference());
        }
    }
}
