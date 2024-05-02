package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;

@Slf4j
@Service
@RequiredArgsConstructor
public class EditJudgmentOnlineMapper extends RecordJudgmentOnlineMapper {

    @Override
    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {

        //TODO Will there be a case where we will edit existing judgments after going live ?
        // in that case Ill have to check activeJudgment exists or not
        JudgmentDetails activeJudgment = super.addUpdateActiveJudgment(caseData);
        activeJudgment.toBuilder()
            .state(getJudgmentState(caseData))
            .build();
        return activeJudgment;
    }

    protected JudgmentState getJudgmentState(CaseData caseData) {
        return JudgmentState.MODIFIED;
    }
}
