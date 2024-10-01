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
        JudgmentDetailsCJES.JudgmentDetailsCJESBuilder requestBody = JudgmentDetailsCJES.builder();
        JudgmentDetails judgmentDetails = getJudgment(caseData, isActiveJudgment);

        if (judgmentDetails != null) {
            requestBody
                .serviceId(SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) ? SPEC_SERVICE_ID : UNSPEC_SERVICE_ID)
                .judgmentId(judgmentDetails.getJudgmentId().toString())
                .judgmentEventTimeStamp(judgmentDetails.getLastUpdateTimeStamp())
                .courtEPIMsId(judgmentDetails.getCourtLocation())
                .ccdCaseRef(caseData.getCcdCaseReference().toString())
                .caseNumber(caseData.getLegacyCaseReference())
                .judgmentAdminOrderTotal(Double.valueOf(judgmentDetails.getTotalAmount()))
                .judgmentAdminOrderDate(judgmentDetails.getIssueDate())
                .registrationType(RegistrationType.valueOf(judgmentDetails.getRtlState()).getRegistrationType())
                .cancellationDate(judgmentDetails.getCancelDate())
                .defendant1(JudgmentDefendantDetails.builder()
                                .defendantName(judgmentDetails.getDefendant1Name())
                                .defendantDateOfBirth(judgmentDetails.getDefendant1Dob())
                                .defendantAddress(judgmentDetails.getDefendant1Address())
                                .build());

            if (judgmentDetails.getDefendant2Name() != null && judgmentDetails.getDefendant2Address() != null) {
                requestBody.defendant2(JudgmentDefendantDetails.builder()
                                           .defendantName(judgmentDetails.getDefendant2Name())
                                           .defendantDateOfBirth(judgmentDetails.getDefendant2Dob())
                                           .defendantAddress(judgmentDetails.getDefendant2Address())
                                           .build());
            }

            return requestBody.build();
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
