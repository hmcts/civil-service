package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastTrackPersonalInjuryFieldBuilder implements SdoCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackPersonalInjury(FastTrackPersonalInjury.builder()
                .input1(
                        "The claimant has permission to rely upon the written expert evidence already uploaded to the Digital Portal with the particulars of claim and in" +
                                " addition has permission to rely upon any associated correspondence or updating report which is uploaded to the Digital Portal by 4pm on")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                .input2(
                        "Any questions which are to be addressed to an expert must be sent to the expert directly and uploaded to the Digital Portal by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                .input3(
                        "The answers to the questions shall be answered by the Expert by")
                .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .input4("and uploaded to the Digital Portal by")
                .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .build());
    }
}
