package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordJudgmentOnlineMapper extends JudgmentOnlineMapper {

    @Override
    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {
        List<Element<Party>> defendants = new ArrayList<Element<Party>>();
        defendants.add(element(caseData.getRespondent1()));
        if (caseData.isMultiPartyDefendant()) {
            defendants.add(element(caseData.getRespondent2()));
        }
        JudgmentDetails activeJudgment = super.addUpdateActiveJudgment(caseData);
        return activeJudgment.toBuilder()
            .createdTimestamp(LocalDateTime.now())
            .state(getJudgmentState(caseData))
            .type(JudgmentType.JUDGMENT_FOLLOWING_HEARING)
            .instalmentDetails(caseData.getJoInstalmentDetails())
            .paymentPlan(caseData.getJoPaymentPlan())
            .isRegisterWithRTL(caseData.getJoIsRegisteredWithRTL())
            .issueDate(caseData.getJoOrderMadeDate())
            .orderedAmount(caseData.getJoAmountOrdered())
            .costs(caseData.getJoAmountCostOrdered())
            .totalAmount(new BigDecimal(caseData.getJoAmountOrdered()).add(new BigDecimal(Optional.ofNullable(caseData.getJoAmountCostOrdered()).orElse("0"))).toString())
            .build();
    }

    protected JudgmentState getJudgmentState(CaseData caseData) {
        return JudgmentState.ISSUED;
    }

}
