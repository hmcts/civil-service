package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class JudgmentOnlineMapper {

    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {
        JudgmentDetails activeJudgment = isNull(caseData.getActiveJudgment()) ? JudgmentDetails.builder()
            .judgmentId(getNextJudgmentId(caseData)).build() : caseData.getActiveJudgment();
        return activeJudgment.toBuilder()
            .isJointJudgment(YesOrNo.YES)
            .lastUpdateTimeStamp(LocalDateTime.now())
            .courtLocation(caseData.getCaseManagementLocation() != null ? caseData.getCaseManagementLocation().getBaseLocation() : null)
            .build();
    }

    public void moveToHistoricJudgment(CaseData caseData) {
        JudgmentDetails activeJudgment = addUpdateActiveJudgment(caseData);
        if (isHistoricJudgment(activeJudgment)) {
            List<Element<JudgmentDetails>> historicList = isNull(caseData.getHistoricJudgment())
                ? new ArrayList<>() : caseData.getHistoricJudgment();
            historicList.add(element(activeJudgment));
            Collections.sort(
                historicList,
                (o1, o2) -> o2.getValue().getLastUpdateTimeStamp().compareTo(o1.getValue().getLastUpdateTimeStamp())
            );
            caseData.setHistoricJudgment(historicList);
            caseData.setActiveJudgment(null);
        } else {
            caseData.setActiveJudgment(activeJudgment);
        }
    }

    protected abstract JudgmentState getJudgmentState(CaseData caseData);

    private Integer getNextJudgmentId(CaseData caseData) {
        return caseData.getHistoricJudgment() != null ? caseData.getHistoricJudgment().size() + 1
            : 1;
    }

    private boolean isHistoricJudgment(JudgmentDetails activeJudgment) {
        return JudgmentState.CANCELLED.equals(activeJudgment.getState())
            || JudgmentState.SET_ASIDE_ERROR.equals(activeJudgment.getState())
            || JudgmentState.SET_ASIDE.equals(activeJudgment.getState());
    }
}
