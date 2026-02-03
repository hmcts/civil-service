package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.finalorders.HearingLengthFinalOrderList;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.time.LocalDate;

@Accessors(chain = true)
@Data
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
