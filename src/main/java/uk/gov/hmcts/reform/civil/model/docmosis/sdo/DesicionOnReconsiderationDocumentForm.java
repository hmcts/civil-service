package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UpholdingPreviousOrderReason;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonForReconsideration;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class DesicionOnReconsiderationDocumentForm implements MappableObject {

    private final String caseNumber;

    private final LocalDate currentDate;

    private final Party applicant1;
    private final Party respondent1;
    private final boolean hasRespondent2;
    private final Party respondent2;
    private final boolean hasApplicant2;
    private final Party applicant2;
    private final String judgeName;
    private final boolean writtenByJudge;
    private ReasonForReconsideration reasonForReconsideration;
    private DecisionOnRequestReconsiderationOptions decisionOnRequestReconsiderationOptions;
    private UpholdingPreviousOrderReason upholdingPreviousOrderReason;

}
