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

    private String caseNumber;

    private LocalDate currentDate;

    private Party applicant1;
    private Party respondent1;
    private boolean hasRespondent2;
    private Party respondent2;
    private boolean hasApplicant2;
    private Party applicant2;
    private String judgeName;
    private boolean writtenByJudge;
    private ReasonForReconsideration reasonForReconsideration;
    private DecisionOnRequestReconsiderationOptions decisionOnRequestReconsiderationOptions;
    private UpholdingPreviousOrderReason upholdingPreviousOrderReason;

}
