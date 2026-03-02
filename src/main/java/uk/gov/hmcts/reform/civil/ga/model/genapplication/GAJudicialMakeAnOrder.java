package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.ga.enums.GAJudgeOrderClaimantOrDefenseFixedList;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.FinalOrderShowToggle;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption;

import java.time.LocalDate;
import java.util.List;

@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAJudicialMakeAnOrder {

    private List<FinalOrderShowToggle> showJudgeRecitalText;
    private String judgeRecitalText;
    private GAJudgeMakeAnOrderOption makeAnOrder;
    private String orderText;
    private String dismissalOrderText;
    private String directionsText;
    private LocalDate directionsResponseByDate;
    private String reasonForDecisionText;
    private LocalDate judgeApproveEditOptionDate;
    private YesOrNo displayjudgeApproveEditOptionDate;
    private YesOrNo displayjudgeApproveEditOptionDoc;
    private GAJudgeOrderClaimantOrDefenseFixedList judgeApproveEditOptionDoc;
    private YesOrNo isOrderProcessedByStayScheduler;
    private GAByCourtsInitiativeGAspec judicialByCourtsInitiative;
    private YesOrNo displayjudgeApproveEditOptionDateForUnlessOrder;
    private LocalDate judgeApproveEditOptionDateForUnlessOrder;
    private YesOrNo isOrderProcessedByUnlessScheduler;
    private String orderCourtOwnInitiative;
    private LocalDate orderCourtOwnInitiativeDate;
    private String orderWithoutNotice;
    private LocalDate orderWithoutNoticeDate;
    private YesOrNo showReasonForDecision;

    @JsonCreator
    GAJudicialMakeAnOrder(@JsonProperty("showJudgeRecitalText") List<FinalOrderShowToggle> showJudgeRecitalText,
                          @JsonProperty("judgeRecitalText") String judgeRecitalText,
                          @JsonProperty("makeAnOrder") GAJudgeMakeAnOrderOption makeAnOrder,
                          @JsonProperty("orderText") String orderText,
                          @JsonProperty("dismissalOrderText") String dismissalOrderText,
                          @JsonProperty("directionsText") String directionsText,
                          @JsonProperty("directionsResponseByDate") LocalDate directionsResponseByDate,
                          @JsonProperty("reasonForDecisionText") String reasonForDecisionText,
                          @JsonProperty("judgeApproveEditOptionDate") LocalDate judgeApproveEditOptionDate,
                          @JsonProperty("displayjudgeApproveEditOptionDate") YesOrNo displayjudgeApproveEditOptionDate,
                          @JsonProperty("displayjudgeApproveEditOptionDoc")
                              YesOrNo displayjudgeApproveEditOptionDoc,
                          @JsonProperty("judgeApproveEditOptionDoc")
                              GAJudgeOrderClaimantOrDefenseFixedList judgeApproveEditOptionDoc,
                          @JsonProperty("isOrderProcessedByStayScheduler")
                          YesOrNo isOrderProcessedByStayScheduler,
                          @JsonProperty("judicialByCourtsInitiative") GAByCourtsInitiativeGAspec
                              judicialByCourtsInitiative,
                          @JsonProperty("displayjudgeApproveEditOptionDateForUnlessOrder")
                              YesOrNo displayjudgeApproveEditOptionDateForUnlessOrder,
                          @JsonProperty("judgeApproveEditOptionDateForUnlessOrder")
                              LocalDate judgeApproveEditOptionDateForUnlessOrder,
                          @JsonProperty("isOrderProcessedByUnlessScheduler")
                              YesOrNo isOrderProcessedByUnlessScheduler,
                          @JsonProperty("orderCourtOwnInitiative") String orderCourtOwnInitiative,
                          @JsonProperty("orderCourtOwnInitiativeDate") LocalDate orderCourtOwnInitiativeDate,
                          @JsonProperty("orderWithoutNotice") String orderWithoutNotice,
                          @JsonProperty("orderWithoutNoticeDate") LocalDate orderWithoutNoticeDate,
                          @JsonProperty("showReasonForDecision") YesOrNo showReasonForDecision) {
        this.showJudgeRecitalText = showJudgeRecitalText;
        this.judgeRecitalText = judgeRecitalText;
        this.makeAnOrder = makeAnOrder;
        this.orderText = orderText;
        this.dismissalOrderText = dismissalOrderText;
        this.directionsText = directionsText;
        this.directionsResponseByDate = directionsResponseByDate;
        this.reasonForDecisionText = reasonForDecisionText;
        this.judgeApproveEditOptionDate = judgeApproveEditOptionDate;
        this.displayjudgeApproveEditOptionDate = displayjudgeApproveEditOptionDate;
        this.displayjudgeApproveEditOptionDoc = displayjudgeApproveEditOptionDoc;
        this.judgeApproveEditOptionDoc = judgeApproveEditOptionDoc;
        this.isOrderProcessedByStayScheduler = isOrderProcessedByStayScheduler;
        this.judicialByCourtsInitiative = judicialByCourtsInitiative;
        this.displayjudgeApproveEditOptionDateForUnlessOrder = displayjudgeApproveEditOptionDateForUnlessOrder;
        this.judgeApproveEditOptionDateForUnlessOrder = judgeApproveEditOptionDateForUnlessOrder;
        this.isOrderProcessedByUnlessScheduler = isOrderProcessedByUnlessScheduler;
        this.orderCourtOwnInitiative = orderCourtOwnInitiative;
        this.orderCourtOwnInitiativeDate = orderCourtOwnInitiativeDate;
        this.orderWithoutNotice = orderWithoutNotice;
        this.orderWithoutNoticeDate = orderWithoutNoticeDate;
        this.showReasonForDecision = showReasonForDecision;
    }

    public GAJudicialMakeAnOrder copy() {
        return new GAJudicialMakeAnOrder()
            .setShowJudgeRecitalText(showJudgeRecitalText)
            .setJudgeRecitalText(judgeRecitalText)
            .setMakeAnOrder(makeAnOrder)
            .setOrderText(orderText)
            .setDismissalOrderText(dismissalOrderText)
            .setDirectionsText(directionsText)
            .setDirectionsResponseByDate(directionsResponseByDate)
            .setReasonForDecisionText(reasonForDecisionText)
            .setJudgeApproveEditOptionDate(judgeApproveEditOptionDate)
            .setDisplayjudgeApproveEditOptionDate(displayjudgeApproveEditOptionDate)
            .setDisplayjudgeApproveEditOptionDoc(displayjudgeApproveEditOptionDoc)
            .setJudgeApproveEditOptionDoc(judgeApproveEditOptionDoc)
            .setIsOrderProcessedByStayScheduler(isOrderProcessedByStayScheduler)
            .setJudicialByCourtsInitiative(judicialByCourtsInitiative)
            .setDisplayjudgeApproveEditOptionDateForUnlessOrder(displayjudgeApproveEditOptionDateForUnlessOrder)
            .setJudgeApproveEditOptionDateForUnlessOrder(judgeApproveEditOptionDateForUnlessOrder)
            .setIsOrderProcessedByUnlessScheduler(isOrderProcessedByUnlessScheduler)
            .setOrderCourtOwnInitiative(orderCourtOwnInitiative)
            .setOrderCourtOwnInitiativeDate(orderCourtOwnInitiativeDate)
            .setOrderWithoutNotice(orderWithoutNotice)
            .setOrderWithoutNoticeDate(orderWithoutNoticeDate)
            .setShowReasonForDecision(showReasonForDecision);
    }
}
