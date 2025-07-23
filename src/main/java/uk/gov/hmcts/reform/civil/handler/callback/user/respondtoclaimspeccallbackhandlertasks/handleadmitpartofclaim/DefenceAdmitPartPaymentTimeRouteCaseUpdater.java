package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@Slf4j
public class DefenceAdmitPartPaymentTimeRouteCaseUpdater implements HandleAdmitPartOfClaimCaseUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Updating Defence Admit Part Payment Time Route for caseId: {}", caseData.getCcdCaseReference());

        if (YES.equals(caseData.getIsRespondent1()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null) {
            log.debug("Respondent 1 condition met for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
        } else if (YES.equals(caseData.getIsRespondent2()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != null) {
            log.debug("Respondent 2 condition met for caseId: {}", caseData.getCcdCaseReference());
            updatedCaseData.defenceAdmitPartPaymentTimeRouteGeneric(caseData.getDefenceAdmitPartPaymentTimeRouteRequired2());
        }
    }
}
