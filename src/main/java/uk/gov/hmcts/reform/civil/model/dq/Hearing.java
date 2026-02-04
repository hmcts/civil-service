package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.HearingLength;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Hearing {

    private HearingLength hearingLength;
    private String hearingLengthHours;
    private String hearingLengthDays;
    private YesOrNo unavailableDatesRequired;
    private List<Element<UnavailableDate>> unavailableDates;

    public Hearing copy() {
        return new Hearing()
            .setHearingLength(hearingLength)
            .setHearingLengthHours(hearingLengthHours)
            .setHearingLengthDays(hearingLengthDays)
            .setUnavailableDatesRequired(unavailableDatesRequired)
            .setUnavailableDates(unavailableDates);
    }

}
