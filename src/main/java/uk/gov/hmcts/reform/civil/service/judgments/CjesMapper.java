package uk.gov.hmcts.reform.civil.service.judgments;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.JudgmentDefendantDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.JudgmentDetailsCJES;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.RegistrationType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Component
@AllArgsConstructor
public class CjesMapper {

    private static final String SPEC_SERVICE_ID = "AAA6";
    private static final String UNSPEC_SERVICE_ID = "AAA7";

    public JudgmentDetailsCJES toJudgmentDetailsCJES(CaseData caseData, Boolean isActiveJudgment) {
        JudgmentDetails judgmentDetails = getJudgment(caseData, isActiveJudgment);

        if (judgmentDetails != null) {
            JudgmentDetailsCJES requestBody = new JudgmentDetailsCJES();
            requestBody.setServiceId(SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) ? SPEC_SERVICE_ID : UNSPEC_SERVICE_ID);
            requestBody.setJudgmentId(judgmentDetails.getJudgmentId().toString());
            requestBody.setJudgmentEventTimeStamp(judgmentDetails.getLastUpdateTimeStamp());
            requestBody.setCourtEPIMsId(judgmentDetails.getCourtLocation());
            requestBody.setCcdCaseRef(caseData.getCcdCaseReference().toString());
            requestBody.setCaseNumber(caseData.getLegacyCaseReference());
            requestBody.setJudgmentAdminOrderTotal(Double.valueOf(judgmentDetails.getTotalAmount()));
            requestBody.setJudgmentAdminOrderDate(judgmentDetails.getIssueDate());
            requestBody.setRegistrationType(RegistrationType.valueOf(judgmentDetails.getRtlState()).getRegistrationType());
            requestBody.setCancellationDate(judgmentDetails.getCancelDate());

            JudgmentDefendantDetails defendant1 = new JudgmentDefendantDetails();
            defendant1.setDefendantName(judgmentDetails.getDefendant1Name());
            defendant1.setDefendantDateOfBirth(judgmentDetails.getDefendant1Dob());
            defendant1.setDefendantAddress(judgmentDetails.getDefendant1Address());
            requestBody.setDefendant1(defendant1);

            if (judgmentDetails.getDefendant2Name() != null && judgmentDetails.getDefendant2Address() != null) {
                JudgmentDefendantDetails defendant2 = new JudgmentDefendantDetails();
                defendant2.setDefendantName(judgmentDetails.getDefendant2Name());
                defendant2.setDefendantDateOfBirth(judgmentDetails.getDefendant2Dob());
                defendant2.setDefendantAddress(judgmentDetails.getDefendant2Address());
                requestBody.setDefendant2(defendant2);
            }

            return requestBody;
        }
        throw new IllegalArgumentException("Judgment details cannot be null");
    }

    private JudgmentDetails getJudgment(CaseData caseData, Boolean isActiveJudgment) {
        JudgmentDetails judgmentDetails = caseData.getActiveJudgment();

        if (!isActiveJudgment && !caseData.getHistoricJudgment().isEmpty()) {
            judgmentDetails = unwrapElements(caseData.getHistoricJudgment()).get(0);
        }
        return judgmentDetails;
    }

}
