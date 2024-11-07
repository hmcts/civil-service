package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
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
        updatedData.fastTrackHearingTime(FastTrackHearingTime.builder()
                .dateFrom(LocalDate.now().plusWeeks(22))
                .dateTo(LocalDate.now().plusWeeks(30))
                .dateToToggle(dateToShowTrue)
                .helpText1(
                        "If either party considers that the time estimate is insufficient, " +
                                "they must inform the court within 7 days of the date of this " +
                                "order.")
                .helpText2(
                        "Not more than seven nor less than three clear days before the " +
                                "trial, the claimant must file at court and serve an indexed and" +
                                " paginated bundle of documents which complies with the " +
                                "requirements of Rule 39.5 Civil Procedure Rules and which " +
                                "complies with requirements of PD32." +
                                " The parties must endeavour to agree the contents of the bundle" +
                                " before it is filed. The bundle will include a case summary and" +
                                " a chronology.")
                .build());
    }
}
