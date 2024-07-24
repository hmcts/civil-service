package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.HearingLength;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@Builder(toBuilder = true)
public class Hearing {

    private HearingLength hearingLength;
    private String hearingLengthHours;
    private String hearingLengthDays;
    private YesOrNo unavailableDatesRequired;
    private List<Element<UnavailableDate>> unavailableDates;

}
