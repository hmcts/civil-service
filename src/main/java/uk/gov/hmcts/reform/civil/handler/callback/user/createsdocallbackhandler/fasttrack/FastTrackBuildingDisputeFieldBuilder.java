package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastTrackBuildingDisputeFieldBuilder implements SdoCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackBuildingDispute(FastTrackBuildingDispute.builder()
                .input1(
                        "The claimant must prepare a Scott Schedule of the defects, items of damage, or any other relevant matters")
                .input2("""
                        The columns should be headed:
                          •  Item
                          •  Alleged defect
                          •  Claimant’s costing
                          •  Defendant’s response
                          •  Defendant’s costing
                          •  Reserved for Judge’s use""")
                .input3(
                        "The claimant must upload to the Digital Portal the Scott Schedule with the relevant columns completed by 4pm on")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                        10)))
                .input4(
                        "The defendant must upload to the Digital Portal an amended version of the Scott Schedule with the relevant columns in response completed by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                        12)))
                .build());
    }
}
