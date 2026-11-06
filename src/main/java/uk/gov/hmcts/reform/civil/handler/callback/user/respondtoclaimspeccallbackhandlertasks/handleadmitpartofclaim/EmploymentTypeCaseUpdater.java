package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@Slf4j
public class EmploymentTypeCaseUpdater implements HandleAdmitPartOfClaimCaseUpdater {

    @Override
    public void update(CaseData caseData) {
        log.info("Updating Employment Type for caseId: {}", caseData.getCcdCaseReference());

        if (YES.equals(caseData.getIsRespondent1())) {
            caseData.setRespondToClaimAdmitPartEmploymentTypeLRspecGeneric(
                YES.equals(caseData.getDefenceAdmitPartEmploymentTypeRequired())
                    ? caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec()
                    : null
            );
            return;
        }

        if (caseData.isCurrentDefendantRespondent2()) {
            caseData.setRespondToClaimAdmitPartEmploymentTypeLRspecGeneric(
                YES.equals(caseData.getDefenceAdmitPartEmploymentType2Required())
                    ? caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec2()
                    : null
            );
            return;
        }

        // Flags unset (e.g. 1v1): keep previous behaviour
        if (YES.equals(caseData.getDefenceAdmitPartEmploymentTypeRequired())) {
            caseData.setRespondToClaimAdmitPartEmploymentTypeLRspecGeneric(
                caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec());
        }
        if (YES.equals(caseData.getDefenceAdmitPartEmploymentType2Required())) {
            caseData.setRespondToClaimAdmitPartEmploymentTypeLRspecGeneric(
                caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec2());
        }
    }
}
