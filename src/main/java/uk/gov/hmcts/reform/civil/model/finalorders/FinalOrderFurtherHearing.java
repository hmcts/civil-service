package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.finalorders.HearingLengthFinalOrderList;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor

public class FinalOrderFurtherHearing {

    private LocalDate listFromDate;
    private LocalDate dateToDate;
    private HearingLengthFinalOrderList lengthList;
    private CaseHearingLengthElement lengthListOther;
    private DynamicList alternativeHearingList;
    private HearingChannel hearingMethodList;
    private String hearingNotesText;
    private DynamicList hearingLocationList;
    private DatesFinalOrders datesToAvoidDateDropdown;
}
