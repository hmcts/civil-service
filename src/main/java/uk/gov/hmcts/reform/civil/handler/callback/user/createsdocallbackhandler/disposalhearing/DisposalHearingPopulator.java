package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgementDeductionValue;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisposalHearingPopulator {

    private final List<SdoCaseFieldBuilder> disposalHearingBuilders;

    public void setDisposalHearingFields(CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        log.info("Setting disposal hearing fields for case data");
        disposalHearingBuilders.forEach(disposalHearingBuilder -> disposalHearingBuilder.build(updatedData));
        updateDeductionValue(caseData, updatedData);
    }

    private void updateDeductionValue(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        Optional.ofNullable(caseData.getDrawDirectionsOrder())
            .map(JudgementSum::getJudgementSum)
            .map(d -> d + "%")
            .ifPresent(deductionPercentage -> {
                log.info("Updating deduction value to {}", deductionPercentage);
                DisposalHearingJudgementDeductionValue tempDisposalHearingJudgementDeductionValue =
                    DisposalHearingJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.disposalHearingJudgementDeductionValue(tempDisposalHearingJudgementDeductionValue);

                FastTrackJudgementDeductionValue tempFastTrackJudgementDeductionValue =
                    FastTrackJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.fastTrackJudgementDeductionValue(tempFastTrackJudgementDeductionValue).build();

                SmallClaimsJudgementDeductionValue tempSmallClaimsJudgementDeductionValue =
                    SmallClaimsJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.smallClaimsJudgementDeductionValue(tempSmallClaimsJudgementDeductionValue).build();
            });
    }
}
