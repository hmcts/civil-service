package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.fasttrack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class FastTrackHearingTimeFieldBuilder implements SdoCaseFieldBuilder {

    private final List<DateToShowToggle> dateToShowTrue = List.of(DateToShowToggle.SHOW);

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Building FastTrackHearingTime fields for caseId: {}", updatedData.build().getCcdCaseReference());
        updatedData.fastTrackHearingTime(FastTrackHearingTime.builder()
                .dateFrom(LocalDate.now().plusWeeks(22))
                .dateTo(LocalDate.now().plusWeeks(30))
                .dateToToggle(dateToShowTrue)
                .helpText1(
                        "If either party considers that the time estimate is insufficient, " +
                                "they must inform the court within 7 days of the date of this " +
                                "order.")
                .build());
    }
}
