package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;

import java.util.List;

@Data
@Builder
public class Hearing {

    private String hearingLength;
    private YesOrNo unavailableDatesRequired;
    private List<UnavailableDate> unavailableDates;

}
