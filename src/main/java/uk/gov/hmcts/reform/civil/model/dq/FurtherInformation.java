package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
public class FurtherInformation {

    private final YesOrNo futureApplications;
    private final String reasonForFutureApplications;
    private final String otherInformationForJudge;
}
