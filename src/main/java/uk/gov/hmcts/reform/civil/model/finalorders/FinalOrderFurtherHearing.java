package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FinalOrderFurtherHearing {
    private LocalDate listFromDate;
    private LocalDate dateToDate;
    private CaseHearingLengthElement lengthListOther;
    private DynamicList alternativeHearingList;
    private HearingMethodFinalOrderList hearingMethodList;
    private String hearingNotesText;
}
