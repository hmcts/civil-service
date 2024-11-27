package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastTrackHousingDisrepairFieldBuilder implements SdoCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackHousingDisrepair(FastTrackHousingDisrepair.builder()
                .input1(
                        "The claimant must prepare a Scott Schedule of the items in disrepair.")
                .input2("""
                        The columns should be headed:
                          •  Item
                          •  Alleged disrepair
                          •  Defendant’s response
                          •  Reserved for Judge’s use""")
                .input3(
                        "The claimant must upload to the Digital Portal the Scott Schedule with the relevant columns completed by 4pm on")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                        10)))
                .input4(
                        "The defendant must upload to the Digital Portal the amended Scott Schedule with the relevant columns in response completed by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                        12)))
                .build());
    }
}
