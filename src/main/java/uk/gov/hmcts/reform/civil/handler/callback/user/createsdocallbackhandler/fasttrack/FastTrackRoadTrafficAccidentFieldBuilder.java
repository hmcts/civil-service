package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastTrackRoadTrafficAccidentFieldBuilder implements SdoCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackRoadTrafficAccident(FastTrackRoadTrafficAccident.builder()
                .input(
                        "Photographs and/or a plan of the accident location shall be prepared and agreed by the parties and uploaded to the Digital Portal by 4pm on")
                .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                        8)))
                .build());
    }
}
