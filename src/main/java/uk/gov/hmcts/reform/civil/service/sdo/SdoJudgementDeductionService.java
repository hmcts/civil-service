package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgementDeductionValue;

import java.util.Optional;

@Service
public class SdoJudgementDeductionService {

    public void populateJudgementDeductionValues(CaseData caseData) {
        Optional.ofNullable(caseData.getDrawDirectionsOrder())
            .map(JudgementSum::getJudgementSum)
            .map(deduction -> deduction + "%")
            .ifPresent(deductionPercentage -> {
                caseData.setDisposalHearingJudgementDeductionValue(
                    new DisposalHearingJudgementDeductionValue().setValue(deductionPercentage)
                );

                caseData.setFastTrackJudgementDeductionValue(
                    new FastTrackJudgementDeductionValue().setValue(deductionPercentage)
                );

                SmallClaimsJudgementDeductionValue smallClaimsValue = new SmallClaimsJudgementDeductionValue();
                smallClaimsValue.setValue(deductionPercentage);
                caseData.setSmallClaimsJudgementDeductionValue(smallClaimsValue);
            });
    }
}
