package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;


@Slf4j
@Service
@RequiredArgsConstructor
public abstract class JudgmentOnlineMapper {

    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {
        //TODO CHECK IF WE NEED THIS
        JudgmentDetails activeJudgment = isNull(caseData.getActiveJudgment()) ? JudgmentDetails.builder().build() : caseData.getActiveJudgment();
        return activeJudgment.toBuilder()
            .judgmentId(getNextJudgmentId(caseData))
            .isJointJudgment(YesOrNo.YES)
            .courtLocation(caseData.getCaseManagementLocation().getBaseLocation())//TODO: verify if this is right
            .build();
    }

    public void updateHistoricJudgment(CaseData caseData) {
        JudgmentDetails activeJudgment = addUpdateActiveJudgment(caseData);
        if (isHistoricJudgment(activeJudgment)) {
            if (isNull(caseData.getHistoricJudgment())) {
                List<Element<JudgmentDetails>> historicList = new ArrayList<Element<JudgmentDetails>>();
                historicList.add(element(activeJudgment));
                caseData.setActiveJudgment(null);
                caseData.setHistoricJudgment(historicList);
            }
        } else {
            caseData.setActiveJudgment(activeJudgment);
        }
    }

    protected abstract JudgmentState getJudgmentState(CaseData caseData);

    public Integer getNextJudgmentId(CaseData caseData) {
        return caseData.getActiveJudgment() != null ? caseData.getActiveJudgment().getJudgmentId()
            : Optional.ofNullable(caseData.getHistoricJudgment()).orElse(Collections.emptyList()).size() + 1;
    }

    private boolean isHistoricJudgment(JudgmentDetails activeJudgment) {
        return JudgmentState.CANCELLED.equals(activeJudgment.getState())
            || JudgmentState.SET_ASIDE_ERROR.equals(activeJudgment.getState())
            || JudgmentState.SET_ASIDE.equals(activeJudgment.getState()) ? true : false;
    }
}
