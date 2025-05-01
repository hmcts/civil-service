package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;

import java.math.BigDecimal;
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

    private static final int MAX_LENGTH_PARTY_NAME = 70;

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
        updateJudgmentTabDataWithActiveJudgment(activeJudgment, caseData);
    }

    public JudgmentDetails updateDefendantDetails(JudgmentDetails activeJudgment, CaseData caseData, RoboticsAddressMapper addressMapper) {
        if (caseData.getRespondent1().getPartyName() != null
            && caseData.getRespondent1().getPartyName().length() > MAX_LENGTH_PARTY_NAME) {
            activeJudgment.setDefendant1Name(JudgmentsOnlineHelper.removeWelshCharacters(caseData.getRespondent1().getPartyName().substring(
                0, MAX_LENGTH_PARTY_NAME)));
        } else {
            activeJudgment.setDefendant1Name(JudgmentsOnlineHelper.removeWelshCharacters(caseData.getRespondent1().getPartyName()));
        }
        activeJudgment.setDefendant1Address(JudgmentsOnlineHelper.getJudgmentAddress(caseData.getRespondent1().getPrimaryAddress(), addressMapper));
        activeJudgment.setDefendant1Dob(caseData.getRespondent1().getDateOfBirth());
        if (YesOrNo.YES == caseData.getAddRespondent2()) {
            activeJudgment.setDefendant2Name(caseData.getRespondent2().getPartyName());
            activeJudgment.setDefendant2Address(JudgmentsOnlineHelper.getJudgmentAddress(caseData.getRespondent2().getPrimaryAddress(), addressMapper));
            activeJudgment.setDefendant2Dob(caseData.getRespondent2().getDateOfBirth());
        }
        return activeJudgment;
    }

    public void updateJudgmentTabDataWithActiveJudgment(JudgmentDetails activeJudgment, CaseData caseData) {
        caseData.setJoIsDisplayInJudgmentTab(YesOrNo.YES);
        caseData.setJoDefendantName1(activeJudgment.getDefendant1Name());
        caseData.setJoDefendantName2(activeJudgment.getDefendant2Name());
        caseData.setJoPaymentPlanSelected(activeJudgment.getPaymentPlan().getType());
        caseData.setJoState(activeJudgment.getState());
        if (null != activeJudgment.getPaymentPlan()
            && PaymentPlanSelection.PAY_IN_INSTALMENTS.equals(activeJudgment.getPaymentPlan().getType())) {
            caseData.setJoRepaymentAmount(activeJudgment.getInstalmentDetails().getAmount());
            caseData.setJoRepaymentStartDate(activeJudgment.getInstalmentDetails().getStartDate());
            caseData.setJoRepaymentFrequency(activeJudgment.getInstalmentDetails().getPaymentFrequency());
        } else {
            caseData.setJoRepaymentAmount(null);
            caseData.setJoRepaymentStartDate(null);
            caseData.setJoRepaymentFrequency(null);
        }

        if (JudgmentState.CANCELLED.equals(activeJudgment.getState())
            || JudgmentState.SATISFIED.equals(activeJudgment.getState())) {
            caseData.setJoIssueDate(activeJudgment.getIssueDate());
            caseData.setJoFullyPaymentMadeDate(activeJudgment.getFullyPaymentMadeDate());
        }
    }

    public void updateJudgmentTabDataWithActiveJudgment(JudgmentDetails activeJudgment, CaseData.CaseDataBuilder<?, ?> caseDataBuilder, BigDecimal interest) {
        caseDataBuilder.joIsLiveJudgmentExists(YesOrNo.YES);
        caseDataBuilder.joIsDisplayInJudgmentTab(YesOrNo.YES);
        caseDataBuilder.joDefendantName1(activeJudgment.getDefendant1Name());
        caseDataBuilder.joDefendantName2(activeJudgment.getDefendant2Name());
        caseDataBuilder.joPaymentPlanSelected(activeJudgment.getPaymentPlan().getType());
        caseDataBuilder.joState(activeJudgment.getState());
        if (null != activeJudgment.getPaymentPlan()
            && PaymentPlanSelection.PAY_IN_INSTALMENTS.equals(activeJudgment.getPaymentPlan().getType())) {
            caseDataBuilder.joRepaymentAmount(activeJudgment.getInstalmentDetails().getAmount());
            caseDataBuilder.joRepaymentStartDate(activeJudgment.getInstalmentDetails().getStartDate());
            caseDataBuilder.joRepaymentFrequency(activeJudgment.getInstalmentDetails().getPaymentFrequency());
        } else {
            caseDataBuilder.joRepaymentAmount(null);
            caseDataBuilder.joRepaymentStartDate(null);
            caseDataBuilder.joRepaymentFrequency(null);
        }

        if (JudgmentState.CANCELLED.equals(activeJudgment.getState())
            || JudgmentState.SATISFIED.equals(activeJudgment.getState())) {
            caseDataBuilder.joIssueDate(activeJudgment.getIssueDate());
            caseDataBuilder.joFullyPaymentMadeDate(activeJudgment.getFullyPaymentMadeDate());
        }
        caseDataBuilder.joRepaymentSummaryObject(JudgmentsOnlineHelper.calculateRepaymentBreakdownSummary(activeJudgment, interest))
            .joJudgementByAdmissionIssueDate(LocalDateTime.now());
    }

    protected abstract JudgmentState getJudgmentState(CaseData caseData);

    private Integer getNextJudgmentId(CaseData caseData) {
        return caseData.getHistoricJudgment() != null ? caseData.getHistoricJudgment().size() + 1
            : 1;
    }

    private boolean isHistoricJudgment(JudgmentDetails activeJudgment) {
        return JudgmentState.SET_ASIDE_ERROR.equals(activeJudgment.getState())
            || JudgmentState.SET_ASIDE.equals(activeJudgment.getState());
    }
}
