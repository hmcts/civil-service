package uk.gov.hmcts.reform.civil.service.judgments.paidinfull;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentPaidInFullOnlineMapper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.judgments.CjesService;

@Component
@Slf4j
@AllArgsConstructor
public class JudgmentPaidInFullProcessor {

    private final CjesService cjesService;
    private final JudgmentPaidInFullOnlineMapper paidInFullJudgmentOnlineMapper;

    public CaseData process(CaseData caseData) {
        log.info("Judgment paid in full processor started");
        caseData.setJoIsLiveJudgmentExists(YesOrNo.YES);
        caseData.setActiveJudgment(paidInFullJudgmentOnlineMapper.addUpdateActiveJudgment(caseData));
        caseData.setJoRepaymentSummaryObject(JudgmentsOnlineHelper.calculateRepaymentBreakdownSummary(caseData.getActiveJudgment()));
        if (caseData.isActiveJudgmentRegisteredWithRTL()) {
            cjesService.sendJudgment(caseData, true);
        }
        log.info("Judgment paid in full processor completed");
        return caseData;
    }
}
