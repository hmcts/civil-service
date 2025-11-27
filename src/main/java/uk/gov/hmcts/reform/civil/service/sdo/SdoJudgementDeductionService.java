package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgementDeductionValue;

import java.util.Optional;

/**
 * Central place for the deduction-value cloning logic shared across all tracks.
 */
@Service
public class SdoJudgementDeductionService {

    public void populateJudgementDeductionValues(CaseData caseData) {
        Optional.ofNullable(caseData.getDrawDirectionsOrder())
            .map(JudgementSum::getJudgementSum)
            .map(deduction -> deduction + "%")
            .ifPresent(deductionPercentage -> {
                caseData.setDisposalHearingJudgementDeductionValue(
                    DisposalHearingJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build()
                );

                caseData.setFastTrackJudgementDeductionValue(
                    FastTrackJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build()
                );

                caseData.setSmallClaimsJudgementDeductionValue(
                    SmallClaimsJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build()
                );
            });
    }
}
